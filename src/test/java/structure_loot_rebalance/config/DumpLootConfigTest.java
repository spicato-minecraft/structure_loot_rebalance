package structure_loot_rebalance.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

class DumpLootConfigTest {

	@Test
	void writeRunConfigDefaults() throws Exception {
		Path configPath = Path.of("run/config/structure_loot_rebalance/loot-modifications.json");
		Files.createDirectories(configPath.getParent());
		LootModificationsConfig config = new LootModificationsConfig(StructureTierRegistry.buildDefaults());
		Files.writeString(configPath, config.toJson());
	}
}
