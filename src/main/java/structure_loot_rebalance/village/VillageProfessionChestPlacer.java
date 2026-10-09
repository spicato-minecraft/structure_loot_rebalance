package structure_loot_rebalance.village;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Inserts one profession chest next to a job site when the piece has none.
 */
public final class VillageProfessionChestPlacer {

	private VillageProfessionChestPlacer() {
	}

	public static List<StructureBlockInfo> ensureChest(
			VillageProfession profession,
			List<StructureBlockInfo> processed
	) {
		Map<BlockPos, StructureBlockInfo> byPos = index(processed);
		if (hasChest(byPos)) {
			return processed;
		}
		Optional<Placement> placement = findPlacement(profession, byPos);
		if (placement.isEmpty()) {
			return processed;
		}
		Placement chosen = placement.get();
		List<StructureBlockInfo> result = new ArrayList<>(processed.size() + 1);
		for (StructureBlockInfo block : processed) {
			if (!block.pos().equals(chosen.pos())) {
				result.add(block);
			}
		}
		result.add(chest(chosen.pos(), chosen.facing(), profession.lootTable()));
		return result;
	}

	static Optional<Placement> findPlacement(
			VillageProfession profession,
			Map<BlockPos, StructureBlockInfo> byPos
	) {
		Placement best = null;
		for (StructureBlockInfo block : byPos.values()) {
			if (!profession.isJobSite(block.state())) {
				continue;
			}
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				BlockPos candidate = block.pos().relative(direction);
				int score = score(candidate, byPos);
				if (score < 0) {
					continue;
				}
				Direction facing = direction.getOpposite();
				Placement next = new Placement(candidate, facing, score, packed(candidate));
				if (best == null || next.isBetterThan(best)) {
					best = next;
				}
			}
		}
		return Optional.ofNullable(best);
	}

	static boolean hasChest(Map<BlockPos, StructureBlockInfo> byPos) {
		for (StructureBlockInfo block : byPos.values()) {
			if (block.state().getBlock() instanceof ChestBlock) {
				return true;
			}
		}
		return false;
	}

	private static int score(BlockPos pos, Map<BlockPos, StructureBlockInfo> byPos) {
		StructureBlockInfo existing = byPos.get(pos);
		if (existing != null && !isReplaceable(existing.state())) {
			return -1;
		}
		StructureBlockInfo below = byPos.get(pos.below());
		boolean solidFloor = below != null && !below.state().isAir() && !below.state().canBeReplaced();
		if (existing == null) {
			return solidFloor ? 6 : -1;
		}
		return solidFloor ? 15 : 10;
	}

	private static boolean isReplaceable(BlockState state) {
		if (state.getBlock() instanceof JigsawBlock) {
			return false;
		}
		return state.isAir();
	}

	private static Map<BlockPos, StructureBlockInfo> index(List<StructureBlockInfo> processed) {
		Map<BlockPos, StructureBlockInfo> byPos = new HashMap<>();
		for (StructureBlockInfo block : processed) {
			byPos.put(block.pos(), block);
		}
		return byPos;
	}

	private static StructureBlockInfo chest(BlockPos pos, Direction facing, ResourceKey<LootTable> lootTable) {
		BlockState state = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing);
		CompoundTag nbt = new CompoundTag();
		nbt.putString("id", "minecraft:chest");
		nbt.putString(RandomizableContainer.LOOT_TABLE_TAG, lootTable.identifier().toString());
		return new StructureBlockInfo(pos, state, nbt);
	}

	private static long packed(BlockPos pos) {
		return BlockPos.asLong(pos.getX(), pos.getY(), pos.getZ());
	}

	record Placement(BlockPos pos, Direction facing, int score, long packed) {
		boolean isBetterThan(Placement other) {
			if (score != other.score) {
				return score > other.score;
			}
			return packed < other.packed;
		}
	}
}
