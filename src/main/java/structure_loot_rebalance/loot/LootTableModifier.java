package structure_loot_rebalance.loot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import structure_loot_rebalance.StructureLootRebalance;
import structure_loot_rebalance.config.DefaultLootModifications.AddEntryConfig;
import structure_loot_rebalance.config.DefaultLootModifications.EnchantedBookSpec;
import structure_loot_rebalance.config.DefaultLootModifications.ItemSpec;
import structure_loot_rebalance.config.DefaultLootModifications.LootTableModificationConfig;
import structure_loot_rebalance.config.DefaultLootModifications.ModifyEntryConfig;
import structure_loot_rebalance.config.DefaultLootModifications.RemoveEntryConfig;
import structure_loot_rebalance.config.DefaultLootModifications.SimpleItemSpec;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Modifies loot tables based on config. Adds items to target loot tables with
 * configurable weights and quantities. Supports simple items and enchanted books.
 */
public final class LootTableModifier {

    private final Map<Identifier, LootTableModificationConfig> modifications;

    public LootTableModifier(Map<Identifier, LootTableModificationConfig> modifications) {
        this.modifications = modifications;
    }

    public void init() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) {
                return;
            }
            Identifier tableId = key.identifier();
            LootTableModificationConfig config = modifications.get(tableId);
            if (config == null) {
                return;
            }
            for (AddEntryConfig entry : config.add()) {
                LootPool.Builder poolBuilder = buildPool(entry, registries);
                if (poolBuilder != null) {
                    tableBuilder.withPool(poolBuilder);
                }
            }
        });

        java.util.Set<Identifier> tablesWithModifyOrRemove = modifications.entrySet().stream()
                .filter(e -> !e.getValue().modify().isEmpty() || !e.getValue().remove().isEmpty())
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toSet());

        if (!tablesWithModifyOrRemove.isEmpty()) {
            LootTableEvents.MODIFY_DROPS.register((holder, context, drops) -> {
                Identifier tableId = holder.unwrapKey()
                        .map(ResourceKey::identifier)
                        .orElse(null);
                if (tableId == null) return;

                LootTableModificationConfig config = modifications.get(tableId);
                if (config == null) return;

                var registryAccess = context.getLevel() != null ? context.getLevel().registryAccess() : null;
                if (registryAccess == null) return;

                for (ModifyEntryConfig modify : config.modify()) {
                    if (modify.excludeEnchantments().isEmpty()) continue;
                    Set<Identifier> excludeIds = modify.excludeEnchantments().stream()
                            .map(Identifier::tryParse)
                            .filter(id -> id != null)
                            .collect(Collectors.toSet());
                    drops.removeIf(stack -> shouldExcludeEnchantments(stack, modify.item(), excludeIds, registryAccess));
                }
                for (RemoveEntryConfig remove : config.remove()) {
                    drops.removeIf(stack -> matchesRemoveCriteria(stack, remove, registryAccess));
                }
            });
        }
    }

    private static boolean shouldExcludeEnchantments(ItemStack stack, String targetItem, Set<Identifier> excludeEnchantments,
                                                     net.minecraft.core.RegistryAccess registryAccess) {
        if (stack.isEmpty()) return false;
        if (!targetItem.equals(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
                && !("minecraft:enchanted_book".equals(targetItem) && stack.is(Items.ENCHANTED_BOOK))) {
            return false;
        }
        var enchantments = stack.getEnchantments();
        for (var e : enchantments.entrySet()) {
            Identifier enchId = e.getKey().unwrapKey().map(ResourceKey::identifier).orElse(null);
            if (enchId != null && excludeEnchantments.contains(enchId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesRemoveCriteria(ItemStack stack, RemoveEntryConfig remove,
                                                 net.minecraft.core.RegistryAccess registryAccess) {
        if (stack.isEmpty()) return false;
        String stackItemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (!remove.item().equals(stackItemId) && !("minecraft:enchanted_book".equals(remove.item()) && stack.is(Items.ENCHANTED_BOOK))) {
            return false;
        }
        if (remove.enchantment() != null) {
            var enchantments = stack.getEnchantments();
            Identifier targetEnch = Identifier.tryParse(remove.enchantment());
            if (targetEnch == null) return false;
            boolean hasEnchantment = false;
            for (var e : enchantments.entrySet()) {
                Identifier enchId = e.getKey().unwrapKey().map(ResourceKey::identifier).orElse(null);
                if (targetEnch.equals(enchId)) {
                    if (remove.enchantmentLevel() != null) {
                        int level = e.getIntValue();
                        if (remove.enchantmentLevel() instanceof Integer single) {
                            if (level != single) return false;
                        } else if (remove.enchantmentLevel() instanceof List<?> range && ((List<?>) range).size() >= 2) {
                            List<?> r = (List<?>) range;
                            int min = ((Number) r.get(0)).intValue();
                            int max = ((Number) r.get(1)).intValue();
                            if (level < min || level > max) return false;
                        }
                    }
                    hasEnchantment = true;
                    break;
                }
            }
            if (!hasEnchantment) return false;
        }
        return true;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(StructureLootRebalance.MOD_ID);

    private static LootPool.Builder buildPool(AddEntryConfig entry, HolderLookup.Provider registries) {
        int weight = Math.max(1, (int) Math.round(entry.weight()));
        ItemSpec item = entry.item();

        if (item instanceof EnchantedBookSpec spec) {
            var enchantRegistry = registries.lookupOrThrow(Registries.ENCHANTMENT);
            var enchantId = Identifier.tryParse(spec.enchantment());
            if (enchantId == null) {
                LOGGER.warn("Invalid enchantment ID in add entry: {}", spec.enchantment());
                return null;
            }
            var holder = enchantRegistry.get(ResourceKey.create(Registries.ENCHANTMENT, enchantId));
            if (holder.isEmpty()) {
                LOGGER.warn("Unknown enchantment in add entry: {}", spec.enchantment());
                return null;
            }

            int mendingWeight = Math.min(100, Math.max(1, weight * 5));
            int emptyWeight = 100 - mendingWeight;

            LootPoolEntryContainer.Builder<?> itemEntry = LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                    .apply(EnchantRandomlyFunction.randomEnchantment()
                            .withEnchantment(holder.get())
                            .allowingIncompatibleEnchantments())
                    .apply(SetItemCountFunction.setCount(buildCountProvider(entry)))
                    .setWeight(mendingWeight);

            return LootPool.lootPool()
                    .add(itemEntry)
                    .add(EmptyLootItem.emptyItem().setWeight(emptyWeight));
        }

        if (item instanceof SimpleItemSpec spec) {
            Identifier itemId = Identifier.tryParse(spec.id());
            if (itemId == null) {
                LOGGER.warn("Invalid item ID in add entry: {}", spec.id());
                return null;
            }

            var itemRegistry = registries.lookupOrThrow(Registries.ITEM);
            var itemHolder = itemRegistry.get(ResourceKey.create(Registries.ITEM, itemId));

            if (itemHolder.isPresent()) {
                LootPoolEntryContainer.Builder<?> entryBuilder = LootItem.lootTableItem(itemHolder.get().value())
                        .apply(SetItemCountFunction.setCount(buildCountProvider(entry)))
                        .setWeight(weight);
                return LootPool.lootPool().add(entryBuilder);
            }

            var lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, itemId);
            LootPoolEntryContainer.Builder<?> nested = NestedLootTable.lootTableReference(lootTableKey)
                    .apply(SetItemCountFunction.setCount(buildCountProvider(entry)))
                    .setWeight(weight);
            return LootPool.lootPool().add(nested);
        }

        return null;
    }

    private static NumberProvider buildCountProvider(AddEntryConfig entry) {
        if (entry.quantityMin() == entry.quantityMax()) {
            return ConstantValue.exactly(entry.quantityMin());
        }
        return UniformGenerator.between(entry.quantityMin(), entry.quantityMax());
    }
}
