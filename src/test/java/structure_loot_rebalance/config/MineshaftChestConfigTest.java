package structure_loot_rebalance.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MineshaftChestConfigTest {

	@AfterEach
	void reset() {
		MineshaftChestConfig.reset();
	}

	@Test
	void defaultRollBoundTargetsMoreChestsThanVanilla() {
		assertEquals(20, MineshaftChestConfig.rollBound());
	}

	@Test
	void worldgenConfigOverridesRollBound() {
		WorldgenConfigLoader.parse("""
				{ "mineshaft_chest_roll_bound": 25 }
				""");
		assertEquals(25, MineshaftChestConfig.rollBound());
	}

	@Test
	void rollBoundMustStayInRange() {
		assertThrows(IllegalArgumentException.class, () -> MineshaftChestConfig.setRollBound(0));
		assertThrows(IllegalArgumentException.class, () -> MineshaftChestConfig.setRollBound(101));
	}
}
