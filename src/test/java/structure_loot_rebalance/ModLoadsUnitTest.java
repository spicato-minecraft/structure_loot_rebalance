package structure_loot_rebalance;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ModLoadsUnitTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void modMainClassLoads() {
		assertNotNull(StructureLootRebalance.MOD_ID);
		assertEquals("structure_loot_rebalance", StructureLootRebalance.MOD_ID);
	}
}
