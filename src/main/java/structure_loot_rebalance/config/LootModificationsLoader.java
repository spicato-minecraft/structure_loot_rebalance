package structure_loot_rebalance.config;

import structure_loot_rebalance.StructureLootRebalance;
import structure_loot_rebalance.config.DefaultLootModifications.LootTableModificationConfig;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads and merges loot modifications config. Writes default config if missing.
 * User config overrides defaults for each table.
 */
public final class LootModificationsLoader {

    public static LootModificationsConfig load(Path configDir, String modId) throws IOException {
        Path configPath = configDir.resolve(modId).resolve("loot-modifications.json");
        Files.createDirectories(configPath.getParent());

        Map<ResourceLocation, LootTableModificationConfig> defaults = DefaultLootModifications.getDefaults();

        if (!Files.exists(configPath)) {
            LootModificationsConfig defaultConfig = new LootModificationsConfig(defaults);
            Files.writeString(configPath, defaultConfig.toJson());
            StructureLootRebalance.LOGGER.info("Wrote default loot modifications config to {}", configPath);
            return defaultConfig;
        }

        String json = Files.readString(configPath);
        LootModificationsConfig userConfig;
        try {
            userConfig = LootModificationsConfig.load(json);
        } catch (Exception e) {
            StructureLootRebalance.LOGGER.warn("Failed to parse loot modifications config, using defaults", e);
            return new LootModificationsConfig(defaults);
        }

        // Merge: start with defaults, overlay user config (user overrides per table)
        Map<ResourceLocation, LootTableModificationConfig> merged = new LinkedHashMap<>(defaults);
        for (Map.Entry<ResourceLocation, LootTableModificationConfig> entry : userConfig.lootModifications().entrySet()) {
            merged.put(entry.getKey(), entry.getValue());
        }
        return new LootModificationsConfig(merged);
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
