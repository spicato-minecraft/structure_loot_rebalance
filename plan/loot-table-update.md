# Loot Table Add/Remove Update — Planning Prompt

Use this prompt in a new chat to plan the update:

---

I want to update my Minecraft Fabric mod so it can **add and remove items** from vanilla loot tables via a single configuration file. The mod should stay lightweight and configurable, without replacing entire loot tables.

### Project Scope & Rename

The scope of this project has outgrown its name. The mod now focuses on **rebalancing structure-based loot tables** (chest loot from ancient cities, strongholds, bastions, shipwrecks, etc.). The mod should be **renamed** to reflect this broader scope (e.g. `structure_loot_rebalance` or similar).

The mending modifications currently in `run/config/mending_rebalance/loot-injections.json` are the **first default set** of updates. Default settings will be expanded over time. The new config format should ship with these mending additions as the initial defaults. **Default values must be easy to modify in code** — a clear, centralized structure (e.g. constants, builders, or a dedicated class) so new defaults can be added with minimal friction.

### Goals

1. **Add items** to specified loot tables with configurable weight and quantity.
2. **Remove items** from specified loot tables by matching item and, optionally, other criteria.
3. **Modify** existing loot entries via `exclude_enchantments` — filter enchantments from `enchant_randomly` pools so entries still produce enchanted books/items but never with the excluded enchantments (e.g. exclude mending from high-level tables).
4. **Generate a config** that scaffolds all structure loot table keys with empty `add`, `remove`, and `modify` arrays, then populates the default values (mending additions from the current implementation) into the appropriate tables. Users see a complete template and can extend or override any structure.

### Current Implementation

- Mod uses Fabric `LootTableEvents.MODIFY` to inject items into built-in loot tables.
- Config is at `config/mending_rebalance/loot-injections.json`.
- For enchanted books, the mod builds them in code (e.g. mending) instead of using loot table references, to avoid load-order issues.
- Weight controls chance by adding an empty entry so the item is not guaranteed; higher weight = higher chance.
- Target tables include: ancient city ice box, stronghold library, ancient city, bastion variants, buried treasure, underwater ruins, shipwrecks.

### Configuration Requirements

**Config structure:** Configurations must be **keyed by the target namespaced loot table ID**. Each key is a loot table (e.g. `minecraft:chests/ancient_city_ice_box`); each value is an object containing `add`, `remove`, and/or `modify` arrays. Each array holds a **list of objects** so multiple items can be configured per table.

Complete configuration structure:

> **Disclaimer:** No values in this example should be used in the default values unless they match the first default set. The first default set is defined by `run/config/mending_rebalance/loot-injections.json` (mending book additions only; no diamond, no end city removals or modify). Other example values illustrate the schema for user customization.

```json
{
  "loot_modifications": {
    "minecraft:chests/ancient_city_ice_box": {
      "add": [
        {
          "item": "mending_rebalance:mending_book",
          "weight": 10.0,
          "quantity": [1, 1]
        },
        {
          "item": "minecraft:diamond",
          "weight": 2.0,
          "quantity": [1, 3]
        }
      ],
      "remove": [],
      "modify": []
    },
    "minecraft:chests/stronghold_library": {
      "add": [
        {
          "item": "mending_rebalance:mending_book",
          "weight": 10.0,
          "quantity": [1, 1]
        }
      ],
      "remove": [],
      "modify": []
    },
    "minecraft:chests/end_city_treasure": {
      "add": [],
      "remove": [],
      "modify": [
        {
          "item": "minecraft:enchanted_book",
          "exclude_enchantments": ["minecraft:mending"]
        }
      ]
    }
  }
}
```

**Add** entry object:
- `item`: item or item definition (e.g. `minecraft:diamond` or an enchanted book spec like `{ "type": "enchanted_book", "enchantment": "minecraft:mending" }`)
- `weight`: probability weight (higher = more likely per roll)
- `quantity`: amount given on success (min/max range, e.g. `[1, 1]` or `[1, 3]`)

**Remove** entry object:
- `item`: item to remove (e.g. `minecraft:enchanted_book`)
- Optional matching criteria (all applied together when specified):
  - `enchantment`: match only items with this enchantment (e.g. `minecraft:mending`) — useful to remove only mending books while leaving other enchanted books
  - `enchantment_level`: match by enchantment level or level range (e.g. `1` or `[1, 3]`)
  - `potion` / `effect`: for potions, match by effect type or tier
  - `nbt` / `components`: match on item data (custom name, lore, durability, etc.)
  - `weight`: match loot entries by their weight value (for targeting specific entry variants)

**Modify** entry object (`exclude_enchantments` only for now):
- `item`: item type to target (e.g. `minecraft:enchanted_book` or `minecraft:iron_sword` for enchanted armor/tools)
- `exclude_enchantments`: array of enchantment IDs to remove from the loot entry's `enchant_randomly` pool
- **How it works:** Vanilla loot tables use `enchant_randomly` to pick an enchantment at roll time. This modify finds such entries for the given item and filters the specified enchantments out of the pool. The entry still produces enchanted items, but never with the excluded enchantments. Use case: high-level tables (e.g. end city) can roll mending via `treasure: true`; exclude mending so those sources never produce mending books while still dropping other enchanted books.

**Additional structure notes:**
- The generated default config scaffolds every structure loot table key with empty `add`, `remove`, and `modify` arrays, then populates the mending additions from `loot-injections.json` into the appropriate tables. The plan should define the complete set of structure loot table IDs to include (e.g. all `minecraft:chests/*` tables for structures).
- Empty arrays (`[]`) denote tables with no additions, removals, or modify entries; can be left for tables to tweak later.
- Schema should allow comments or be documented so parameters are clear.

### Other Considerations

- **Enchanted items**: support defining enchanted books (and possibly other enchanted items) in config (enchantment ID, level, etc.) and build them in code to avoid nested loot table load-order issues.
- **Quantity**: support both fixed counts (e.g. `1`) and ranges (e.g. `1–3`) for added items.
- **Probability**: keep the current weight-vs-empty design so weight affects chance; document how weight maps to approximate probability.
- **Removals**: fabric's `MODIFY` only adds; removals may need another approach (e.g. REPLACE for specific tables or a different pattern). Include removal design as part of the plan.
- **Modify (exclude_enchantments)**: during `LootTableEvents.MODIFY`, inspect existing pools/entries for `enchant_randomly` functions; for matching entries, clone the function with an enchantment pool that excludes the specified IDs. If the original uses a tag or "all enchantments," resolve to an explicit list first, then filter.
- **Default values in code**: Defaults should live in a single, well-organized place (e.g. `DefaultLootModifications` class or similar) with type-safe structures—no hardcoded JSON strings. Adding a new structure key or a new addition to an existing table should be a simple code change (e.g. one method call or map entry). This supports iteratively building defaults over time.
- **Validation**: warn or skip invalid entries (unknown item, unknown table, invalid syntax) instead of crashing.
- **Backward compatibility & rename**: when the mod is renamed, the config path changes (e.g. `config/mending_rebalance/` → `config/structure_loot_rebalance/`). If the old `loot-injections.json` exists, migrate it into the new config format. The item `mending_rebalance:mending_book` in config can map to the in-code enchanted book builder; consider using `{ "type": "enchanted_book", "enchantment": "minecraft:mending" }` in the new schema for portability.

Please outline: (1) the mod rename and config migration, (2) the updated config schema, (3) the logic for additions, removals, and modify (exclude_enchantments), and (4) how to handle enchanted items and loot table modification with the current Fabric loot API.
