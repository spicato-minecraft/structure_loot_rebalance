package structure_loot_rebalance.config;

import net.minecraft.resources.Identifier;
import structure_loot_rebalance.StructureLootRebalance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads and merges loot modifications config v4. Migrates legacy configs to defaults.
 */
public final class LootModificationsLoader {

	private LootModificationsLoader() {
	}

	public static LootModificationsConfig load(Path configDir, String modId) throws IOException {
		Path configPath = configDir.resolve(modId).resolve("loot-modifications.json");
		Files.createDirectories(configPath.getParent());

		Map<Identifier, LootTableConfig> defaults = DefaultLootModifications.getDefaults();

		if (!Files.exists(configPath)) {
			return writeAndReturnDefaults(configPath, defaults);
		}

		String json = Files.readString(configPath);
		if (isLegacyConfig(json)) {
			StructureLootRebalance.LOGGER.warn(
					"Replacing legacy loot-modifications.json with config v{} defaults",
					StructureClearRates.SCHEMA_VERSION
			);
			return writeAndReturnDefaults(configPath, defaults);
		}
		LootModificationsConfig userConfig;
		try {
			userConfig = LootModificationsConfig.load(json);
		} catch (Exception e) {
			StructureLootRebalance.LOGGER.warn("Failed to parse loot modifications config, using defaults", e);
			return new LootModificationsConfig(defaults);
		}

		Map<Identifier, LootTableConfig> merged = new LinkedHashMap<>(defaults);
		merged.putAll(userConfig.lootModifications());
		return new LootModificationsConfig(merged);
	}

	private static LootModificationsConfig writeAndReturnDefaults(
			Path configPath,
			Map<Identifier, LootTableConfig> defaults
	) throws IOException {
		LootModificationsConfig defaultConfig = new LootModificationsConfig(defaults);
		Files.writeString(configPath, defaultConfig.toJson());
		StructureLootRebalance.LOGGER.info("Wrote default loot modifications config to {}", configPath);
		return defaultConfig;
	}

	private static boolean isLegacyConfig(String json) {
		if (!json.contains("\"loot_modifications\"")) {
			return true;
		}
		return !json.contains("\"schema_version\": " + StructureClearRates.SCHEMA_VERSION);
	}

	public static void writeDefault(Path configDir, String modId) throws IOException {
		Path configPath = configDir.resolve(modId).resolve("loot-modifications.json");
		Files.createDirectories(configPath.getParent());
		if (!Files.exists(configPath)) {
			LootModificationsConfig defaultConfig = new LootModificationsConfig(DefaultLootModifications.getDefaults());
			Files.writeString(configPath, defaultConfig.toJson());
			StructureLootRebalance.LOGGER.info("Wrote default loot modifications config to {}", configPath);
		}
	}
}
