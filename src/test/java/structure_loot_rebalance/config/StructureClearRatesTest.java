package structure_loot_rebalance.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StructureClearRatesTest {

	private static final double TOLERANCE = 0.001;

	@Test
	void strongholdRates_matchExpectedCountTargets() {
		LootTableConfig config = LootTableConfig.of(LootTier.TIER_3, "stronghold", 7, true);
		StructureClearRates.Rates rates = StructureClearRates.forTable(config);

		assertEquals(0.1071, rates.mending(), 0.001);
		assertEquals(0.4762, rates.primaryPerRoll(), 0.001);
		assertEquals(3, rates.primaryRollsPerChest());
		assertEquals(10.0, StructureClearRates.primaryExpectedPerClear(config), 0.05);
		assertEquals(0.75, StructureClearRates.mendingExpectedPerClear(config), 0.05);
	}

	@Test
	void ancientCityMendingTargetsOnePointFiveTypical() {
		LootTableConfig city = StructureTierRegistry.buildDefaults().get(
				net.minecraft.resources.Identifier.parse("minecraft:chests/ancient_city"));
		LootTableConfig iceBox = StructureTierRegistry.buildDefaults().get(
				net.minecraft.resources.Identifier.parse("minecraft:chests/ancient_city_ice_box"));

		assertEquals(0.75, StructureClearRates.mendingExpectedPerClear(city), 0.05);
		assertEquals(0.75, StructureClearRates.mendingExpectedPerClear(iceBox), 0.05);
		assertEquals(0.75 / 18.0, StructureClearRates.forTable(city).mending(), 0.002);
		assertEquals(0.1875, StructureClearRates.forTable(iceBox).mending(), 0.002);
		assertEquals(1.5,
				StructureClearRates.mendingExpectedPerClear(city) + StructureClearRates.mendingExpectedPerClear(iceBox),
				0.05);
	}

	@Test
	void netherFortressIsTier2WithReducedMending() {
		LootTableConfig fortress = StructureTierRegistry.buildDefaults().get(
				net.minecraft.resources.Identifier.parse("minecraft:chests/nether_bridge"));

		assertEquals(LootTier.TIER_2, fortress.tier());
		assertEquals(20.0 / 3.0, StructureClearRates.primaryExpectedPerClear(fortress), 0.05);
		assertEquals(0.5, StructureClearRates.mendingExpectedPerClear(fortress), 0.05);
		assertEquals(0.0625, StructureClearRates.forTable(fortress).mending(), 0.002);
	}

	@Test
	void woodlandMansionHasBoostedMending() {
		LootTableConfig mansion = StructureTierRegistry.buildDefaults().get(
				net.minecraft.resources.Identifier.parse("minecraft:chests/woodland_mansion"));

		assertEquals(1.25, StructureClearRates.mendingExpectedPerClear(mansion), 0.05);
		assertEquals(0.0833, StructureClearRates.forTable(mansion).mending(), 0.002);
	}

	@Test
	void trialSupplyIsTier2PrimaryOnly() {
		LootTableConfig supply = StructureTierRegistry.buildDefaults().get(
				net.minecraft.resources.Identifier.parse("minecraft:chests/trial_chambers/supply"));

		assertEquals(LootTier.TIER_2, supply.tier());
		assertEquals(20.0 / 3.0, StructureClearRates.primaryExpectedPerClear(supply), 0.05);
		assertEquals(0.0, StructureClearRates.mendingExpectedPerClear(supply), TOLERANCE);
	}

	@Test
	void lowNTier1StructuresUseExtraRollAndHitTarget() {
		LootTableConfig pillager = LootTableConfig.of(LootTier.TIER_1, "pillager_outpost", 1, false);
		LootTableConfig dungeon = LootTableConfig.of(LootTier.TIER_1, "dungeon", 2, false);
		StructureClearRates.Rates pillagerRates = StructureClearRates.forTable(pillager);

		assertEquals(3, pillagerRates.primaryRollsPerChest());
		assertEquals(2, StructureClearRates.forTable(dungeon).primaryRollsPerChest());
		assertEquals(0.8333, pillagerRates.primaryPerRoll(), 0.002);
		assertEquals(2.5, StructureClearRates.primaryExpectedPerClear(pillager), 0.05);
		assertEquals(2.5, StructureClearRates.primaryExpectedPerClear(dungeon), 0.05);
	}

	@Test
	void jungleTempleTier2HitsTargetDespiteLowN() {
		LootTableConfig jungle = LootTableConfig.of(LootTier.TIER_2, "jungle_temple", 2, false);
		StructureClearRates.Rates rates = StructureClearRates.forTable(jungle);

		assertEquals(4, rates.primaryRollsPerChest());
		assertEquals(0.8333, rates.primaryPerRoll(), 0.002);
		assertEquals(20.0 / 3.0, StructureClearRates.primaryExpectedPerClear(jungle), 0.05);
	}

	@Test
	void mineshaftTier1BaseRate() {
		LootTableConfig config = LootTableConfig.of(LootTier.TIER_1, "mineshaft", 6, false);
		StructureClearRates.Rates rates = StructureClearRates.forTable(config);

		assertEquals(0.0, rates.mending(), TOLERANCE);
		assertEquals(0.4167, rates.primaryPerRoll(), 0.002);
		assertEquals(1, rates.primaryRollsPerChest());
		assertEquals(2.5, StructureClearRates.primaryExpectedPerClear(config), 0.05);
	}

	@Test
	void buriedTreasureFortuneFocusedRate() {
		LootTableConfig config = LootTableConfig.buriedTreasure();
		StructureClearRates.Rates rates = StructureClearRates.forTable(config);

		assertEquals(0.0, rates.mending(), TOLERANCE);
		assertEquals(0.5, rates.primaryPerRoll(), 0.002);
		assertEquals(1, rates.primaryRollsPerChest());
	}

	@Test
	void structureClearMonteCarlo_expectedBookCounts() {
		int n = 7;
		int rollsPerChest = StructureClearRates.TIER3_PRIMARY_ROLLS_PER_CHEST;
		double pMending = StructureClearRates.perChestFromExpected(
				StructureClearRates.DEFAULT_MENDING_EXPECTED, n);
		double pPrimaryPerRoll = StructureClearRates.perChestFromExpected(
				StructureClearRates.TIER3_PRIMARY_EXPECTED, n * rollsPerChest);

		int trials = 50_000;
		long totalPrimary = 0;
		long totalMending = 0;

		java.util.Random random = new java.util.Random(42L);
		for (int t = 0; t < trials; t++) {
			int primaryCount = 0;
			int mendingCount = 0;
			for (int i = 0; i < n; i++) {
				if (random.nextDouble() < pMending) {
					mendingCount++;
				}
				for (int roll = 0; roll < rollsPerChest; roll++) {
					if (random.nextDouble() < pPrimaryPerRoll) {
						primaryCount++;
					}
				}
			}
			totalPrimary += primaryCount;
			totalMending += mendingCount;
		}

		assertEquals(StructureClearRates.TIER3_PRIMARY_EXPECTED,
				totalPrimary / (double) trials, 0.08);
		assertEquals(StructureClearRates.DEFAULT_MENDING_EXPECTED,
				totalMending / (double) trials, 0.08);
	}
}
