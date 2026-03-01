package structure_loot_rebalance.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import structure_loot_rebalance.StructureLootRebalance;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import structure_loot_rebalance.config.DefaultLootModifications.AddEntryConfig;
import structure_loot_rebalance.config.DefaultLootModifications.EnchantedBookSpec;
import structure_loot_rebalance.config.DefaultLootModifications.ItemSpec;
import structure_loot_rebalance.config.DefaultLootModifications.LootTableModificationConfig;
import structure_loot_rebalance.config.DefaultLootModifications.ModifyEntryConfig;
import structure_loot_rebalance.config.DefaultLootModifications.RemoveEntryConfig;
import structure_loot_rebalance.config.DefaultLootModifications.SimpleItemSpec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Root config for loot-modifications.json. Parses and serializes the
 * loot_modifications map keyed by loot table ID.
 */
public record LootModificationsConfig(Map<ResourceLocation, LootTableModificationConfig> lootModifications) {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static LootModificationsConfig load(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject mods = root.getAsJsonObject("loot_modifications");
            Map<ResourceLocation, LootTableModificationConfig> map = new LinkedHashMap<>();

            if (mods != null) {
                for (Map.Entry<String, JsonElement> entry : mods.entrySet()) {
                    ResourceLocation tableId = ResourceLocation.tryParse(entry.getKey());
                    if (tableId != null) {
                        LootTableModificationConfig table = parseTableModification(entry.getValue().getAsJsonObject());
                        map.put(tableId, table);
                    }
                }
            }

            return new LootModificationsConfig(map);
        } catch (Exception e) {
            throw new JsonParseException("Failed to parse loot modifications config", e);
        }
    }

    private static LootTableModificationConfig parseTableModification(JsonObject obj) {
        List<AddEntryConfig> add = parseAddList(obj.getAsJsonArray("add"));
        List<RemoveEntryConfig> remove = parseRemoveList(obj.getAsJsonArray("remove"));
        List<ModifyEntryConfig> modify = parseModifyList(obj.getAsJsonArray("modify"));
        return new LootTableModificationConfig(add, remove, modify);
    }

    private static List<AddEntryConfig> parseAddList(JsonElement elem) {
        List<AddEntryConfig> list = new ArrayList<>();
        if (elem == null || !elem.isJsonArray()) return list;
        for (JsonElement e : elem.getAsJsonArray()) {
            try {
                list.add(parseAddEntry(e.getAsJsonObject()));
            } catch (Exception ex) {
                StructureLootRebalance.LOGGER.warn("Skipping invalid add entry: {}", ex.getMessage());
            }
        }
        return list;
    }

    private static AddEntryConfig parseAddEntry(JsonObject obj) {
        ItemSpec item = parseItemSpec(obj.get("item"));
        double weight = obj.has("weight") ? obj.get("weight").getAsDouble() : 1.0;
        if (weight <= 0) {
            StructureLootRebalance.LOGGER.warn("Invalid weight {} in add entry, using 1.0", weight);
            weight = 1.0;
        }
        int qMin = 1;
        int qMax = 1;
        if (obj.has("quantity")) {
            JsonElement q = obj.get("quantity");
            if (q.isJsonArray()) {
                JsonArray arr = q.getAsJsonArray();
                qMin = arr.size() > 0 ? arr.get(0).getAsInt() : 1;
                qMax = arr.size() > 1 ? arr.get(1).getAsInt() : qMin;
            } else {
                qMin = qMax = q.getAsInt();
            }
        }
        return new AddEntryConfig(item, weight, qMin, qMax);
    }

    private static ItemSpec parseItemSpec(JsonElement elem) {
        if (elem == null) return new SimpleItemSpec("minecraft:air");
        if (elem.isJsonPrimitive()) {
            return new SimpleItemSpec(elem.getAsString());
        }
        JsonObject o = elem.getAsJsonObject();
        if (o.has("type") && "enchanted_book".equals(o.get("type").getAsString())) {
            String enchantment = o.has("enchantment") ? o.get("enchantment").getAsString() : "minecraft:mending";
            int level = o.has("level") ? o.get("level").getAsInt() : 1;
            return new EnchantedBookSpec(enchantment, level);
        }
        return new SimpleItemSpec(o.has("id") ? o.get("id").getAsString() : "minecraft:air");
    }

    private static List<RemoveEntryConfig> parseRemoveList(JsonElement elem) {
        List<RemoveEntryConfig> list = new ArrayList<>();
        if (elem == null || !elem.isJsonArray()) return list;
        for (JsonElement e : elem.getAsJsonArray()) {
            try {
                JsonObject o = e.getAsJsonObject();
                String item = o.has("item") ? o.get("item").getAsString() : null;
                if (item != null) {
                    String enchantment = o.has("enchantment") ? o.get("enchantment").getAsString() : null;
                    Object enchantmentLevel = o.has("enchantment_level") ? parseEnchantmentLevel(o.get("enchantment_level")) : null;
                    Integer weight = o.has("weight") ? o.get("weight").getAsInt() : null;
                    list.add(new RemoveEntryConfig(item, enchantment, enchantmentLevel, weight));
                }
            } catch (Exception ex) {
                StructureLootRebalance.LOGGER.warn("Skipping invalid remove entry: {}", ex.getMessage());
            }
        }
        return list;
    }

    private static Object parseEnchantmentLevel(JsonElement e) {
        if (e.isJsonArray()) {
            JsonArray a = e.getAsJsonArray();
            return a.size() >= 2
                    ? List.of(a.get(0).getAsInt(), a.get(1).getAsInt())
                    : (a.size() == 1 ? a.get(0).getAsInt() : 1);
        }
        return e.getAsInt();
    }

    private static List<ModifyEntryConfig> parseModifyList(JsonElement elem) {
        List<ModifyEntryConfig> list = new ArrayList<>();
        if (elem == null || !elem.isJsonArray()) return list;
        for (JsonElement e : elem.getAsJsonArray()) {
            try {
                JsonObject o = e.getAsJsonObject();
                String item = o.has("item") ? o.get("item").getAsString() : null;
                List<String> exclude = new ArrayList<>();
                if (o.has("exclude_enchantments")) {
                    for (JsonElement x : o.getAsJsonArray("exclude_enchantments")) {
                        exclude.add(x.getAsString());
                    }
                }
                if (item != null) {
                    list.add(new ModifyEntryConfig(item, exclude));
                }
            } catch (Exception ex) {
                StructureLootRebalance.LOGGER.warn("Skipping invalid modify entry: {}", ex.getMessage());
            }
        }
        return list;
    }

    public String toJson() {
        JsonObject root = new JsonObject();
        JsonObject mods = new JsonObject();
        for (Map.Entry<ResourceLocation, LootTableModificationConfig> entry : lootModifications.entrySet()) {
            mods.add(entry.getKey().toString(), serializeTableModification(entry.getValue()));
        }
        root.add("loot_modifications", mods);
        return GSON.toJson(root);
    }

    private static JsonObject serializeTableModification(LootTableModificationConfig table) {
        JsonObject obj = new JsonObject();
        JsonArray addArr = new JsonArray();
        for (AddEntryConfig a : table.add()) {
            addArr.add(serializeAddEntry(a));
        }
        obj.add("add", addArr);

        JsonArray removeArr = new JsonArray();
        for (RemoveEntryConfig r : table.remove()) {
            removeArr.add(serializeRemoveEntry(r));
        }
        obj.add("remove", removeArr);

        JsonArray modifyArr = new JsonArray();
        for (ModifyEntryConfig m : table.modify()) {
            modifyArr.add(serializeModifyEntry(m));
        }
        obj.add("modify", modifyArr);
        return obj;
    }

    private static JsonObject serializeAddEntry(AddEntryConfig a) {
        JsonObject obj = new JsonObject();
        if (a.item() instanceof EnchantedBookSpec spec) {
            JsonObject itemObj = new JsonObject();
            itemObj.addProperty("type", "enchanted_book");
            itemObj.addProperty("enchantment", spec.enchantment());
            itemObj.addProperty("level", spec.level());
            obj.add("item", itemObj);
        } else if (a.item() instanceof SimpleItemSpec spec) {
            obj.addProperty("item", spec.id());
        }
        obj.addProperty("weight", a.weight());
        if (a.quantityMin() == a.quantityMax()) {
            obj.addProperty("quantity", a.quantityMin());
        } else {
            JsonArray q = new JsonArray();
            q.add(a.quantityMin());
            q.add(a.quantityMax());
            obj.add("quantity", q);
        }
        return obj;
    }

    private static JsonObject serializeRemoveEntry(RemoveEntryConfig r) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", r.item());
        if (r.enchantment() != null) obj.addProperty("enchantment", r.enchantment());
        if (r.enchantmentLevel() != null) {
            if (r.enchantmentLevel() instanceof List<?> list) {
                JsonArray arr = new JsonArray();
                for (Object x : list) arr.add(((Number) x).intValue());
                obj.add("enchantment_level", arr);
            } else {
                obj.addProperty("enchantment_level", ((Number) r.enchantmentLevel()).intValue());
            }
        }
        if (r.weight() != null) obj.addProperty("weight", r.weight());
        return obj;
    }

    private static JsonObject serializeModifyEntry(ModifyEntryConfig m) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", m.item());
        JsonArray arr = new JsonArray();
        for (String e : m.excludeEnchantments()) arr.add(e);
        obj.add("exclude_enchantments", arr);
        return obj;
    }
}
