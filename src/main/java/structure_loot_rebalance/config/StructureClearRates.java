package structure_loot_rebalance.config;

/**
 * Derives per-chest branch rates from structure-clear expectations and loot event count n.
 */
public final class StructureClearRates {

	public static final int SCHEMA_VERSION = 7;

	public static final double TIER1_PRIMARY_EXPECTED = 2.5;
	public static final int TIER1_PRIMARY_ROLLS_PER_CHEST = 1;

	public static final int TIER2_PRIMARY_ROLLS_PER_CHEST = 2;
	public static final int TIER3_PRIMARY_ROLLS_PER_CHEST = 3;
	public static final double TIER2_TIER3_PRIMARY_PER_ROLL_MULTIPLIER = 4.0 / 3.0;

	public static final double TIER2_PRIMARY_EXPECTED =
			TIER1_PRIMARY_EXPECTED * TIER2_PRIMARY_ROLLS_PER_CHEST * TIER2_TIER3_PRIMARY_PER_ROLL_MULTIPLIER;
	public static final double TIER3_PRIMARY_EXPECTED = 10.0;

	public static final double BURIED_TREASURE_PRIMARY_EXPECTED = 0.5;

	public static final double DEFAULT_MENDING_EXPECTED = 0.75;
	public static final double ANCIENT_CITY_MENDING_EXPECTED = 0.75;
	public static final double ANCIENT_CITY_ICE_BOX_MENDING_EXPECTED = 0.75;
	public static final double NETHER_FORTRESS_MENDING_EXPECTED = 0.5;
	public static final double WOODLAND_MANSION_MENDING_EXPECTED = 1.25;

	private StructureClearRates() {
	}

	public record Rates(double mending, double primaryPerRoll, int primaryRollsPerChest) {
	}

	public static Rates forTable(LootTableConfig config) {
		int tier = config.tier().level();
		int n = Math.max(1, config.structureLootEvents());
		double multiplier = config.rateMultiplier();
		int primaryRolls = primaryRollsPerChest(config);

		double primaryPerRoll = tier >= 1
				? perChestFromExpected(
						primaryExpectedForTier(tier) * multiplier,
						n * primaryRolls)
				: 0.0;
		double mending = config.mendingEnabled()
				? perChestFromExpected(config.resolvedMendingExpected() * multiplier, n)
				: 0.0;

		return new Rates(mending, primaryPerRoll, primaryRolls);
	}

	public static double primaryExpectedForTier(int tier) {
		return switch (tier) {
			case 2 -> TIER2_PRIMARY_EXPECTED;
			case 3 -> TIER3_PRIMARY_EXPECTED;
			default -> TIER1_PRIMARY_EXPECTED;
		};
	}

	public static int primaryRollsPerChest(int tier) {
		return switch (tier) {
			case 2 -> TIER2_PRIMARY_ROLLS_PER_CHEST;
			case 3 -> TIER3_PRIMARY_ROLLS_PER_CHEST;
			default -> TIER1_PRIMARY_ROLLS_PER_CHEST;
		};
	}

	public static int primaryRollsPerChest(LootTableConfig config) {
		if (LootTableConfig.FORTUNE_ONLY_WEIGHTS.equals(config.baseWeights())) {
			return TIER1_PRIMARY_ROLLS_PER_CHEST;
		}
		int tier = config.tier().level();
		int n = Math.max(1, config.structureLootEvents());
		int rolls = primaryRollsPerChest(tier);
		double expected = primaryExpectedForTier(tier) * config.rateMultiplier();
		while (expected / (n * rolls) > 0.9999) {
			rolls++;
		}
		return rolls;
	}

	public static double primaryPerRollTierMultiplier(int tier) {
		return tier >= 2 ? TIER2_TIER3_PRIMARY_PER_ROLL_MULTIPLIER : 1.0;
	}

	public static double primaryExpectedPerClear(LootTableConfig config) {
		Rates rates = forTable(config);
		int n = Math.max(1, config.structureLootEvents());
		return n * rates.primaryRollsPerChest() * rates.primaryPerRoll();
	}

	public static double mendingExpectedPerClear(LootTableConfig config) {
		if (!config.mendingEnabled()) {
			return 0.0;
		}
		Rates rates = forTable(config);
		int n = Math.max(1, config.structureLootEvents());
		return n * rates.mending();
	}

	public static double perChestFromExpected(double expectedPerClear, int lootEvents) {
		if (lootEvents <= 0) {
			throw new IllegalArgumentException("lootEvents must be positive");
		}
		return Math.min(0.9999, Math.max(0.0, expectedPerClear / lootEvents));
	}

	public static double buriedTreasureRateMultiplier() {
		return BURIED_TREASURE_PRIMARY_EXPECTED / TIER1_PRIMARY_EXPECTED;
	}
}
