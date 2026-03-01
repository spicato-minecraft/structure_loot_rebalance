package mending_rebalance;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mending_rebalance.config.LootInjectionConfig;
import mending_rebalance.loot.LootTableModifier;

import java.nio.file.Path;
import java.util.List;

public class Mending_rebalance implements ModInitializer {
	public static final String MOD_ID = "mending_rebalance";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Path configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
		try {
			LootInjectionConfig.writeDefault(configDir);
		} catch (Exception e) {
			LOGGER.warn("Could not write default loot injection config", e);
		}
		LootInjectionConfig config = LootInjectionConfig.load(configDir);
		List<LootTableModifier.LootInjectionEntry> entries = config.entries().stream()
				.filter(e -> e.item() != null && e.loot_table() != null)
				.map(e -> {
					var loc = parseResourceLocation(e.loot_table(), "chests/simple_dungeon");
					return loc != null ? new LootTableModifier.LootInjectionEntry(e.item(), e.weightAsInt(), loc) : null;
				})
				.filter(e -> e != null)
				.toList();
		LootTableModifier.register(entries);
		LootTableModifier.init();
		LOGGER.info("Registered {} loot table injection entries", entries.size());
	}

	private static net.minecraft.resources.ResourceLocation parseResourceLocation(String s, String fallback) {
		if (s == null || s.isBlank()) {
			s = "minecraft:" + fallback;
		}
		if (!s.contains(":")) {
			s = "minecraft:" + s;
		}
		return net.minecraft.resources.ResourceLocation.tryParse(s);
	}
}