package structure_loot_rebalance.mixin;

import net.minecraft.world.level.levelgen.structure.structures.MineshaftPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import structure_loot_rebalance.config.MineshaftChestConfig;

@Mixin(MineshaftPieces.MineShaftCorridor.class)
public abstract class MineShaftCorridorChestSpawnMixin {

	@ModifyConstant(
			method = "postProcess",
			constant = @Constant(intValue = MineshaftChestConfig.VANILLA_ROLL_BOUND),
			require = 2
	)
	private int structureLootRebalance$increaseChestSpawnRate(int vanillaBound) {
		return MineshaftChestConfig.rollBound();
	}
}
