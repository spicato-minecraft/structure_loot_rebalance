package structure_loot_rebalance.loot;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import structure_loot_rebalance.MinecraftTestBootstrap;
import structure_loot_rebalance.loot.CatalogLoot.CatalogEntry;
import structure_loot_rebalance.loot.CatalogLoot.CatalogLayer;
import structure_loot_rebalance.loot.CatalogLoot.EntryKind;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogLootTest {

	@BeforeAll
	static void bootstrap() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void ancientCityKeepsIndependentMendingBesidePrimaryBooks() throws Exception {
		CatalogLoot catalog = CatalogLoot.loadBundled();
		List<CatalogLayer> layers = catalog.layers(Identifier.parse("minecraft:chests/ancient_city"));

		CatalogLayer primary = layers.stream()
				.filter(layer -> layer.rollId().endsWith(":primary"))
				.findFirst()
				.orElseThrow();
		CatalogLayer mending = layers.stream()
				.filter(layer -> layer.rollId().endsWith(":mending"))
				.findFirst()
				.orElseThrow();

		assertEquals(2, primary.copies());
		assertEquals(1, mending.copies());
		assertTrue(enchants(primary).contains("minecraft:swift_sneak"));
		assertFalse(enchants(primary).contains("minecraft:mending"));
		assertEquals(List.of("minecraft:mending"), enchants(mending));
		assertEquals(3, LootTableModifier.addCatalogPools(
				catalog,
				Identifier.parse("minecraft:chests/ancient_city"),
				pool -> {
				}
		));
	}

	@Test
	void netherFortressGroupDropsEveryChild() throws Exception {
		CatalogLoot catalog = CatalogLoot.loadBundled();
		CatalogLayer brew = catalog.layers(Identifier.parse("minecraft:chests/nether_bridge")).stream()
				.filter(layer -> layer.rollId().equals("roll_jxd8dky6"))
				.findFirst()
				.orElseThrow();
		List<CatalogEntry> groups = brew.entries().stream()
				.filter(entry -> entry.kind() == EntryKind.GROUP)
				.toList();

		assertEquals(2, groups.size());
		assertEquals(
				List.of("minecraft:potion", "minecraft:nether_wart", "minecraft:magma_cream"),
				groups.get(0).children().stream().map(CatalogEntry::item).toList()
		);
		assertEquals(4, groups.get(1).children().size());

		LootPool pool = CatalogLootPools.pool(brew.entries()).build();
		List<?> built = poolEntries(pool);
		assertEquals(3, built.size());
		assertEquals(
				"net.minecraft.world.level.storage.loot.entries.NestedLootTable",
				built.get(0).getClass().getName()
		);
		assertEquals(
				"net.minecraft.world.level.storage.loot.entries.NestedLootTable",
				built.get(2).getClass().getName()
		);
	}

	@Test
	void designerRollsStaySeparateFromVanillaAndDroppedBooks() throws Exception {
		CatalogLoot catalog = CatalogLoot.loadBundled();

		assertFalse(catalog.modifies(Identifier.parse("minecraft:chests/end_city_treasure")));
		assertFalse(catalog.modifies(Identifier.parse("minecraft:chests/trial_chambers/reward_ominous")));
		assertFalse(catalog.modifies(Identifier.parse("minecraft:chests/village/village_butcher")));
		assertFalse(catalog.modifies(Identifier.parse("minecraft:chests/shipwreck_supply")));

		List<CatalogLayer> mineshaft = catalog.layers(Identifier.parse("minecraft:chests/abandoned_mineshaft"));
		assertEquals(2, mineshaft.size());
		assertFalse(enchants(mineshaft.get(0)).contains("minecraft:protection"));
		assertTrue(mineshaft.get(1).entries().stream().anyMatch(entry -> "minecraft:night_vision".equals(entry.potion())));

		List<String> ruinEnchants = enchants(catalog.layers(Identifier.parse("minecraft:chests/underwater_ruin_big")).getFirst());
		assertEquals(List.of("minecraft:respiration", "minecraft:depth_strider"), ruinEnchants);

		CatalogLayer buried = catalog.layers(Identifier.parse("minecraft:chests/buried_treasure")).getFirst();
		assertEquals(1, buried.copies());
		assertEquals(5000, buried.entries().get(0).weight());
		assertEquals(5000, buried.entries().get(1).weight());

		assertEquals(2, catalog.layers(Identifier.parse("minecraft:chests/simple_dungeon")).getFirst().copies());
		assertEquals(1, catalog.layers(Identifier.parse("minecraft:chests/desert_pyramid")).getFirst().copies());
		assertTrue(catalog.layers(Identifier.parse("minecraft:chests/trial_chambers/supply")).stream()
				.noneMatch(layer -> layer.rollId().endsWith(":mending")));
		assertTrue(catalog.layers(Identifier.parse("minecraft:chests/trial_chambers/reward")).stream()
				.anyMatch(layer -> layer.rollId().endsWith(":mending")));

		boolean silkTouch = catalog.modifiedTables().stream()
				.flatMap(table -> catalog.layers(table).stream())
				.flatMap(layer -> layer.entries().stream())
				.anyMatch(entry -> "minecraft:silk_touch".equals(entry.enchant()));
		assertFalse(silkTouch);
	}

	private static List<String> enchants(CatalogLayer layer) {
		return layer.entries().stream()
				.map(CatalogEntry::enchant)
				.filter(enchant -> enchant != null)
				.toList();
	}

	@SuppressWarnings("unchecked")
	private static List<?> poolEntries(LootPool pool) throws Exception {
		Field field = LootPool.class.getDeclaredField("entries");
		field.setAccessible(true);
		return (List<?>) field.get(pool);
	}
}
