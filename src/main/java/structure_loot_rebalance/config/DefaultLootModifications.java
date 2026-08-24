package structure_loot_rebalance.config;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralized default loot modifications. Structure loot table IDs and default
 * add/remove/modify values. Adding a new structure or entry is a single code change.
 */
public final class DefaultLootModifications {

    /** All structure chest loot table IDs to scaffold in the default config. */
    public static final List<Identifier> STRUCTURE_LOOT_TABLES = List.of(
            resourceLocation("minecraft:chests/ancient_city"),
            resourceLocation("minecraft:chests/ancient_city_ice_box"),
            resourceLocation("minecraft:chests/stronghold_corridor"),
            resourceLocation("minecraft:chests/stronghold_crossing"),
            resourceLocation("minecraft:chests/stronghold_library"),
            resourceLocation("minecraft:chests/bastion_treasure"),
            resourceLocation("minecraft:chests/bastion_other"),
            resourceLocation("minecraft:chests/bastion_bridge"),
            resourceLocation("minecraft:chests/bastion_hoglin_stable"),
            resourceLocation("minecraft:chests/end_city_treasure"),
            resourceLocation("minecraft:chests/buried_treasure"),
            resourceLocation("minecraft:chests/underwater_ruin_big"),
            resourceLocation("minecraft:chests/underwater_ruin_small"),
            resourceLocation("minecraft:chests/shipwreck_treasure"),
            resourceLocation("minecraft:chests/shipwreck_supply"),
            resourceLocation("minecraft:chests/shipwreck_map"),
            resourceLocation("minecraft:chests/woodland_mansion"),
            resourceLocation("minecraft:chests/desert_pyramid"),
            resourceLocation("minecraft:chests/jungle_temple"),
            resourceLocation("minecraft:chests/pillager_outpost"),
            resourceLocation("minecraft:chests/simple_dungeon")
    );

    /**
     * Builds the default loot modifications. First default set: mending book
     * additions from the original loot-injections.json plan.
     */
    public static Map<Identifier, LootTableModificationConfig> getDefaults() {
        Map<Identifier, LootTableModificationConfig> defaults = new LinkedHashMap<>();

        for (Identifier tableId : STRUCTURE_LOOT_TABLES) {
            List<AddEntryConfig> add = new ArrayList<>();
            List<RemoveEntryConfig> remove = List.of();
            List<ModifyEntryConfig> modify = List.of();

            addMendingBookIfTarget(add, tableId, "ancient_city_ice_box", 10.0);
            addMendingBookIfTarget(add, tableId, "stronghold_library", 10.0);
            addMendingBookIfTarget(add, tableId, "ancient_city", 2.0);
            addMendingBookIfTarget(add, tableId, "bastion_treasure", 2.0);
            addMendingBookIfTarget(add, tableId, "bastion_other", 2.0);
            addMendingBookIfTarget(add, tableId, "bastion_bridge", 2.0);
            addMendingBookIfTarget(add, tableId, "bastion_hoglin_stable", 2.0);
            addMendingBookIfTarget(add, tableId, "buried_treasure", 1.0);
            addMendingBookIfTarget(add, tableId, "underwater_ruin_big", 0.5);
            addMendingBookIfTarget(add, tableId, "underwater_ruin_small", 0.5);
            addMendingBookIfTarget(add, tableId, "shipwreck_treasure", 0.25);
            addMendingBookIfTarget(add, tableId, "shipwreck_supply", 0.25);
            addMendingBookIfTarget(add, tableId, "shipwreck_map", 0.25);

            defaults.put(tableId, new LootTableModificationConfig(add, remove, modify));
        }

        return defaults;
    }

    private static void addMendingBookIfTarget(
            List<AddEntryConfig> add,
            Identifier tableId,
            String pathSuffix,
            double weight
    ) {
        if (tableId.getPath().equals("chests/" + pathSuffix)) {
            add.add(AddEntryConfig.enchantedBook("minecraft:mending", 1, weight, 1, 1));
        }
    }

    private static Identifier resourceLocation(String id) {
        return Identifier.parse(id);
    }

    /** Config DTO for a single loot table's add/remove/modify. */
    public record LootTableModificationConfig(
            List<AddEntryConfig> add,
            List<RemoveEntryConfig> remove,
            List<ModifyEntryConfig> modify
    ) {}

    /** Add entry: item (simple or enchanted book), weight, quantity range. */
    public record AddEntryConfig(
            ItemSpec item,
            double weight,
            int quantityMin,
            int quantityMax
    ) {
        public static AddEntryConfig simpleItem(String itemId, double weight, int quantityMin, int quantityMax) {
            return new AddEntryConfig(new SimpleItemSpec(itemId), weight, quantityMin, quantityMax);
        }

        public static AddEntryConfig enchantedBook(String enchantment, int level, double weight, int quantityMin, int quantityMax) {
            return new AddEntryConfig(new EnchantedBookSpec(enchantment, level), weight, quantityMin, quantityMax);
        }
    }

    public sealed interface ItemSpec permits SimpleItemSpec, EnchantedBookSpec {}

    /** Simple item by ID. */
    public record SimpleItemSpec(String id) implements ItemSpec {}

    /** Enchanted book item spec. */
    public record EnchantedBookSpec(String enchantment, int level) implements ItemSpec {}

    /** Remove entry: item + optional matching criteria. */
    public record RemoveEntryConfig(
            String item,
            String enchantment,
            Object enchantmentLevel,
            Integer weight
    ) {}

    /** Modify entry: exclude_enchantments on matching item. */
    public record ModifyEntryConfig(String item, List<String> excludeEnchantments) {}
}
