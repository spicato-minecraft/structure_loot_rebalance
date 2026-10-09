package structure_loot_rebalance.village;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import structure_loot_rebalance.StructureLootRebalance;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Village job-site buildings and the chest loot table each one should use.
 */
public enum VillageProfession {
	ARMORER("armorer", BuiltInLootTables.VILLAGE_ARMORER, Blocks.BLAST_FURNACE),
	BUTCHER("butcher", BuiltInLootTables.VILLAGE_BUTCHER, Blocks.SMOKER),
	CARTOGRAPHER("cartograph", BuiltInLootTables.VILLAGE_CARTOGRAPHER, Blocks.CARTOGRAPHY_TABLE),
	CLERIC("temple", BuiltInLootTables.VILLAGE_TEMPLE, Blocks.BREWING_STAND),
	FARMER("farm", farmLoot(), Blocks.COMPOSTER),
	FISHERMAN("fisher", BuiltInLootTables.VILLAGE_FISHER, Blocks.BARREL),
	FLETCHER("fletcher", BuiltInLootTables.VILLAGE_FLETCHER, Blocks.FLETCHING_TABLE),
	LEATHERWORKER("tannery", BuiltInLootTables.VILLAGE_TANNERY,
			Blocks.CAULDRON, Blocks.WATER_CAULDRON, Blocks.LAVA_CAULDRON, Blocks.POWDER_SNOW_CAULDRON),
	LIBRARIAN("library", libraryLoot(), Blocks.LECTERN),
	MASON("mason", BuiltInLootTables.VILLAGE_MASON, Blocks.STONECUTTER),
	SHEPHERD("shepherd", BuiltInLootTables.VILLAGE_SHEPHERD, Blocks.LOOM),
	TOOLSMITH("tool", BuiltInLootTables.VILLAGE_TOOLSMITH, Blocks.SMITHING_TABLE),
	WEAPONSMITH("weapon", BuiltInLootTables.VILLAGE_WEAPONSMITH, Blocks.GRINDSTONE);

	private static final List<VillageProfession> MATCH_ORDER = List.of(
			CARTOGRAPHER, FLETCHER, SHEPHERD, LEATHERWORKER, WEAPONSMITH, TOOLSMITH,
			ARMORER, BUTCHER, FISHERMAN, LIBRARIAN, MASON, CLERIC, FARMER
	);

	private final String pathToken;
	private final ResourceKey<LootTable> lootTable;
	private final Set<Block> jobSites;

	VillageProfession(String pathToken, ResourceKey<LootTable> lootTable, Block... jobSites) {
		this.pathToken = pathToken;
		this.lootTable = lootTable;
		this.jobSites = Set.of(jobSites);
	}

	public ResourceKey<LootTable> lootTable() {
		return lootTable;
	}

	public boolean isJobSite(BlockState state) {
		return jobSites.contains(state.getBlock());
	}

	public static VillageProfession fromTemplate(Identifier template) {
		if (template == null) {
			return null;
		}
		String path = template.getPath().toLowerCase(Locale.ROOT);
		if (!path.contains("/houses/")) {
			return null;
		}
		for (VillageProfession profession : MATCH_ORDER) {
			if (path.contains(profession.pathToken)) {
				return profession;
			}
		}
		return null;
	}

	public static boolean isProfessionHouse(Identifier template) {
		return fromTemplate(template) != null;
	}

	private static ResourceKey<LootTable> libraryLoot() {
		return ResourceKey.create(
				Registries.LOOT_TABLE,
				Identifier.fromNamespaceAndPath(StructureLootRebalance.MOD_ID, "chests/village/library")
		);
	}

	private static ResourceKey<LootTable> farmLoot() {
		return ResourceKey.create(
				Registries.LOOT_TABLE,
				Identifier.fromNamespaceAndPath(StructureLootRebalance.MOD_ID, "chests/village/farm")
		);
	}
}
