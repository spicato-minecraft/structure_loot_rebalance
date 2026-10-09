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
import structure_loot_rebalance.StructureLootRebalance;

/**
 * Applies one fixed enchantment level to an enchanted book stack.
 */
public final class FixedEnchantBookFunction implements LootItemFunction {

	public static final MapCodec<FixedEnchantBookFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					Codec.STRING.fieldOf("enchantment").forGetter(FixedEnchantBookFunction::enchantmentId),
					Codec.INT.optionalFieldOf("level", 1).forGetter(FixedEnchantBookFunction::level)
			).apply(instance, FixedEnchantBookFunction::new)
	);

	private final String enchantmentId;
	private final int level;

	public FixedEnchantBookFunction(String enchantmentId) {
		this(enchantmentId, 1);
	}

	public FixedEnchantBookFunction(String enchantmentId, int level) {
		this.enchantmentId = enchantmentId;
		this.level = Math.max(1, level);
	}

	public static void register() {
		Registry.register(
				BuiltInRegistries.LOOT_FUNCTION_TYPE,
				Identifier.fromNamespaceAndPath(StructureLootRebalance.MOD_ID, "fixed_enchant_book"),
				CODEC
		);
	}

	@Override
	public MapCodec<FixedEnchantBookFunction> codec() {
		return CODEC;
	}

	public String enchantmentId() {
		return enchantmentId;
	}

	public int level() {
		return level;
	}

	@Override
	public ItemStack apply(ItemStack stack, LootContext context) {
		return ModEnchantedBooks.create(enchantmentId, level, context.getResolver());
	}
}
