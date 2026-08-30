package structure_loot_rebalance.config;

import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default tier map and per-structure loot event counts (n).
 */
public final class StructureTierRegistry {

	private static final double TIER_1_RARE_MULTIPLIER = 0.25;

	private StructureTierRegistry() {
	}

	public static Map<Identifier, LootTableConfig> buildDefaults() {
		Map<Identifier, LootTableConfig> defaults = new LinkedHashMap<>();

		// Tier 1 — primary only
		register(defaults, LootTier.TIER_1, "mineshaft", 6, false, null,
				"minecraft:chests/abandoned_mineshaft");
		register(defaults, LootTier.TIER_1, "dungeon", 2, false, null,
				"minecraft:chests/simple_dungeon");
		register(defaults, LootTier.TIER_1, "pillager_outpost", 1, false, null,
				"minecraft:chests/pillager_outpost");

		registerRare(defaults, "village_temple", 1,
				"minecraft:chests/village_temple");
		registerRare(defaults, "ruined_portal", 1,
				"minecraft:chests/ruined_portal");

		registerThemed(defaults, LootTier.TIER_1, "shipwreck", 4, false, null, LootTheme.UNDERWATER,
				"minecraft:chests/shipwreck_treasure");
		registerThemed(defaults, LootTier.TIER_1, "underwater_ruin", 2, false, null, LootTheme.UNDERWATER,
				"minecraft:chests/underwater_ruin_big",
				"minecraft:chests/underwater_ruin_small");

		defaults.put(
				id("minecraft:chests/buried_treasure"),
				LootTableConfig.buriedTreasure()
		);

		// Tier 2 — 2 primary rolls per chest (~6.7 books / clear)
		register(defaults, LootTier.TIER_2, "desert_pyramid", 4, false, null,
				"minecraft:chests/desert_pyramid");
		register(defaults, LootTier.TIER_2, "jungle_temple", 2, false, null,
				"minecraft:chests/jungle_temple");
		registerThemed(
				defaults,
				LootTier.TIER_2,
				"ancient_city",
				18,
				true,
				StructureClearRates.ANCIENT_CITY_MENDING_EXPECTED,
				LootTheme.SWIFT_SNEAK,
				"minecraft:chests/ancient_city"
		);
		registerThemed(
				defaults,
				LootTier.TIER_2,
				"nether_fortress",
				8,
				true,
				StructureClearRates.NETHER_FORTRESS_MENDING_EXPECTED,
				LootTheme.SOUL_SPEED,
				"minecraft:chests/nether_bridge"
		);

		// Tier 3 — 3 primary rolls per chest + independent Mending roll (where enabled)
		register(defaults, LootTier.TIER_3, "stronghold", 7, true, null,
				"minecraft:chests/stronghold_corridor",
				"minecraft:chests/stronghold_crossing",
				"minecraft:chests/stronghold_library");
		register(defaults, LootTier.TIER_3, "ancient_city_ice_box", 4, true,
				StructureClearRates.ANCIENT_CITY_ICE_BOX_MENDING_EXPECTED,
				"minecraft:chests/ancient_city_ice_box");
		register(defaults, LootTier.TIER_3, "woodland_mansion", 15, true,
				StructureClearRates.WOODLAND_MANSION_MENDING_EXPECTED,
				"minecraft:chests/woodland_mansion");
		registerThemed(defaults, LootTier.TIER_3, "bastion", 8, true, null, LootTheme.SOUL_SPEED,
				"minecraft:chests/bastion_treasure",
				"minecraft:chests/bastion_other",
				"minecraft:chests/bastion_bridge",
				"minecraft:chests/bastion_hoglin_stable");

		register(defaults, LootTier.TIER_2, "trial_chambers", 20, false, null,
				"minecraft:chests/trial_chambers/supply",
				"minecraft:chests/trial_chambers/corridor");
		register(defaults, LootTier.TIER_3, "trial_chambers", 12, true, null,
				"minecraft:chests/trial_chambers/reward");

		return Map.copyOf(defaults);
	}

	private static void registerRare(
			Map<Identifier, LootTableConfig> defaults,
			String structureId,
			int lootEvents,
			String... tableIds
	) {
		for (String tableId : tableIds) {
			defaults.put(id(tableId), LootTableConfig.ofRare(
					LootTier.TIER_1_RARE, structureId, lootEvents, TIER_1_RARE_MULTIPLIER));
		}
	}

	private static void register(
			Map<Identifier, LootTableConfig> defaults,
			LootTier tier,
			String structureId,
			int lootEvents,
			boolean mendingEnabled,
			Double mendingExpectedPerClear,
			String... tableIds
	) {
		for (String tableId : tableIds) {
			defaults.put(id(tableId), LootTableConfig.of(
					tier, structureId, lootEvents, mendingEnabled, mendingExpectedPerClear));
		}
	}

	private static void registerThemed(
			Map<Identifier, LootTableConfig> defaults,
			LootTier tier,
			String structureId,
			int lootEvents,
			boolean mendingEnabled,
			Double mendingExpectedPerClear,
			LootTheme theme,
			String... tableIds
	) {
		for (String tableId : tableIds) {
			LootTableConfig base = LootTableConfig.of(
					tier, structureId, lootEvents, mendingEnabled, mendingExpectedPerClear);
			defaults.put(id(tableId), LootTableConfig.withTheme(base, theme));
		}
	}

	private static Identifier id(String tableId) {
		return Identifier.parse(tableId);
	}
}
