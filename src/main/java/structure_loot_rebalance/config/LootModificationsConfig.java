package structure_loot_rebalance.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import structure_loot_rebalance.StructureLootRebalance;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root config for loot-modifications.json (config v5).
 */
public record LootModificationsConfig(Map<Identifier, LootTableConfig> lootModifications) {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static LootModificationsConfig load(String json) {
		try {
			JsonObject root = JsonParser.parseString(json).getAsJsonObject();
			JsonObject mods = root.getAsJsonObject("loot_modifications");
			Map<Identifier, LootTableConfig> map = new LinkedHashMap<>();

			if (mods != null) {
				for (Map.Entry<String, JsonElement> entry : mods.entrySet()) {
					Identifier tableId = Identifier.tryParse(entry.getKey());
					if (tableId == null) {
						StructureLootRebalance.LOGGER.warn("Skipping invalid loot table id: {}", entry.getKey());
						continue;
					}
					try {
						LootTableConfig table = parseTableConfig(entry.getValue().getAsJsonObject());
						map.put(tableId, table);
					} catch (Exception ex) {
						StructureLootRebalance.LOGGER.warn("Skipping invalid table config for {}: {}", entry.getKey(), ex.getMessage());
					}
				}
			}

			return new LootModificationsConfig(map);
		} catch (Exception e) {
			throw new JsonParseException("Failed to parse loot modifications config", e);
		}
	}

	private static LootTableConfig parseTableConfig(JsonObject obj) {
		LootTier tier = parseTier(obj);
		String structureId = requiredString(obj, "structure_id");
		int lootEvents = requiredPositiveInt(obj, "structure_loot_events");
		boolean mendingEnabled = !obj.has("mending_enabled") || obj.get("mending_enabled").getAsBoolean();
		double rateMultiplier = obj.has("rate_multiplier") ? obj.get("rate_multiplier").getAsDouble() : 1.0;
		Map<String, Integer> baseWeights = parseBaseWeights(obj.getAsJsonObject("base_weights"));
		LootTheme theme = obj.has("theme") && !obj.get("theme").isJsonNull()
				? LootTheme.fromId(obj.get("theme").getAsString())
				: null;
		double themeWeightMultiplier = obj.has("theme_weight_multiplier")
				? obj.get("theme_weight_multiplier").getAsDouble()
				: LootTableConfig.DEFAULT_THEME_WEIGHT_MULTIPLIER;
		Double mendingExpected = obj.has("mending_expected") && !obj.get("mending_expected").isJsonNull()
				? obj.get("mending_expected").getAsDouble()
				: null;

		return new LootTableConfig(
				tier, structureId, lootEvents, mendingEnabled, baseWeights, rateMultiplier, theme,
				themeWeightMultiplier, mendingExpected
		);
	}

	private static LootTier parseTier(JsonObject obj) {
		if (!obj.has("tier")) {
			throw new JsonParseException("Missing tier");
		}
		int tier = obj.get("tier").getAsInt();
		return switch (tier) {
			case 1 -> obj.has("rate_multiplier") && obj.get("rate_multiplier").getAsDouble() < 1.0
					? LootTier.TIER_1_RARE
					: LootTier.TIER_1;
			case 2 -> LootTier.TIER_2;
			case 3 -> LootTier.TIER_3;
			default -> throw new JsonParseException("Invalid tier: " + tier);
		};
	}

	private static Map<String, Integer> parseBaseWeights(JsonObject weights) {
		if (weights == null) {
			return LootTableConfig.BASE_WEIGHTS;
		}
		Map<String, Integer> map = parseOptionalWeights(weights);
		return map.isEmpty() ? LootTableConfig.BASE_WEIGHTS : map;
	}

	private static Map<String, Integer> parseOptionalWeights(JsonObject weights) {
		if (weights == null) {
			return Map.of();
		}
		Map<String, Integer> map = new LinkedHashMap<>();
		for (Map.Entry<String, JsonElement> entry : weights.entrySet()) {
			int weight = entry.getValue().getAsInt();
			if (weight > 0) {
				map.put(entry.getKey(), weight);
			}
		}
		return map.isEmpty() ? Map.of() : Map.copyOf(map);
	}

	private static String requiredString(JsonObject obj, String key) {
		if (!obj.has(key)) {
			throw new JsonParseException("Missing " + key);
		}
		return obj.get(key).getAsString();
	}

	private static int requiredPositiveInt(JsonObject obj, String key) {
		if (!obj.has(key)) {
			throw new JsonParseException("Missing " + key);
		}
		int value = obj.get(key).getAsInt();
		if (value < 1) {
			throw new JsonParseException(key + " must be >= 1");
		}
		return value;
	}

	public String toJson() {
		JsonObject root = new JsonObject();
		root.addProperty("schema_version", StructureClearRates.SCHEMA_VERSION);
		JsonObject mods = new JsonObject();
		for (Map.Entry<Identifier, LootTableConfig> entry : lootModifications.entrySet()) {
			mods.add(entry.getKey().toString(), serializeTableConfig(entry.getValue()));
		}
		root.add("loot_modifications", mods);
		return GSON.toJson(root);
	}

	private static JsonObject serializeTableConfig(LootTableConfig config) {
		JsonObject obj = new JsonObject();
		obj.addProperty("tier", config.tier().level());
		obj.addProperty("structure_id", config.structureId());
		obj.addProperty("structure_loot_events", config.structureLootEvents());
		obj.addProperty("mending_enabled", config.mendingEnabled());
		if (config.rateMultiplier() != 1.0) {
			obj.addProperty("rate_multiplier", config.rateMultiplier());
		}

		if (!config.baseWeights().equals(LootTableConfig.BASE_WEIGHTS)) {
			JsonObject weights = new JsonObject();
			for (Map.Entry<String, Integer> weight : config.baseWeights().entrySet()) {
				weights.addProperty(weight.getKey(), weight.getValue());
			}
			obj.add("base_weights", weights);
		}

		if (config.theme() != null) {
			obj.addProperty("theme", config.theme().id());
		}
		if (config.themeWeightMultiplier() != LootTableConfig.DEFAULT_THEME_WEIGHT_MULTIPLIER) {
			obj.addProperty("theme_weight_multiplier", config.themeWeightMultiplier());
		}
		if (config.mendingExpectedPerClear() != null) {
			obj.addProperty("mending_expected", config.mendingExpectedPerClear());
		}
		return obj;
	}
}
