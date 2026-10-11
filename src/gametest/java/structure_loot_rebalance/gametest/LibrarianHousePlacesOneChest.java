package structure_loot_rebalance.gametest;

import com.mojang.datafixers.util.Either;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.LootTable;
import structure_loot_rebalance.village.VillageProfession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LibrarianHousePlacesOneChest {

	private static final Identifier LIBRARIAN_HOUSE = Identifier.fromNamespaceAndPath(
			"minecraft",
			"village/plains/houses/plains_library_1"
	);

	@GameTest(structure = "structure_loot_rebalance_test:library_room", maxTicks = 100)
	public void librarianHousePlacesOneChest(GameTestHelper context) {
		ServerLevel level = context.getLevel();
		StructureTemplateManager templates = level.getStructureTemplateManager();
		SinglePoolElement piece = new LibrarianHousePiece();
		Vec3i size = piece.getSize(templates, net.minecraft.world.level.block.Rotation.NONE);
		BlockPos origin = context.absolutePos(new BlockPos(1, 1, 1));
		BlockPos far = origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1);
		BoundingBox bounds = BoundingBox.fromCorners(origin, far);

		boolean placed = piece.place(
				templates,
				level,
				level.structureManager(),
				level.getChunkSource().getGenerator(),
				origin,
				origin,
				net.minecraft.world.level.block.Rotation.NONE,
				bounds,
				level.getRandom(),
				LiquidSettings.IGNORE_WATERLOGGING,
				false
		);
		context.assertTrue(placed, "vanilla place path did not place the librarian house");

		List<BlockPos> chests = new ArrayList<>();
		int lecterns = 0;
		for (BlockPos pos : BlockPos.betweenClosed(origin, far)) {
			BlockState state = level.getBlockState(pos);
			if (state.getBlock() instanceof ChestBlock) {
				chests.add(pos.immutable());
			}
			if (state.is(Blocks.LECTERN)) {
				lecterns++;
			}
		}

		context.assertTrue(lecterns > 0, "librarian house template did not place a lectern");
		context.assertTrue(chests.size() == 1, "expected one chest beside the librarian job site, found " + chests.size());

		BlockPos chestPos = chests.getFirst();
		context.assertTrue(besideLectern(level, chestPos), "chest at " + chestPos + " is not beside a lectern");

		BlockEntity blockEntity = level.getBlockEntity(chestPos);
		context.assertTrue(blockEntity instanceof RandomizableContainerBlockEntity, "placed chest has no container");
		RandomizableContainerBlockEntity chest = (RandomizableContainerBlockEntity) blockEntity;
		ResourceKey<LootTable> lootTable = chest.getLootTable();
		context.assertTrue(
				VillageProfession.LIBRARIAN.lootTable().equals(lootTable),
				"chest loot table was " + lootTable
		);
		context.succeed();
	}

	private static boolean besideLectern(ServerLevel level, BlockPos chestPos) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (level.getBlockState(chestPos.relative(direction)).is(Blocks.LECTERN)) {
				return true;
			}
		}
		return false;
	}

	private static final class LibrarianHousePiece extends SinglePoolElement {
		private LibrarianHousePiece() {
			super(
					Either.left(LIBRARIAN_HOUSE),
					Holder.direct(new StructureProcessorList(List.of())),
					StructureTemplatePool.Projection.RIGID,
					Optional.empty()
			);
		}
	}
}
