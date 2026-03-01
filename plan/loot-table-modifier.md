# Mending Rebalance — Loot Table Modifier Plan

## Context

Mending should be predictably accessible in end-game content rather than relying on villager RNG. This plan covers adding mending enchantment books to structure chest loot tables.

## Goal

- **Establish end-game as primary source**: Add mending to ancient cities, ancient city center, and bastion remnants (end cities and end ships excluded — too easy to exploit with elytra).
- **Add rare alternatives**: Include mending in treasure chests, stronghold libraries, shipwrecks, and underwater ruins with smaller chances — enough to reward exploration without undermining end-game as the main path.

---

## Implementation Approach

### Concept

Inject mending enchantment books into chest loot tables via loot table modifiers. Each structure has its own configurable weight. Ancient city center and stronghold libraries are the primary sources; ancient city outer chests and bastion remnants offer mid-tier chances; treasure chests, shipwrecks, and underwater ruins offer rare alternatives.

### Config-Driven Design

The loot table modifier is **config-driven**. A JSON config at `config/mending_rebalance/loot-injections.json` defines all injections. If the file does not exist, the mod writes a default config (cloned from the vanilla plan below).

**Config schema** (`loot-injections.json`):

```json
{
  "entries": [
    {
      "item": "namespace:path",
      "weight": 10,
      "loot_table": "namespace:path"
    }
  ]
}
```

| Field | Description |
|-------|-------------|
| `item` | The item or loot table to inject, by namespace and path. Use a direct item (e.g. `minecraft:diamond`) or a loot table reference (e.g. `mending_rebalance:mending_book`) for complex items like enchanted books. |
| `weight` | Probability scaler — higher values increase the chance of finding the item in a given chest. Can be fractional (e.g. 0.5, 0.25). |
| `loot_table` | Target loot table by namespace and path (e.g. `minecraft:chests/ancient_city_ice_box`). The item is injected into this loot table. |

The mod resolves `item` as follows: if it is a registered item, it adds an item entry; otherwise it treats it as a loot table reference (e.g. for enchanted books with specific enchantments).

### Target Structures and Weights (Default Config)

(Relative to pool total; higher = more likely per roll)

| Structure | Weight | Role |
|-----------|--------|------|
| Ancient city center | 10 | Primary — rewards venturing to the deep dark center |
| Stronghold library | 10 | Primary — signals mending exists, rewards stronghold exploration |
| Ancient city | 2 | Mid-tier — exploration reward in deep dark |
| Bastion remnant | 2 | Mid-tier — Nether end-game content |
| Treasure chest | 1 | Rare bonus — does not undermine end-game |
| Underwater ruins | 0.5 | Very rare — exploration reward |
| Shipwreck | 0.25 | Very rare — exploration reward |

**Excluded**: End cities and end ships — too easy to exploit once the player has elytra.

### Considerations

- Loot tables should be modified (not replaced) so vanilla loot is preserved.
- Use Fabric loot table modifiers to append mending entries to the relevant structure chest loot tables.

---

## Design Principles

- **Predictability**: Players who complete end-game content should have a clear path to mending.
- **Progression**: Mending becomes a reward for late-game exploration.
- **Chance gradient**: End-game structures are the reliable path; everything else is a rare bonus.
