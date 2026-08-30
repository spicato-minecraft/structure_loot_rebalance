package structure_loot_rebalance.loot;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import structure_loot_rebalance.StructureLootRebalance;

public final class ModEnchantedBooks {

	private static final String MOD_BOOK_TAG = StructureLootRebalance.MOD_ID;

	private ModEnchantedBooks() {
	}

	public static ItemStack create(Holder<Enchantment> enchantment, int level) {
		ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
		ItemEnchantments.Mutable stored = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
		stored.set(enchantment, level);
		book.set(DataComponents.STORED_ENCHANTMENTS, stored.toImmutable());
		tagAsModBook(book);
		return book;
	}

	public static ItemStack create(String enchantmentId, int level, HolderGetter.Provider registries) {
		Identifier id = Identifier.parse(enchantmentId);
		Holder<Enchantment> holder = registries.lookupOrThrow(Registries.ENCHANTMENT)
				.getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, id));
		return create(holder, level);
	}

	public static boolean isModBook(ItemStack stack) {
		if (stack.isEmpty() || !stack.is(Items.ENCHANTED_BOOK)) {
			return false;
		}
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		if (customData == null) {
			return false;
		}
		return customData.copyTag().getBoolean(MOD_BOOK_TAG).orElse(false);
	}

	private static void tagAsModBook(ItemStack book) {
		CompoundTag tag = new CompoundTag();
		tag.putBoolean(MOD_BOOK_TAG, true);
		book.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}
}
