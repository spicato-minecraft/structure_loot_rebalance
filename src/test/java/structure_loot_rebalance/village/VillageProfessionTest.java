package structure_loot_rebalance.village;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import structure_loot_rebalance.MinecraftTestBootstrap;
import structure_loot_rebalance.StructureLootRebalance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VillageProfessionTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void mapsEachJobSiteHouseToAProfession() {
		assertEquals(VillageProfession.CLERIC, VillageProfession.fromTemplate(id("village/plains/houses/plains_temple_3")));
		assertEquals(VillageProfession.CLERIC, VillageProfession.fromTemplate(id("village/desert/houses/desert_temple_2")));
		assertEquals(VillageProfession.LIBRARIAN, VillageProfession.fromTemplate(id("village/plains/houses/plains_library_1")));
		assertEquals(VillageProfession.ARMORER, VillageProfession.fromTemplate(id("village/snowy/houses/snowy_armorer_house_1")));
		assertEquals(VillageProfession.BUTCHER, VillageProfession.fromTemplate(id("village/plains/houses/plains_butcher_shop_1")));
		assertEquals(VillageProfession.CARTOGRAPHER, VillageProfession.fromTemplate(id("village/desert/houses/desert_cartographer_house_1")));
		assertEquals(VillageProfession.FISHERMAN, VillageProfession.fromTemplate(id("village/taiga/houses/taiga_fisher_cottage_1")));
		assertEquals(VillageProfession.FLETCHER, VillageProfession.fromTemplate(id("village/plains/houses/plains_fletcher_house_1")));
		assertEquals(VillageProfession.LEATHERWORKER, VillageProfession.fromTemplate(id("village/plains/houses/plains_tannery_1")));
		assertEquals(VillageProfession.MASON, VillageProfession.fromTemplate(id("village/snowy/houses/snowy_masons_house_1")));
		assertEquals(VillageProfession.SHEPHERD, VillageProfession.fromTemplate(id("village/plains/houses/plains_shepherds_house_1")));
		assertEquals(VillageProfession.TOOLSMITH, VillageProfession.fromTemplate(id("village/plains/houses/plains_tool_smith_1")));
		assertEquals(VillageProfession.WEAPONSMITH, VillageProfession.fromTemplate(id("village/plains/houses/plains_weaponsmith_1")));
		assertEquals(VillageProfession.FARMER, VillageProfession.fromTemplate(id("village/plains/houses/plains_small_farm_1")));
		assertEquals(VillageProfession.FARMER, VillageProfession.fromTemplate(id("village/savanna/houses/savanna_large_farm_2")));
	}

	@Test
	void includesZombieVillageProfessionHouses() {
		assertEquals(
				VillageProfession.LIBRARIAN,
				VillageProfession.fromTemplate(id("village/taiga/zombie/houses/taiga_library_1"))
		);
	}

	@Test
	void ignoresGenericHousesAndStreets() {
		assertNull(VillageProfession.fromTemplate(id("village/plains/houses/plains_small_house_1")));
		assertNull(VillageProfession.fromTemplate(id("village/plains/streets/corner_01")));
		assertNull(VillageProfession.fromTemplate(id("village/plains/town_centers/plains_fountain_01")));
		assertFalse(VillageProfession.isProfessionHouse(id("village/plains/houses/plains_medium_house_1")));
	}

	@Test
	void vanillaProfessionsUseVanillaLootTables() {
		assertEquals(BuiltInLootTables.VILLAGE_TEMPLE, VillageProfession.CLERIC.lootTable());
		assertEquals(BuiltInLootTables.VILLAGE_ARMORER, VillageProfession.ARMORER.lootTable());
		assertEquals(BuiltInLootTables.VILLAGE_WEAPONSMITH, VillageProfession.WEAPONSMITH.lootTable());
	}

	@Test
	void libraryAndFarmUseModLootTables() {
		assertEquals(
				StructureLootRebalance.MOD_ID + ":chests/village/library",
				VillageProfession.LIBRARIAN.lootTable().identifier().toString()
		);
		assertEquals(
				StructureLootRebalance.MOD_ID + ":chests/village/farm",
				VillageProfession.FARMER.lootTable().identifier().toString()
		);
		assertTrue(VillageProfession.isProfessionHouse(id("village/plains/houses/plains_library_1")));
		assertNotNull(VillageProfession.FARMER.lootTable());
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("minecraft", path);
	}
}
