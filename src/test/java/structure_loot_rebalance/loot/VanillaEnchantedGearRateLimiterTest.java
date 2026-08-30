package structure_loot_rebalance.loot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class VanillaEnchantedGearRateLimiterTest {

	@Test
	void emptyOrUnenchantedStacksAreNotLimited() {
		assertFalse(VanillaEnchantedGearRateLimiter.isCompetingVanillaGear(net.minecraft.world.item.ItemStack.EMPTY));
	}
}
