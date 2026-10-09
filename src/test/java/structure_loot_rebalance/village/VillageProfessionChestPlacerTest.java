package structure_loot_rebalance.village;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import structure_loot_rebalance.MinecraftTestBootstrap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class VillageProfessionChestPlacerTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void placesChestOnAirBesideJobSite() {
		BlockPos stand = new BlockPos(2, 1, 2);
		BlockPos air = stand.relative(Direction.NORTH);
		List<StructureBlockInfo> processed = List.of(
				block(stand, Blocks.BREWING_STAND.defaultBlockState()),
				block(stand.below(), Blocks.COBBLESTONE.defaultBlockState()),
				block(air, Blocks.AIR.defaultBlockState()),
				block(air.below(), Blocks.COBBLESTONE.defaultBlockState()),
				block(stand.relative(Direction.SOUTH), Blocks.COBBLESTONE.defaultBlockState())
		);

		List<StructureBlockInfo> result = VillageProfessionChestPlacer.ensureChest(
				VillageProfession.CLERIC,
				processed
		);

		StructureBlockInfo chest = chestIn(result);
		assertEquals(air, chest.pos());
		assertEquals(Direction.SOUTH, chest.state().getValue(ChestBlock.FACING));
		assertEquals(
				VillageProfession.CLERIC.lootTable().identifier().toString(),
				chest.nbt().getString(RandomizableContainer.LOOT_TABLE_TAG).orElseThrow()
		);
	}

	@Test
	void keepsExistingChestWithoutPlacingASecond() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString(RandomizableContainer.LOOT_TABLE_TAG, "minecraft:chests/village/village_temple");
		List<StructureBlockInfo> processed = List.of(
				block(new BlockPos(1, 1, 1), Blocks.BREWING_STAND.defaultBlockState()),
				block(new BlockPos(2, 1, 1), Blocks.AIR.defaultBlockState()),
				new StructureBlockInfo(
						new BlockPos(3, 1, 1),
						Blocks.CHEST.defaultBlockState(),
						nbt
				)
		);

		List<StructureBlockInfo> result = VillageProfessionChestPlacer.ensureChest(
				VillageProfession.CLERIC,
				processed
		);

		assertSame(processed, result);
		assertEquals(1, result.stream().filter(block -> block.state().getBlock() instanceof ChestBlock).count());
		assertEquals(
				"minecraft:chests/village/village_temple",
				chestIn(result).nbt().getString(RandomizableContainer.LOOT_TABLE_TAG).orElseThrow()
		);
	}

	@Test
	void doesNotPlaceWhenJobSiteHasNoAirNeighbor() {
		BlockPos stand = new BlockPos(0, 1, 0);
		List<StructureBlockInfo> processed = List.of(
				block(stand, Blocks.BREWING_STAND.defaultBlockState()),
				block(stand.north(), Blocks.COBBLESTONE.defaultBlockState()),
				block(stand.south(), Blocks.COBBLESTONE.defaultBlockState()),
				block(stand.east(), Blocks.COBBLESTONE.defaultBlockState()),
				block(stand.west(), Blocks.COBBLESTONE.defaultBlockState())
		);

		List<StructureBlockInfo> result = VillageProfessionChestPlacer.ensureChest(
				VillageProfession.CLERIC,
				processed
		);

		assertSame(processed, result);
	}

	@Test
	void placesFarmChestBesideComposter() {
		BlockPos composter = new BlockPos(4, 1, 4);
		BlockPos air = composter.west();
		List<StructureBlockInfo> processed = List.of(
				block(composter, Blocks.COMPOSTER.defaultBlockState()),
				block(air, Blocks.AIR.defaultBlockState()),
				block(air.below(), Blocks.DIRT.defaultBlockState())
		);

		List<StructureBlockInfo> result = VillageProfessionChestPlacer.ensureChest(
				VillageProfession.FARMER,
				processed
		);

		StructureBlockInfo chest = chestIn(result);
		assertEquals(air, chest.pos());
		assertEquals(
				"structure_loot_rebalance:chests/village/farm",
				chest.nbt().getString(RandomizableContainer.LOOT_TABLE_TAG).orElseThrow()
		);
	}

	private static StructureBlockInfo chestIn(List<StructureBlockInfo> blocks) {
		return blocks.stream()
				.filter(block -> block.state().getBlock() instanceof ChestBlock)
				.findFirst()
				.orElseThrow();
	}

	private static StructureBlockInfo block(BlockPos pos, BlockState state) {
		return new StructureBlockInfo(pos, state, null);
	}
}
