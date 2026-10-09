package structure_loot_rebalance.loot;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import structure_loot_rebalance.StructureLootRebalance;
import structure_loot_rebalance.loot.CatalogLoot.CatalogEntry;

import java.util.List;

/**
 * Builds one weighted pool per catalog layer.
 * A group is one weighted outcome that drops every child. Child weights are not a pick.
 */
public final class CatalogLootPools {

	private CatalogLootPools() {
	}

	public static LootPool.Builder pool(List<CatalogEntry> entries) {
		LootPool.Builder pool = LootPool.lootPool();
		int added = 0;
		for (CatalogEntry entry : entries) {
			LootPoolEntryContainer.Builder<?> built = weightedEntry(entry);
			if (built == null) {
				continue;
			}
			pool.add(built);
			added++;
		}
		return added == 0 ? null : pool;
	}

	private static LootPoolEntryContainer.Builder<?> weightedEntry(CatalogEntry entry) {
		if (entry.weight() <= 0) {
			return null;
		}
		return switch (entry.kind()) {
			case EMPTY -> EmptyLootItem.emptyItem().setWeight(entry.weight());
			case ITEM -> {
				LootPoolSingletonContainer.Builder<?> item = itemEntry(entry);
				yield item == null ? null : item.setWeight(entry.weight());
			}
			case GROUP -> inline(andTable(entry.children()), entry.weight());
			case ROLL -> inline(orTable(entry.children()), entry.weight());
		};
	}

	private static LootPoolEntryContainer.Builder<?> inline(LootTable table, int weight) {
		if (table == null) {
			return null;
		}
		return NestedLootTable.inlineLootTable(table).setWeight(weight);
	}

	private static LootTable andTable(List<CatalogEntry> children) {
		LootTable.Builder table = LootTable.lootTable();
		int pools = 0;
		for (CatalogEntry child : children) {
			pools += appendGuaranteed(table, child);
		}
		return pools == 0 ? null : table.build();
	}

	private static int appendGuaranteed(LootTable.Builder table, CatalogEntry child) {
		return switch (child.kind()) {
			case ITEM -> {
				LootPoolSingletonContainer.Builder<?> item = itemEntry(child);
				if (item == null) {
					yield 0;
				}
				table.withPool(LootPool.lootPool().add(item.setWeight(1)));
				yield 1;
			}
			case GROUP -> {
				int added = 0;
				for (CatalogEntry nested : child.children()) {
					added += appendGuaranteed(table, nested);
				}
				yield added;
			}
			case ROLL -> {
				LootPool.Builder nested = pool(child.children());
				if (nested == null) {
					yield 0;
				}
				table.withPool(nested);
				yield 1;
			}
			case EMPTY -> 0;
		};
	}

	private static LootTable orTable(List<CatalogEntry> children) {
		LootPool.Builder nested = pool(children);
		if (nested == null) {
			return null;
		}
		return LootTable.lootTable().withPool(nested).build();
	}

	private static LootPoolSingletonContainer.Builder<?> itemEntry(CatalogEntry entry) {
		if ("minecraft:silk_touch".equals(entry.enchant())) {
			return null;
		}
		Identifier itemId = Identifier.parse(entry.item());
		if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
			StructureLootRebalance.LOGGER.warn("Skipping unknown catalog item {}", entry.item());
			return null;
		}
		Item item = BuiltInRegistries.ITEM.getValue(itemId);
		LootPoolSingletonContainer.Builder<?> builder = LootItem.lootTableItem(item);
		if (entry.countMin() != 1 || entry.countMax() != 1) {
			builder.apply(SetItemCountFunction.setCount(countProvider(entry.countMin(), entry.countMax())));
		}
		if (entry.potion() != null) {
			Holder<Potion> potion = potion(entry.potion());
			if (potion == null) {
				return null;
			}
			builder.apply(SetPotionFunction.setPotion(potion));
		}
		if (entry.enchant() != null && "minecraft:enchanted_book".equals(entry.item())) {
			builder.apply(() -> new FixedEnchantBookFunction(entry.enchant(), entry.enchantLevel()));
		}
		return builder;
	}

	private static Holder<Potion> potion(String potionId) {
		Identifier id = Identifier.parse(potionId);
		return BuiltInRegistries.POTION.get(id).orElseGet(() -> {
			StructureLootRebalance.LOGGER.warn("Skipping unknown catalog potion {}", potionId);
			return null;
		});
	}

	private static net.minecraft.world.level.storage.loot.providers.number.NumberProvider countProvider(int min, int max) {
		if (min == max) {
			return ConstantValue.exactly(min);
		}
		return UniformGenerator.between(min, max);
	}
}
