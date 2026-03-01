package mending_rebalance.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import mending_rebalance.Mending_rebalance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Config-driven loot injection entries. Loaded from config/loot-injections.json.
 * Default config is cloned from vanilla mending rebalance plan.
 */
public record LootInjectionConfig(List<Entry> entries) {

    public record Entry(String item, Double weight, String loot_table) {
        public int weightAsInt() {
            return weight != null ? (int) Math.round(weight) : 1;
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static LootInjectionConfig load(Path configDir) {
        Path configPath = configDir.resolve("loot-injections.json");
        if (!Files.exists(configPath)) {
            Mending_rebalance.LOGGER.info("Loot injection config not found at {}, using defaults", configPath);
            return getDefault();
        }
        try {
            String json = Files.readString(configPath);
            LootInjectionConfig config = GSON.fromJson(json, LootInjectionConfig.class);
            if (config == null || config.entries == null) {
                return getDefault();
            }
            return config;
        } catch (IOException | JsonSyntaxException e) {
            Mending_rebalance.LOGGER.error("Failed to load loot injection config, using defaults", e);
            return getDefault();
        }
    }

    public static void writeDefault(Path configDir) throws IOException {
        Files.createDirectories(configDir);
        Path configPath = configDir.resolve("loot-injections.json");
        if (!Files.exists(configPath)) {
            Files.writeString(configPath, GSON.toJson(getDefault()));
            Mending_rebalance.LOGGER.info("Wrote default loot injection config to {}", configPath);
        }
    }

    private static LootInjectionConfig getDefault() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry("mending_rebalance:mending_book", 10.0, "minecraft:chests/ancient_city_ice_box"));
        entries.add(new Entry("mending_rebalance:mending_book", 10.0, "minecraft:chests/stronghold_library"));
        entries.add(new Entry("mending_rebalance:mending_book", 2.0, "minecraft:chests/ancient_city"));
        entries.add(new Entry("mending_rebalance:mending_book", 2.0, "minecraft:chests/bastion_treasure"));
        entries.add(new Entry("mending_rebalance:mending_book", 2.0, "minecraft:chests/bastion_other"));
        entries.add(new Entry("mending_rebalance:mending_book", 2.0, "minecraft:chests/bastion_bridge"));
        entries.add(new Entry("mending_rebalance:mending_book", 2.0, "minecraft:chests/bastion_hoglin_stable"));
        entries.add(new Entry("mending_rebalance:mending_book", 1.0, "minecraft:chests/buried_treasure"));
        entries.add(new Entry("mending_rebalance:mending_book", 0.5, "minecraft:chests/underwater_ruin_big"));
        entries.add(new Entry("mending_rebalance:mending_book", 0.5, "minecraft:chests/underwater_ruin_small"));
        entries.add(new Entry("mending_rebalance:mending_book", 0.25, "minecraft:chests/shipwreck_treasure"));
        entries.add(new Entry("mending_rebalance:mending_book", 0.25, "minecraft:chests/shipwreck_supply"));
        entries.add(new Entry("mending_rebalance:mending_book", 0.25, "minecraft:chests/shipwreck_map"));
        return new LootInjectionConfig(entries);
    }
}
