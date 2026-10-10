package structure_loot_rebalance.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Loads every common mixin target so a renamed class fails here instead of in a player session.
 */
public class ServerMixinApplySweep {

	@GameTest(maxTicks = 20)
	public void serverMixinApplySweep(GameTestHelper context) {
		load(context, "net.minecraft.world.level.levelgen.structure.structures.MineshaftPieces$MineShaftCorridor");
		load(context, "net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement");
		context.succeed();
	}

	private static void load(GameTestHelper context, String binaryName) {
		try {
			Class.forName(binaryName);
		} catch (ClassNotFoundException e) {
			context.fail("Mixin target failed to load: " + binaryName);
		}
	}
}
