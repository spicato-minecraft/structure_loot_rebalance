package mending_rebalance.loot;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Modifies loot tables based on config. Injects items (or loot table references) into
 * target loot tables with configurable weights.
 */
public final class LootTableModifier {

    private static final Map<ResourceLocation, List<LootInjectionEntry>> INJECTIONS = new ConcurrentHashMap<>();

    public record LootInjectionEntry(String item, int weight, ResourceLocation lootTable) {}

    public static void register(List<LootInjectionEntry> entries) {
        for (LootInjectionEntry entry : entries) {
            INJECTIONS.computeIfAbsent(entry.lootTable(), k -> new java.util.ArrayList<>()).add(entry);
        }
    }

    public static void clear() {
        INJECTIONS.clear();
    }

    public static void init() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) {
                return;
            }
            ResourceLocation tableId = key.location();
            java.util.List<LootInjectionEntry> injections = INJECTIONS.get(tableId);
            if (injections == null || injections.isEmpty()) {
                return;
            }
            for (LootInjectionEntry entry : injections) {
                LootPool.Builder poolBuilder = buildPool(entry, registries);
                if (poolBuilder != null) {
                    tableBuilder.withPool(poolBuilder);
                }
            }
        });
    }

    private static LootPool.Builder buildPool(LootInjectionEntry entry, HolderLookup.Provider registries) {
        ResourceLocation itemId = ResourceLocation.tryParse(entry.item());
        if (itemId == null) {
            return null;
        }
        int weight = Math.max(1, entry.weight());

        // Special case: mending_rebalance:mending_book — inject enchanted book directly.
        // Using NestedLootTable causes "Missing element" because mod loot tables may not be
        // loaded yet when vanilla tables are validated.
        // Weight controls probability: mending_weight vs empty_weight so we don't get mending 100% of the time.
        if ("mending_rebalance".equals(itemId.getNamespace()) && "mending_book".equals(itemId.getPath())) {
            var enchantRegistry = registries.lookupOrThrow(Registries.ENCHANTMENT);
            var mendingHolder = enchantRegistry.get(Enchantments.MENDING);
            if (mendingHolder.isPresent()) {
                // Scale: weight 10 -> ~50% chance, weight 2 -> ~10%, weight 1 -> ~5%
                int mendingWeight = Math.min(100, Math.max(1, weight * 5));
                int emptyWeight = 100 - mendingWeight;
                return LootPool.lootPool()
                        .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                                .apply(EnchantRandomlyFunction.randomEnchantment()
                                        .withEnchantment(mendingHolder.get())
                                        .allowingIncompatibleEnchantments())
                                .setWeight(mendingWeight))
                        .add(EmptyLootItem.emptyItem().setWeight(emptyWeight));
            }
        }

        LootPoolEntryContainer.Builder<?> entryBuilder = buildEntry(entry, registries);
        if (entryBuilder != null) {
            return LootPool.lootPool().add(entryBuilder);
        }
        return null;
    }

    private static LootPoolEntryContainer.Builder<?> buildEntry(LootInjectionEntry entry, HolderLookup.Provider registries) {
        ResourceLocation itemId = ResourceLocation.tryParse(entry.item());
        if (itemId == null) {
            return null;
        }
        int weight = Math.max(1, entry.weight());

        var itemRegistry = registries.lookupOrThrow(Registries.ITEM);
        var itemHolder = itemRegistry.get(ResourceKey.create(Registries.ITEM, itemId));

        if (itemHolder.isPresent()) {
            return LootItem.lootTableItem(itemHolder.get().value())
                    .setWeight(weight);
        }
        // Loot table registry is not available during MODIFY (we're loading loot tables).
        // Add reference directly; resolution happens at loot generation time.
        var lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, itemId);
        return NestedLootTable.lootTableReference(lootTableKey)
                .setWeight(weight);
    }
}
