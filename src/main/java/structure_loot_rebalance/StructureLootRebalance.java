package structure_loot_rebalance;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import structure_loot_rebalance.config.LootModificationsLoader;
import structure_loot_rebalance.config.MineshaftChestConfig;
import structure_loot_rebalance.config.WorldgenConfigLoader;
import structure_loot_rebalance.loot.CatalogLoot;
import structure_loot_rebalance.loot.FixedEnchantBookFunction;
import structure_loot_rebalance.loot.LootTableModifier;
import structure_loot_rebalance.village.VillageProfessionChestProcessor;

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
			VillageProfessionChestProcessor.register();
			FixedEnchantBookFunction.register();
			LootModificationsLoader.writeDefault(configDir, MOD_ID);
			LootModificationsLoader.load(configDir, MOD_ID);
			CatalogLoot catalog = CatalogLoot.loadBundled();
			LootTableModifier modifier = new LootTableModifier(catalog);
			modifier.init();

			StructureLootRebalance.LOGGER.info(
					"Registered {} structure loot tables from the 26.1 loot catalog (mineshaft chest roll bound: {})",
					modifier.modifiedTableCount(),
					MineshaftChestConfig.rollBound()
			);
		} catch (IOException e) {
			StructureLootRebalance.LOGGER.error("Failed to load loot modifications config", e);
		}
	}
}
