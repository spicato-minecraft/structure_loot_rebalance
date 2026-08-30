package structure_loot_rebalance;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import structure_loot_rebalance.config.LootModificationsConfig;
import structure_loot_rebalance.config.LootModificationsLoader;
import structure_loot_rebalance.config.MineshaftChestConfig;
import structure_loot_rebalance.config.WorldgenConfigLoader;
import structure_loot_rebalance.loot.FixedEnchantBookFunction;
import structure_loot_rebalance.loot.LootTableModifier;

import java.io.IOException;
import java.nio.file.Path;

public class StructureLootRebalance implements ModInitializer {
	public static final String MOD_ID = "structure_loot_rebalance";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Path configDir = FabricLoader.getInstance().getConfigDir();
		try {
			WorldgenConfigLoader.load();
			FixedEnchantBookFunction.register();
			LootModificationsLoader.writeDefault(configDir, MOD_ID);
			LootModificationsConfig config = LootModificationsLoader.load(configDir, MOD_ID);
			LootTableModifier modifier = new LootTableModifier(config.lootModifications());
			modifier.init();

			StructureLootRebalance.LOGGER.info(
					"Registered {} structure loot tables with multi-pool enchant book rolls (mineshaft chest roll bound: {})",
					config.lootModifications().size(),
					MineshaftChestConfig.rollBound()
			);
		} catch (IOException e) {
			StructureLootRebalance.LOGGER.error("Failed to load loot modifications config", e);
		}
	}
}
