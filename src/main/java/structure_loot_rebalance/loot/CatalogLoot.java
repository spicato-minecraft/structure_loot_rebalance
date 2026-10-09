package structure_loot_rebalance.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Non-vanilla structure layers from the loot-builder export.
 * Entries inside a roll compete. Separate layers on the same table all fire.
 * Independent Mending rolls stay their own layer even when the export left them unlinked.
 */
public final class CatalogLoot {

	static final String SUPPORTED_VERSION = "26.2";
	private static final String RESOURCE = "structure_loot_rebalance/loot_catalog.json";
	private static final Pattern MENDING_ROLL = Pattern.compile("^slr:[^:]+:(minecraft:.+):mending$");

	private final Map<Identifier, List<CatalogLayer>> byTable;

	private CatalogLoot(Map<Identifier, List<CatalogLayer>> byTable) {
		this.byTable = byTable;
	}

	public static CatalogLoot loadBundled() throws IOException {
		InputStream in = CatalogLoot.class.getClassLoader().getResourceAsStream(RESOURCE);
		if (in == null) {
			throw new IOException("Missing bundled loot catalog " + RESOURCE);
		}
		try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			return parse(JsonParser.parseReader(reader).getAsJsonObject());
		}
	}

	public static CatalogLoot parse(JsonObject root) {
		String version = root.has("minecraft_version") ? root.get("minecraft_version").getAsString() : "";
		if (!SUPPORTED_VERSION.equals(version)) {
			throw new IllegalArgumentException(
					"Loot catalog minecraft_version " + version + " cannot be applied on the " + SUPPORTED_VERSION + " branch"
			);
		}

		Map<String, JsonObject> rolls = new LinkedHashMap<>();
		for (JsonElement element : root.getAsJsonArray("rolls")) {
			JsonObject roll = element.getAsJsonObject();
			rolls.put(roll.get("id").getAsString(), roll);
		}

		Map<Identifier, List<CatalogLayer>> byTable = new LinkedHashMap<>();
		Set<String> linked = new java.util.HashSet<>();
		for (JsonElement structureElement : root.getAsJsonArray("structures")) {
			JsonObject structure = structureElement.getAsJsonObject();
			for (JsonElement layerElement : structure.getAsJsonArray("layers")) {
				JsonObject layer = layerElement.getAsJsonObject();
				if ("vanilla".equals(text(layer, "source"))) {
					continue;
				}
				String rollId = layer.get("roll").getAsString();
				String tableId = text(layer, "table");
				if (tableId == null || !rolls.containsKey(rollId)) {
					continue;
				}
				int copies = layer.has("copies") ? Math.max(0, layer.get("copies").getAsInt()) : 1;
				if (copies < 1) {
					continue;
				}
				List<CatalogEntry> entries = resolveRoll(rollId, rolls, new java.util.HashSet<>());
				if (entries.isEmpty()) {
					continue;
				}
				linked.add(rollId);
				byTable.computeIfAbsent(Identifier.parse(tableId), ignored -> new ArrayList<>())
						.add(new CatalogLayer(rollId, text(layer, "source"), copies, List.copyOf(entries)));
			}
		}

		for (String rollId : rolls.keySet()) {
			Matcher matcher = MENDING_ROLL.matcher(rollId);
			if (!matcher.matches() || linked.contains(rollId)) {
				continue;
			}
			Identifier tableId = Identifier.parse(matcher.group(1));
			if (!byTable.containsKey(tableId)) {
				continue;
			}
			List<CatalogEntry> entries = resolveRoll(rollId, rolls, new java.util.HashSet<>());
			if (entries.isEmpty()) {
				continue;
			}
			byTable.get(tableId).add(new CatalogLayer(rollId, "slr", 1, List.copyOf(entries)));
		}

		Map<Identifier, List<CatalogLayer>> frozen = new LinkedHashMap<>();
		for (var entry : byTable.entrySet()) {
			frozen.put(entry.getKey(), List.copyOf(entry.getValue()));
		}
		return new CatalogLoot(Map.copyOf(frozen));
	}

	public boolean modifies(Identifier tableId) {
		return byTable.containsKey(tableId);
	}

	public Set<Identifier> modifiedTables() {
		return byTable.keySet();
	}

	public List<CatalogLayer> layers(Identifier tableId) {
		return byTable.getOrDefault(tableId, List.of());
	}

	private static List<CatalogEntry> resolveRoll(String rollId, Map<String, JsonObject> rolls, Set<String> stack) {
		if (!stack.add(rollId)) {
			return List.of();
		}
		JsonObject roll = rolls.get(rollId);
		if (roll == null) {
			stack.remove(rollId);
			return List.of();
		}
		List<CatalogEntry> entries = new ArrayList<>();
		JsonArray rawEntries = roll.getAsJsonArray("entries");
		if (rawEntries != null) {
			for (JsonElement element : rawEntries) {
				CatalogEntry entry = resolveEntry(element.getAsJsonObject(), rolls, stack);
				if (entry != null) {
					entries.add(entry);
				}
			}
		}
		stack.remove(rollId);
		return entries;
	}

	private static CatalogEntry resolveEntry(JsonObject raw, Map<String, JsonObject> rolls, Set<String> stack) {
		int weight = raw.has("weight") ? raw.get("weight").getAsInt() : 0;
		if (weight <= 0) {
			return null;
		}
		String kind = text(raw, "kind");
		if (kind == null) {
			return null;
		}
		return switch (kind) {
			case "empty" -> CatalogEntry.empty(weight);
			case "item" -> itemEntry(raw, weight);
			case "group", "roll" -> nestedEntry(kind, raw, weight, rolls, stack);
			default -> null;
		};
	}

	private static CatalogEntry itemEntry(JsonObject raw, int weight) {
		String item = text(raw, "item");
		if (item == null || item.isBlank()) {
			return null;
		}
		int countMin = raw.has("count_min") ? raw.get("count_min").getAsInt() : 1;
		int countMax = raw.has("count_max") ? raw.get("count_max").getAsInt() : countMin;
		if (countMax < countMin) {
			countMax = countMin;
		}
		int enchantLevel = raw.has("enchant_level") ? raw.get("enchant_level").getAsInt() : 1;
		return CatalogEntry.item(
				weight,
				item,
				text(raw, "enchant"),
				Math.max(1, enchantLevel),
				text(raw, "potion"),
				Math.max(1, countMin),
				Math.max(1, countMax)
		);
	}

	private static CatalogEntry nestedEntry(
			String kind,
			JsonObject raw,
			int weight,
			Map<String, JsonObject> rolls,
			Set<String> stack
	) {
		String nestedId = text(raw, "roll");
		if (nestedId == null) {
			return null;
		}
		List<CatalogEntry> children = resolveRoll(nestedId, rolls, stack);
		if (children.isEmpty()) {
			return null;
		}
		if ("group".equals(kind)) {
			return CatalogEntry.group(weight, children);
		}
		return CatalogEntry.roll(weight, children);
	}

	private static String text(JsonObject obj, String key) {
		if (!obj.has(key) || obj.get(key).isJsonNull()) {
			return null;
		}
		return obj.get(key).getAsString();
	}

	public enum EntryKind {
		ITEM,
		EMPTY,
		GROUP,
		ROLL
	}

	public record CatalogLayer(String rollId, String source, int copies, List<CatalogEntry> entries) {
	}

	public record CatalogEntry(
			EntryKind kind,
			int weight,
			String item,
			String enchant,
			int enchantLevel,
			String potion,
			int countMin,
			int countMax,
			List<CatalogEntry> children
	) {
		static CatalogEntry empty(int weight) {
			return new CatalogEntry(EntryKind.EMPTY, weight, null, null, 1, null, 1, 1, List.of());
		}

		static CatalogEntry item(
				int weight,
				String item,
				String enchant,
				int enchantLevel,
				String potion,
				int countMin,
				int countMax
		) {
			return new CatalogEntry(
					EntryKind.ITEM, weight, item, enchant, enchantLevel, potion, countMin, countMax, List.of()
			);
		}

		static CatalogEntry group(int weight, List<CatalogEntry> children) {
			return new CatalogEntry(EntryKind.GROUP, weight, null, null, 1, null, 1, 1, List.copyOf(children));
		}

		static CatalogEntry roll(int weight, List<CatalogEntry> children) {
			return new CatalogEntry(EntryKind.ROLL, weight, null, null, 1, null, 1, 1, List.copyOf(children));
		}
	}
}
