package structure_loot_rebalance.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import structure_loot_rebalance.village.VillageProfession;
import structure_loot_rebalance.village.VillageProfessionChestProcessor;

@Mixin(SinglePoolElement.class)
public abstract class SinglePoolElementProfessionChestMixin {

	@Inject(method = "getSettings", at = @At("RETURN"))
	private void structureLootRebalance$addProfessionChest(
			Rotation rotation,
			BoundingBox boundingBox,
			LiquidSettings liquidSettings,
			boolean keepJigsaws,
			CallbackInfoReturnable<StructurePlaceSettings> cir
	) {
		Identifier template;
		try {
			template = ((SinglePoolElement) (Object) this).getTemplateLocation();
		} catch (RuntimeException ignored) {
			return;
		}
		VillageProfession profession = VillageProfession.fromTemplate(template);
		if (profession == null) {
			return;
		}
		cir.getReturnValue().addProcessor(new VillageProfessionChestProcessor(profession));
	}
}
