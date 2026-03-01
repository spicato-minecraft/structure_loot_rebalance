package structure_loot_rebalance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import structure_loot_rebalance.config.LootModificationsConfig;
import structure_loot_rebalance.config.LootModificationsLoader;
import structure_loot_rebalance.loot.LootTableModifier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Path;

public class StructureLootRebalance implements ModInitializer {
	public static final String MOD_ID = "structure_loot_rebalance";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Path configDir = FabricLoader.getInstance().getConfigDir();
		try {
			LootModificationsLoader.writeDefault(configDir, MOD_ID);
			LootModificationsConfig config = LootModificationsLoader.load(configDir, MOD_ID);
			LootTableModifier modifier = new LootTableModifier(config.lootModifications());
			modifier.init();

			int addCount = config.lootModifications().values().stream()
					.mapToInt(t -> t.add().size())
					.sum();
			StructureLootRebalance.LOGGER.info("Registered {} loot table add entries across {} tables",
					addCount, config.lootModifications().size());
		} catch (IOException e) {
			StructureLootRebalance.LOGGER.error("Failed to load loot modifications config", e);
		}
	}
}
