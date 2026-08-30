package structure_loot_rebalance.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import structure_loot_rebalance.StructureLootRebalance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WorldgenConfigLoader {

	private WorldgenConfigLoader() {
	}

	public static void load() {
		MineshaftChestConfig.reset();
		Path path = FabricLoader.getInstance()
				.getConfigDir()
				.resolve(StructureLootRebalance.MOD_ID)
				.resolve("worldgen.json");
		if (!Files.exists(path)) {
			writeDefault(path);
			return;
		}
		try {
			parse(Files.readString(path));
		} catch (Exception e) {
			StructureLootRebalance.LOGGER.warn(
					"Failed to load worldgen config at {}, using defaults: {}",
					path.toAbsolutePath(),
					e.getMessage()
			);
			MineshaftChestConfig.reset();
		}
	}

	static void parse(String json) {
		JsonObject root = JsonParser.parseString(json).getAsJsonObject();
		if (root.has("mineshaft_chest_roll_bound")) {
			MineshaftChestConfig.setRollBound(root.get("mineshaft_chest_roll_bound").getAsInt());
		}
	}

	private static void writeDefault(Path path) {
		try {
			Files.createDirectories(path.getParent());
			String json = """
					{
					  "mineshaft_chest_roll_bound": %d
					}
					""".formatted(MineshaftChestConfig.DEFAULT_ROLL_BOUND);
			Files.writeString(path, json);
		} catch (IOException e) {
			StructureLootRebalance.LOGGER.warn("Failed to write default worldgen config: {}", e.getMessage());
		}
	}
}
