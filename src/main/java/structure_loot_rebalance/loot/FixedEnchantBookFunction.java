package structure_loot_rebalance.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import structure_loot_rebalance.StructureLootRebalance;

/**
 * Applies a fixed Level I enchantment to an enchanted book stack.
 */
public final class FixedEnchantBookFunction implements LootItemFunction {

	public static final MapCodec<FixedEnchantBookFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					Codec.STRING.fieldOf("enchantment").forGetter(FixedEnchantBookFunction::enchantmentId)
			).apply(instance, FixedEnchantBookFunction::new)
	);
	public static final LootItemFunctionType<FixedEnchantBookFunction> TYPE =
			new LootItemFunctionType<>(CODEC);

	private final String enchantmentId;

	public FixedEnchantBookFunction(String enchantmentId) {
		this.enchantmentId = enchantmentId;
	}

	public static void register() {
		Registry.register(
				BuiltInRegistries.LOOT_FUNCTION_TYPE,
				Identifier.fromNamespaceAndPath(StructureLootRebalance.MOD_ID, "fixed_enchant_book"),
				TYPE
		);
	}

	public String enchantmentId() {
		return enchantmentId;
	}

	@Override
	public LootItemFunctionType<FixedEnchantBookFunction> getType() {
		return TYPE;
	}

	@Override
	public ItemStack apply(ItemStack stack, LootContext context) {
		return ModEnchantedBooks.create(enchantmentId, 1, context.getResolver());
	}
}
