package structure_loot_rebalance.village;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import structure_loot_rebalance.StructureLootRebalance;

import java.util.List;

/**
 * Adds a profession chest to village job-site houses that vanilla left empty.
 */
public final class VillageProfessionChestProcessor extends StructureProcessor {

	public static final MapCodec<VillageProfessionChestProcessor> CODEC = MapCodec.unit(
			() -> new VillageProfessionChestProcessor(null)
	);

	private static StructureProcessorType<VillageProfessionChestProcessor> TYPE;

	private final VillageProfession profession;

	public VillageProfessionChestProcessor(VillageProfession profession) {
		this.profession = profession;
	}

	public static void register() {
		TYPE = () -> CODEC;
		Registry.register(
				BuiltInRegistries.STRUCTURE_PROCESSOR,
				Identifier.fromNamespaceAndPath(StructureLootRebalance.MOD_ID, "village_profession_chest"),
				TYPE
		);
	}

	@Override
	public StructureBlockInfo processBlock(
			LevelReader level,
			BlockPos offset,
			BlockPos piecePos,
			StructureBlockInfo rawBlock,
			StructureBlockInfo worldBlock,
			StructurePlaceSettings settings
	) {
		return worldBlock;
	}

	@Override
	public List<StructureBlockInfo> finalizeProcessing(
			ServerLevelAccessor level,
			BlockPos offset,
			BlockPos piecePos,
			List<StructureBlockInfo> rawBlocks,
			List<StructureBlockInfo> processedBlocks,
			StructurePlaceSettings settings
	) {
		if (profession == null) {
			return processedBlocks;
		}
		return VillageProfessionChestPlacer.ensureChest(profession, processedBlocks);
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return TYPE;
	}
}
