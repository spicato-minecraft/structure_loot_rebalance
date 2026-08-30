package structure_loot_rebalance.config;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructureTierRegistryTest {

	@Test
	void primaryBaseExcludesFeatherFalling() {
		assertFalse(LootTableConfig.BASE_WEIGHTS.containsKey("minecraft:feather_falling"));
	}

	@Test
	void tier1BaseIncludesProtection() {
		var defaults = StructureTierRegistry.buildDefaults();
		LootTableConfig mineshaft = defaults.get(Identifier.parse("minecraft:chests/abandoned_mineshaft"));

		assertNotNull(mineshaft);
		assertTrue(mineshaft.baseWeights().containsKey("minecraft:protection"));
	}

	@Test
	void onlyShipwreckTreasureIsModded() {
		var defaults = StructureTierRegistry.buildDefaults();

		assertNotNull(defaults.get(Identifier.parse("minecraft:chests/shipwreck_treasure")));
		assertFalse(defaults.containsKey(Identifier.parse("minecraft:chests/shipwreck_supply")));
		assertFalse(defaults.containsKey(Identifier.parse("minecraft:chests/shipwreck_map")));
	}

	@Test
	void shipwreckTreasureHasUnderwaterThemeInPrimary() {
		var defaults = StructureTierRegistry.buildDefaults();
		LootTableConfig treasure = defaults.get(Identifier.parse("minecraft:chests/shipwreck_treasure"));

		assertNotNull(treasure);
		assertEquals(LootTheme.UNDERWATER, treasure.theme());
		assertTrue(treasure.primaryWeights().containsKey("minecraft:respiration"));
		assertTrue(treasure.primaryWeights().get("minecraft:respiration") > 1);
	}

	@Test
	void underwaterRuinsAreTier1WithUnderwaterTheme() {
		var defaults = StructureTierRegistry.buildDefaults();

		for (String tableId : new String[] {
				"minecraft:chests/underwater_ruin_big",
				"minecraft:chests/underwater_ruin_small"
		}) {
			LootTableConfig ruin = defaults.get(Identifier.parse(tableId));
			assertNotNull(ruin);
			assertEquals(LootTier.TIER_1, ruin.tier());
			assertEquals(LootTheme.UNDERWATER, ruin.theme());
		}
	}

	@Test
	void buriedTreasureIsFortuneFocusedTier1() {
		var defaults = StructureTierRegistry.buildDefaults();
		LootTableConfig buried = defaults.get(Identifier.parse("minecraft:chests/buried_treasure"));

		assertNotNull(buried);
		assertEquals(LootTier.TIER_1, buried.tier());
		assertEquals(LootTheme.FORTUNE, buried.theme());
		assertEquals(LootTableConfig.FORTUNE_ONLY_WEIGHTS, buried.baseWeights());
		assertEquals(0.5, StructureClearRates.forTable(buried).primaryPerRoll(), 0.002);
		assertTrue(buried.primaryWeights().containsKey("minecraft:fortune"));
	}

	@Test
	void ancientCityHasSwiftSneakThemeAndMending() {
		var defaults = StructureTierRegistry.buildDefaults();
		LootTableConfig ancientCity = defaults.get(Identifier.parse("minecraft:chests/ancient_city"));

		assertNotNull(ancientCity);
		assertEquals(LootTier.TIER_2, ancientCity.tier());
		assertEquals(LootTheme.SWIFT_SNEAK, ancientCity.theme());
		assertTrue(ancientCity.mendingEnabled());
		assertTrue(ancientCity.primaryWeights().containsKey("minecraft:swift_sneak"));
	}

	@Test
	void netherFortressIsTier2WithMending() {
		var defaults = StructureTierRegistry.buildDefaults();
		LootTableConfig fortress = defaults.get(Identifier.parse("minecraft:chests/nether_bridge"));

		assertNotNull(fortress);
		assertEquals(LootTier.TIER_2, fortress.tier());
		assertEquals(LootTheme.SOUL_SPEED, fortress.theme());
		assertTrue(fortress.mendingEnabled());
	}

	@Test
	void netherStructuresHaveSoulSpeedTheme() {
		var defaults = StructureTierRegistry.buildDefaults();

		assertEquals(LootTheme.SOUL_SPEED, defaults.get(Identifier.parse("minecraft:chests/nether_bridge")).theme());
		assertEquals(LootTheme.SOUL_SPEED, defaults.get(Identifier.parse("minecraft:chests/bastion_treasure")).theme());
	}

	@Test
	void endCityAndOminousVaultAreAbsent() {
		var defaults = StructureTierRegistry.buildDefaults();

		assertFalse(defaults.containsKey(Identifier.parse("minecraft:chests/end_city_treasure")));
		assertFalse(defaults.containsKey(Identifier.parse("minecraft:chests/trial_chambers/reward_ominous")));
	}

	@Test
	void trialRewardHasMendingSupplyDoesNot() {
		var defaults = StructureTierRegistry.buildDefaults();

		LootTableConfig reward = defaults.get(Identifier.parse("minecraft:chests/trial_chambers/reward"));
		LootTableConfig supply = defaults.get(Identifier.parse("minecraft:chests/trial_chambers/supply"));

		assertNotNull(reward);
		assertNotNull(supply);
		assertTrue(reward.mendingEnabled());
		assertFalse(supply.mendingEnabled());
	}

	@Test
	void configV3RoundTripPreservesTheme() {
		LootTableConfig original = LootTableConfig.withTheme(
				LootTableConfig.of(LootTier.TIER_1, "shipwreck", 4, false),
				LootTheme.UNDERWATER
		);
		var config = new LootModificationsConfig(java.util.Map.of(
				Identifier.parse("minecraft:chests/shipwreck_treasure"),
				original
		));

		LootModificationsConfig parsed = LootModificationsConfig.load(config.toJson());
		LootTableConfig loaded = parsed.lootModifications().get(Identifier.parse("minecraft:chests/shipwreck_treasure"));

		assertNotNull(loaded);
		assertEquals(LootTheme.UNDERWATER, loaded.theme());
		assertNull(LootTableConfig.of(LootTier.TIER_1, "shipwreck", 4, false).theme());
	}
}
