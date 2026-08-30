package structure_loot_rebalance.loot;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;

/**
 * Removes Silk Touch from structure loot; villager librarians remain the source.
 */
public final class SilkTouchLootFilter {

	private SilkTouchLootFilter() {
	}

	static void stripFromDrops(LootContext context, java.util.List<ItemStack> drops) {
		Holder<Enchantment> silkTouch = silkTouch(context);
		drops.removeIf(stack -> shouldRemoveStack(stack, silkTouch));
		for (int i = 0; i < drops.size(); i++) {
			ItemStack stripped = stripSilkTouch(drops.get(i), silkTouch);
			if (stripped != drops.get(i)) {
				drops.set(i, stripped);
			}
		}
	}

	private static Holder<Enchantment> silkTouch(LootContext context) {
		return context.getLevel()
				.registryAccess()
				.lookupOrThrow(Registries.ENCHANTMENT)
				.getOrThrow(Enchantments.SILK_TOUCH);
	}

	private static boolean shouldRemoveStack(ItemStack stack, Holder<Enchantment> silkTouch) {
		if (stack.isEmpty()) {
			return false;
		}
		if (stack.is(Items.ENCHANTED_BOOK)) {
			ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
			return stored.getLevel(silkTouch) > 0 && stored.size() == 1;
		}
		return false;
	}

	private static ItemStack stripSilkTouch(ItemStack stack, Holder<Enchantment> silkTouch) {
		if (stack.isEmpty()) {
			return stack;
		}
		if (stack.is(Items.ENCHANTED_BOOK)) {
			return stripFromStoredEnchantments(stack, silkTouch);
		}
		if (!stack.isEnchanted()) {
			return stack;
		}
		ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
		if (enchants.getLevel(silkTouch) <= 0) {
			return stack;
		}
		ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchants);
		mutable.removeIf(holder -> holder.equals(silkTouch));
		ItemStack copy = stack.copy();
		if (mutable.keySet().isEmpty()) {
			copy.remove(DataComponents.ENCHANTMENTS);
		} else {
			copy.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
		}
		return copy;
	}

	private static ItemStack stripFromStoredEnchantments(ItemStack stack, Holder<Enchantment> silkTouch) {
		ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
		if (stored.getLevel(silkTouch) <= 0) {
			return stack;
		}
		ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(stored);
		mutable.removeIf(holder -> holder.equals(silkTouch));
		ItemStack copy = stack.copy();
		if (mutable.keySet().isEmpty()) {
			return ItemStack.EMPTY;
		}
		copy.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
		return copy;
	}
}
