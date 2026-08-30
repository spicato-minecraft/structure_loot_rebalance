package structure_loot_rebalance.config;

import com.google.gson.JsonParseException;

import java.util.Locale;
import java.util.Map;

/**
 * Structure signature enchants merged into the primary pool at {@link LootTableConfig#DEFAULT_THEME_WEIGHT_MULTIPLIER}.
 */
public enum LootTheme {
	UNDERWATER(
			"underwater",
			Map.of(
					"minecraft:respiration", 1,
					"minecraft:depth_strider", 1
			)
	),
	SWIFT_SNEAK(
			"swift_sneak",
			Map.of("minecraft:swift_sneak", 1)
	),
	SOUL_SPEED(
			"soul_speed",
			Map.of("minecraft:soul_speed", 1)
	),
	FORTUNE(
			"fortune",
			Map.of("minecraft:fortune", 1)
	);

	private final String id;
	private final Map<String, Integer> unitWeights;

	LootTheme(String id, Map<String, Integer> unitWeights) {
		this.id = id;
		this.unitWeights = Map.copyOf(unitWeights);
	}

	public String id() {
		return id;
	}

	public Map<String, Integer> unitWeights() {
		return unitWeights;
	}

	public static LootTheme fromId(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new JsonParseException("theme id is required");
		}
		String normalized = raw.trim().toLowerCase(Locale.ROOT);
		for (LootTheme theme : values()) {
			if (theme.id.equals(normalized)) {
				return theme;
			}
		}
		throw new JsonParseException("Unknown theme: " + raw);
	}
}
