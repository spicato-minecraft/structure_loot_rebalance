package structure_loot_rebalance.loot;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.storage.loot.LootContext;

/**
 * Halves effective enchanted armor and tool drop rates on modded structure tables
 * so vanilla gear does not overshadow mod tool-book progression.
 */
public final class VanillaEnchantedGearRateLimiter {

	static final float KEEP_CHANCE = 0.5f;

	private VanillaEnchantedGearRateLimiter() {
	}

	static void removeHalf(LootContext context, java.util.List<ItemStack> drops) {
		drops.removeIf(stack -> shouldRemove(context, stack));
	}

	private static boolean shouldRemove(LootContext context, ItemStack stack) {
		return isCompetingVanillaGear(stack) && context.getRandom().nextFloat() >= KEEP_CHANCE;
	}

	static boolean isCompetingVanillaGear(ItemStack stack) {
		return isEnchantedPlayerArmor(stack) || isEnchantedTool(stack);
	}

	static boolean isEnchantedPlayerArmor(ItemStack stack) {
		if (stack.isEmpty() || !stack.isEnchanted()) {
			return false;
		}
		Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
		if (equippable == null) {
			return false;
		}
		return switch (equippable.slot()) {
			case HEAD, CHEST, LEGS, FEET -> true;
			default -> false;
		};
	}

	static boolean isEnchantedTool(ItemStack stack) {
		return !stack.isEmpty() && stack.isEnchanted() && stack.has(DataComponents.TOOL);
	}
}
