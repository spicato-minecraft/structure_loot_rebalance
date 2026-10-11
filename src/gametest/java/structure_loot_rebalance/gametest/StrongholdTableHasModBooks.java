package structure_loot_rebalance.gametest;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import structure_loot_rebalance.loot.FixedEnchantBookFunction;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class StrongholdTableHasModBooks {

	private static final String SILK_TOUCH = "minecraft:silk_touch";

	@GameTest(maxTicks = 20)
	public void strongholdTableHasModBooks(GameTestHelper context) {
		LootTable table = context.getLevel().getServer().reloadableRegistries().getLootTable(BuiltInLootTables.STRONGHOLD_LIBRARY);
		context.assertTrue(table != null && table != LootTable.EMPTY, "stronghold library loot table is not loaded");

		List<LootItemFunction> functions = functionsIn(table);
		List<FixedEnchantBookFunction> modBooks = functions.stream()
				.filter(FixedEnchantBookFunction.class::isInstance)
				.map(FixedEnchantBookFunction.class::cast)
				.toList();
		context.assertTrue(!modBooks.isEmpty(), "live stronghold library table was never modified");

		for (FixedEnchantBookFunction book : modBooks) {
			context.assertTrue(
					!SILK_TOUCH.equals(book.enchantmentId()),
					"stronghold library offers Silk Touch via " + book.enchantmentId()
			);
		}

		RegistryOps<JsonElement> ops = context.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
		JsonElement json = LootTable.DIRECT_CODEC.encodeStart(ops, table).getOrThrow(IllegalStateException::new);
		context.assertTrue(
				!json.toString().contains(SILK_TOUCH),
				"stronghold library loot table offers Silk Touch"
		);
		context.succeed();
	}

	private static List<LootItemFunction> functionsIn(LootTable table) {
		List<LootItemFunction> functions = new ArrayList<>();
		Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());
		walk(table, seen, functions);
		return functions;
	}

	private static void walk(Object node, Set<Object> seen, List<LootItemFunction> functions) {
		if (node == null || !seen.add(node)) {
			return;
		}
		if (node instanceof LootItemFunction function) {
			functions.add(function);
			return;
		}
		if (node instanceof Holder<?> holder) {
			if (holder.isBound()) {
				walk(holder.value(), seen, functions);
			}
			return;
		}
		if (node instanceof LootTable table) {
			walk(read(table, "pools"), seen, functions);
			walk(read(table, "modifier"), seen, functions);
			return;
		}
		if (node instanceof LootPool pool) {
			walk(read(pool, "entries"), seen, functions);
			walk(read(pool, "modifier"), seen, functions);
			return;
		}
		if (node instanceof LootPoolEntryContainer entry) {
			walk(read(entry, "modifier"), seen, functions);
		}
		if (node instanceof NestedLootTable nested) {
			walk(read(nested, "value"), seen, functions);
			return;
		}
		if (node instanceof CompositeEntryBase composite) {
			walk(read(composite, "children"), seen, functions);
			return;
		}
		if (node instanceof Iterable<?> iterable) {
			for (Object value : iterable) {
				walk(value, seen, functions);
			}
			return;
		}
		if (node instanceof Optional<?> optional) {
			optional.ifPresent(value -> walk(value, seen, functions));
			return;
		}
		if (node instanceof Either<?, ?> either) {
			either.left().ifPresent(value -> walk(value, seen, functions));
			either.right().ifPresent(value -> walk(value, seen, functions));
		}
	}

	private static Object read(Object target, String fieldName) {
		Class<?> type = target.getClass();
		while (type != null && type != Object.class) {
			try {
				Field field = type.getDeclaredField(fieldName);
				field.setAccessible(true);
				return field.get(target);
			} catch (NoSuchFieldException ignored) {
				type = type.getSuperclass();
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Could not read " + fieldName + " on " + target.getClass().getName(), e);
			}
		}
		throw new IllegalStateException("Missing field " + fieldName + " on " + target.getClass().getName());
	}
}
