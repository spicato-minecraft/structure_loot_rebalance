package structure_loot_rebalance.loot;

import org.junit.jupiter.api.Test;
import structure_loot_rebalance.config.LootTableConfig;
import structure_loot_rebalance.config.LootTheme;
import structure_loot_rebalance.config.LootTier;
import structure_loot_rebalance.config.StructureClearRates;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LootTableModifierTest {

	@Test
	void themedTier2AddsTwoPrimaryPools() {
		LootTableConfig config = LootTableConfig.withTheme(
				LootTableConfig.of(LootTier.TIER_2, "ancient_city", 8, false),
				LootTheme.SWIFT_SNEAK
		);
		int[] poolCount = {0};
		LootTableModifier.addModBookPools(config, pool -> poolCount[0]++);
		assertEquals(2, poolCount[0]);
	}

	@Test
	void tier3AddsThreePrimaryAndMendingPools() {
		LootTableConfig config = LootTableConfig.of(LootTier.TIER_3, "stronghold", 7, true);
		int[] poolCount = {0};
		LootTableModifier.addModBookPools(config, pool -> poolCount[0]++);
		assertEquals(4, poolCount[0]);
	}

	@Test
	void tier1AddsPrimaryPoolOnly() {
		LootTableConfig config = LootTableConfig.of(LootTier.TIER_1, "mineshaft", 6, false);
		int[] poolCount = {0};
		LootTableModifier.addModBookPools(config, pool -> poolCount[0]++);
		assertEquals(1, poolCount[0]);
	}

	@Test
	void tier2AddsTwoPrimaryPools() {
		LootTableConfig config = LootTableConfig.of(LootTier.TIER_2, "desert_pyramid", 4, false);
		int[] poolCount = {0};
		LootTableModifier.addModBookPools(config, pool -> poolCount[0]++);
		assertEquals(2, poolCount[0]);
	}

	@Test
	void buriedTreasureAddsPrimaryPoolOnly() {
		LootTableConfig config = LootTableConfig.buriedTreasure();
		int[] poolCount = {0};
		LootTableModifier.addModBookPools(config, pool -> poolCount[0]++);
		assertEquals(1, poolCount[0]);
	}

	@Test
	void trialSupplySkipsMendingPool() {
		LootTableConfig config = LootTableConfig.of(LootTier.TIER_3, "trial_chambers", 20, false);
		StructureClearRates.Rates rates = StructureClearRates.forTable(config);
		assertEquals(0.0, rates.mending(), 0.001);

		int[] poolCount = {0};
		LootTableModifier.addModBookPools(config, pool -> poolCount[0]++);
		assertEquals(3, poolCount[0]);
	}
}
