package structure_loot_rebalance.config;

/**
 * Stacked tool-enchant tier for structure loot tables.
 */
public enum LootTier {
	TIER_1(1),
	TIER_1_RARE(1),
	TIER_2(2),
	TIER_3(3);

	private final int level;

	LootTier(int level) {
		this.level = level;
	}

	public int level() {
		return level;
	}

	public boolean allowsMending() {
		return true;
	}

	public int primaryRollsPerChest() {
		return StructureClearRates.primaryRollsPerChest(level);
	}
}
