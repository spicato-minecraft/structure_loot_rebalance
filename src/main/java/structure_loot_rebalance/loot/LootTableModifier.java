package structure_loot_rebalance.loot;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import structure_loot_rebalance.config.LootTableConfig;
import structure_loot_rebalance.config.StructureClearRates;

import java.util.Map;
import java.util.Set;

/**
 * Injects catalog layers onto structure chests and strips vanilla books from those tables.
 * Silk Touch removal and the enchanted-gear rate limiter stay on the same table set.
 */
public final class LootTableModifier {

	private static final int WEIGHT_SCALE = 10_000;

	private final CatalogLoot catalog;

	public LootTableModifier(CatalogLoot catalog) {
		this.catalog = catalog;
	}

	public void init() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (!source.isBuiltin()) {
				return;
			}
			Identifier tableId = key.identifier();
			if (!catalog.modifies(tableId)) {
				return;
			}
			addCatalogPools(catalog, tableId, tableBuilder::withPool);
		});

		Set<Identifier> stripVanillaEnchantBooks = catalog.modifiedTables();
		LootTableEvents.MODIFY_DROPS.register((holder, context, drops) -> {
			Identifier tableId = holder.unwrapKey()
					.map(ResourceKey::identifier)
					.orElse(null);
			if (tableId == null) {
				return;
			}
			if (stripVanillaEnchantBooks.contains(tableId)) {
				drops.removeIf(stack -> stack.is(Items.ENCHANTED_BOOK) && !ModEnchantedBooks.isModBook(stack));
				SilkTouchLootFilter.stripFromDrops(context, drops);
				VanillaEnchantedGearRateLimiter.removeHalf(context, drops);
			}
		});
	}

	static int addCatalogPools(
			CatalogLoot catalog,
			Identifier tableId,
			java.util.function.Consumer<LootPool.Builder> addPool
	) {
		int added = 0;
		for (CatalogLoot.CatalogLayer layer : catalog.layers(tableId)) {
			for (int copy = 0; copy < layer.copies(); copy++) {
				LootPool.Builder pool = CatalogLootPools.pool(layer.entries());
				if (pool == null) {
					continue;
				}
				addPool.accept(pool);
				added++;
			}
		}
		return added;
	}

	static void addModBookPools(LootTableConfig config, java.util.function.Consumer<LootPool.Builder> addPool) {
		StructureClearRates.Rates rates = StructureClearRates.forTable(config);
		if (rates.primaryPerRoll() > 0.0) {
			for (int roll = 0; roll < rates.primaryRollsPerChest(); roll++) {
				addPool.accept(buildPrimaryPool(config, rates.primaryPerRoll()));
			}
		}
		if (rates.mending() > 0.0) {
			addPool.accept(buildSingleEnchantPool("minecraft:mending", rates.mending()));
		}
	}

	private static LootPool.Builder buildPrimaryPool(LootTableConfig config, double perChestRate) {
		return buildWeightedEnchantPool(config.primaryWeights(), perChestRate, true);
	}

	private static LootPool.Builder buildSingleEnchantPool(String enchantmentId, double perChestRate) {
		int[] weights = bookAndEmptyWeights(perChestRate);
		LootPoolEntryContainer.Builder<?> bookEntry = LootItem.lootTableItem(Items.ENCHANTED_BOOK)
				.apply(fixedEnchantBook(enchantmentId))
				.setWeight(weights[0]);
		return LootPool.lootPool()
				.add(bookEntry)
				.add(EmptyLootItem.emptyItem().setWeight(weights[1]));
	}

	private static LootPool.Builder buildWeightedEnchantPool(
			Map<String, Integer> weights,
			double perChestRate,
			boolean excludeSilkTouch
	) {
		int[] poolWeights = bookAndEmptyWeights(perChestRate);
		int bookWeightTotal = poolWeights[0];
		int emptyWeight = poolWeights[1];
		int weightSum = weights.entrySet().stream()
				.filter(entry -> !excludeSilkTouch || !"minecraft:silk_touch".equals(entry.getKey()))
				.mapToInt(entry -> entry.getValue())
				.sum();
		if (weightSum <= 0) {
			throw new IllegalStateException("weights must include at least one eligible enchant");
		}

		LootPool.Builder pool = LootPool.lootPool();
		for (var entry : weights.entrySet()) {
			String enchantmentId = entry.getKey();
			if (excludeSilkTouch && "minecraft:silk_touch".equals(enchantmentId)) {
				continue;
			}
			int entryWeight = Math.max(1, (int) Math.round(bookWeightTotal * (entry.getValue() / (double) weightSum)));
			pool.add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
					.apply(fixedEnchantBook(enchantmentId))
					.setWeight(entryWeight));
		}
		pool.add(EmptyLootItem.emptyItem().setWeight(emptyWeight));
		return pool;
	}

	private static net.minecraft.world.level.storage.loot.functions.LootItemFunction.Builder fixedEnchantBook(
			String enchantmentId
	) {
		return () -> new FixedEnchantBookFunction(enchantmentId);
	}

	private static int[] bookAndEmptyWeights(double perChestRate) {
		double clamped = Math.min(0.9999, Math.max(0.0, perChestRate));
		int book = Math.max(1, (int) Math.round(WEIGHT_SCALE * clamped));
		int empty = Math.max(1, (int) Math.round(WEIGHT_SCALE * (1.0 - clamped)));
		return new int[]{book, empty};
	}

	public Set<Identifier> modifiedTableIds() {
		return catalog.modifiedTables();
	}

	public int modifiedTableCount() {
		return catalog.modifiedTables().size();
	}
}
