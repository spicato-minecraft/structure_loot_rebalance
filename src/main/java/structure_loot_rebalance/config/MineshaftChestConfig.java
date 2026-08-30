package structure_loot_rebalance.config;

/**
 * Tunes abandoned mineshaft chest minecart spawn rolls during corridor generation.
 * Vanilla uses a 1-in-100 roll at two positions per corridor section (~1–2 chests per mineshaft).
 */
public final class MineshaftChestConfig {

	public static final int VANILLA_ROLL_BOUND = 100;

	/**
	 * Default 20 → ~5% per roll (~5× vanilla), targeting ~5–6 chests per typical mineshaft clear
	 * to match {@link StructureTierRegistry} {@code structure_loot_events: 6}.
	 */
	public static final int DEFAULT_ROLL_BOUND = 20;

	private static volatile int rollBound = DEFAULT_ROLL_BOUND;

	private MineshaftChestConfig() {
	}

	public static int rollBound() {
		return rollBound;
	}

	static void setRollBound(int value) {
		if (value < 1 || value > VANILLA_ROLL_BOUND) {
			throw new IllegalArgumentException("mineshaft_chest_roll_bound must be 1–100");
		}
		rollBound = value;
	}

	static void reset() {
		rollBound = DEFAULT_ROLL_BOUND;
	}
}
