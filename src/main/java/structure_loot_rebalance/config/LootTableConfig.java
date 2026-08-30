package structure_loot_rebalance.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Config entry for a single loot table.
 * <p>
 * Primary pool = {@link #BASE_WEIGHTS} + optional {@link LootTheme} at {@code theme_weight_multiplier}.
 * Tier 2: two primary rolls per chest at 4/3× tier-1 per-roll odds.
 * Tier 3: three primary rolls per chest at 4/3× tier-1 per-roll odds.
 * Mending: optional independent per-chest roll ({@code mending_expected} books / structure clear).
 */
public record LootTableConfig(
		LootTier tier,
		String structureId,
		int structureLootEvents,
		boolean mendingEnabled,
		Map<String, Integer> baseWeights,
		double rateMultiplier,
		LootTheme theme,
		double themeWeightMultiplier,
		Double mendingExpectedPerClear
) {
	public static final Map<String, Integer> BASE_WEIGHTS = Map.of(
			"minecraft:efficiency", 4,
			"minecraft:unbreaking", 3,
			"minecraft:protection", 3
	);

	public static final Map<String, Integer> FORTUNE_ONLY_WEIGHTS = Map.of("minecraft:fortune", 1);

	public static final double DEFAULT_THEME_WEIGHT_MULTIPLIER = 2.0;

	public LootTableConfig {
		if (structureId == null || structureId.isBlank()) {
			throw new IllegalArgumentException("structureId is required");
		}
		if (structureLootEvents < 1) {
			throw new IllegalArgumentException("structureLootEvents must be >= 1");
		}
		if (rateMultiplier <= 0.0) {
			throw new IllegalArgumentException("rateMultiplier must be > 0");
		}
		if (themeWeightMultiplier <= 0.0) {
			throw new IllegalArgumentException("themeWeightMultiplier must be > 0");
		}
		if (baseWeights == null || baseWeights.isEmpty()) {
			baseWeights = BASE_WEIGHTS;
		} else {
			baseWeights = Map.copyOf(baseWeights);
		}
		if (mendingEnabled && mendingExpectedPerClear != null && mendingExpectedPerClear <= 0.0) {
			throw new IllegalArgumentException("mendingExpectedPerClear must be > 0 when set");
		}
	}

	public static LootTableConfig of(
			LootTier tier,
			String structureId,
			int structureLootEvents,
			boolean mendingEnabled
	) {
		return of(tier, structureId, structureLootEvents, mendingEnabled, null);
	}

	public static LootTableConfig of(
			LootTier tier,
			String structureId,
			int structureLootEvents,
			boolean mendingEnabled,
			Double mendingExpectedPerClear
	) {
		return new LootTableConfig(
				tier, structureId, structureLootEvents, mendingEnabled,
				BASE_WEIGHTS, 1.0, null, DEFAULT_THEME_WEIGHT_MULTIPLIER, mendingExpectedPerClear
		);
	}

	public static LootTableConfig ofRare(
			LootTier tier,
			String structureId,
			int structureLootEvents,
			double rateMultiplier
	) {
		return new LootTableConfig(
				tier, structureId, structureLootEvents, false,
				BASE_WEIGHTS, rateMultiplier, null, DEFAULT_THEME_WEIGHT_MULTIPLIER, null
		);
	}

	public static LootTableConfig withTheme(LootTableConfig base, LootTheme theme) {
		return withTheme(base, theme, DEFAULT_THEME_WEIGHT_MULTIPLIER);
	}

	public static LootTableConfig withTheme(LootTableConfig base, LootTheme theme, double themeWeightMultiplier) {
		return new LootTableConfig(
				base.tier(),
				base.structureId(),
				base.structureLootEvents(),
				base.mendingEnabled(),
				base.baseWeights(),
				base.rateMultiplier(),
				theme,
				themeWeightMultiplier,
				base.mendingExpectedPerClear()
		);
	}

	public static LootTableConfig withMendingExpected(LootTableConfig base, double mendingExpectedPerClear) {
		return new LootTableConfig(
				base.tier(),
				base.structureId(),
				base.structureLootEvents(),
				true,
				base.baseWeights(),
				base.rateMultiplier(),
				base.theme(),
				base.themeWeightMultiplier(),
				mendingExpectedPerClear
		);
	}

	public double resolvedMendingExpected() {
		if (!mendingEnabled) {
			return 0.0;
		}
		return mendingExpectedPerClear != null
				? mendingExpectedPerClear
				: StructureClearRates.DEFAULT_MENDING_EXPECTED;
	}

	/** Buried treasure: ~50% chance of a Fortune I book per find. */
	public static LootTableConfig buriedTreasure() {
		return new LootTableConfig(
				LootTier.TIER_1,
				"buried_treasure",
				1,
				false,
				FORTUNE_ONLY_WEIGHTS,
				StructureClearRates.buriedTreasureRateMultiplier(),
				LootTheme.FORTUNE,
				DEFAULT_THEME_WEIGHT_MULTIPLIER,
				null
		);
	}

	public Map<String, Integer> primaryWeights() {
		Map<String, Integer> weights = new LinkedHashMap<>(baseWeights());
		if (theme == null) {
			return Map.copyOf(weights);
		}
		for (var entry : theme.unitWeights().entrySet()) {
			int themed = Math.max(1, (int) Math.round(entry.getValue() * themeWeightMultiplier));
			weights.merge(entry.getKey(), themed, Integer::sum);
		}
		return Map.copyOf(weights);
	}
}
