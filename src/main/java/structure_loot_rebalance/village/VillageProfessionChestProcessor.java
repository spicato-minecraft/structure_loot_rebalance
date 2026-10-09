package structure_loot_rebalance.village;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import structure_loot_rebalance.StructureLootRebalance;

import java.util.List;

/**
 * Adds a profession chest to village job-site houses that vanilla left empty.
 */
public final class VillageProfessionChestProcessor implements StructureProcessor {

	public static final MapCodec<VillageProfessionChestProcessor> CODEC = MapCodec.unit(
			() -> new VillageProfessionChestProcessor(null)
	);

	private final VillageProfession profession;

	public VillageProfessionChestProcessor(VillageProfession profession) {
		this.profession = profession;
	}

	public static void register() {
		Registry.register(
				BuiltInRegistries.STRUCTURE_PROCESSOR,
				Identifier.fromNamespaceAndPath(StructureLootRebalance.MOD_ID, "village_profession_chest"),
				CODEC
		);
	}

	@Override
	public List<StructureBlockInfo> finalizeProcessing(
			ServerLevelAccessor level,
			BlockPos position,
			BlockPos referencePos,
			List<StructureBlockInfo> originalBlockInfoList,
			List<StructureBlockInfo> processedBlockInfoList,
			StructurePlaceSettings settings
	) {
		if (profession == null) {
			return processedBlockInfoList;
		}
		return VillageProfessionChestPlacer.ensureChest(profession, processedBlockInfoList);
	}

	@Override
	public MapCodec<VillageProfessionChestProcessor> codec() {
		return CODEC;
	}
}
