package structure_loot_rebalance.config;

import net.minecraft.resources.Identifier;

import java.util.Map;

/**
 * Default loot table configs for config v3 (generated from {@link StructureTierRegistry}).
 */
public final class DefaultLootModifications {

	private DefaultLootModifications() {
	}

	public static Map<Identifier, LootTableConfig> getDefaults() {
		return StructureTierRegistry.buildDefaults();
	}
}
