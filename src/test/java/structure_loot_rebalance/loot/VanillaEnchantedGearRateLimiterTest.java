package structure_loot_rebalance.loot;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.Equippable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import structure_loot_rebalance.MinecraftTestBootstrap;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanillaEnchantedGearRateLimiterTest {

	private static final long SEED = 261L;
	private static final int GEAR = 400;
	private static final int KEPT_FOR_SEED = 201;
	private static final int EMPTY_STACKS = 7;

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void gearLimiterHalvesWithFixedRandom() {
		Holder<Enchantment> enchantment = Holder.direct(new Enchantment(
				Component.literal("test"),
				Enchantment.definition(
						HolderSet.empty(),
						1,
						1,
						Enchantment.constantCost(1),
						Enchantment.constantCost(1),
						1,
						EquipmentSlotGroup.MAINHAND
				),
				HolderSet.empty(),
				DataComponentMap.EMPTY
		));

		List<ItemStack> drops = new ArrayList<>();
		for (int i = 0; i < GEAR; i++) {
			drops.add(enchantedGear(i % 2 == 0, enchantment));
		}
		assertTrue(VanillaEnchantedGearRateLimiter.isCompetingVanillaGear(drops.get(0)));
		assertTrue(VanillaEnchantedGearRateLimiter.isCompetingVanillaGear(drops.get(1)));

		for (int i = 0; i < EMPTY_STACKS; i++) {
			drops.add(ItemStack.EMPTY);
		}
		drops.add(plain(Items.DIAMOND_PICKAXE));
		drops.add(plain(Items.BOOK));

		VanillaEnchantedGearRateLimiter.removeHalf(RandomSource.create(SEED), drops);

		long remainingGear = drops.stream().filter(VanillaEnchantedGearRateLimiter::isCompetingVanillaGear).count();
		long remainingEmpty = drops.stream().filter(ItemStack::isEmpty).count();
		long plainPickaxes = drops.stream()
				.filter(stack -> stack.is(Items.DIAMOND_PICKAXE) && !stack.isEnchanted())
				.count();
		long books = drops.stream().filter(stack -> stack.is(Items.BOOK)).count();

		assertEquals(KEPT_FOR_SEED, remainingGear, "enchanted gear kept for seed " + SEED);
		assertEquals(EMPTY_STACKS, remainingEmpty);
		assertEquals(1, plainPickaxes);
		assertEquals(1, books);
		assertFalse(VanillaEnchantedGearRateLimiter.isCompetingVanillaGear(ItemStack.EMPTY));
	}

	private static ItemStack enchantedGear(boolean armor, Holder<Enchantment> enchantment) {
		DataComponentMap.Builder components = DataComponentMap.builder();
		Item item;
		if (armor) {
			item = Items.DIAMOND_CHESTPLATE;
			components.set(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST).build());
		} else {
			item = Items.DIAMOND_PICKAXE;
			components.set(DataComponents.TOOL, new Tool(List.of(), 1.0f, 1, true));
		}
		ItemStack stack = new ItemStack(Holder.direct(item, components.build()));
		ItemEnchantments.Mutable stored = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
		stored.set(enchantment, 1);
		stack.set(DataComponents.ENCHANTMENTS, stored.toImmutable());
		return stack;
	}

	private static ItemStack plain(Item item) {
		return new ItemStack(Holder.direct(item, DataComponentMap.EMPTY));
	}
}
