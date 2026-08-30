# Simplified Pool Model (v3)

**Status:** Implemented

## Mental model

Every modded chest rolls up to **three independent pools**:

1. **Primary** — Efficiency, Unbreaking, Protection I (+ themed enchants at **2× weight** when `theme` is set)
2. **Fortune** — Tier 2+ only
3. **Mending** — Tier 3 + `mending_enabled` only

Tier never changes *which* primary enchants exist. Feather Falling is **villager-only** (not in structure primary pool).

## Rate formula

```
primary_rate = tier_primary_expected ÷ n × rate_multiplier
fortune_rate = fortune_expected ÷ n × rate_multiplier
mending_rate = 1 − (1 − 0.50)^(1/n) × rate_multiplier   (tier 3 + enabled)
```

## Themes (`theme` in config)

| ID | Enchants (2× weight in primary) | Structures |
|----|----------------------------------|------------|
| `underwater` | Respiration, Depth Strider | Shipwreck treasure, buried treasure, underwater ruins |
| `swift_sneak` | Swift Sneak | Ancient city |
| `soul_speed` | Soul Speed | Nether fortress, bastion |

## Config v3 example

```json
{
  "loot_modifications": {
    "minecraft:chests/shipwreck_treasure": {
      "tier": 1,
      "structure_id": "shipwreck",
      "structure_loot_events": 4,
      "mending_enabled": false,
      "theme": "underwater"
    }
  }
}
```

Legacy v1/v2 configs (with `add` or `theme_rate`/`theme_weights`) are replaced with code defaults on load.

## Shipwreck

Only `shipwreck_treasure` is modded. Supply/map chests are vanilla for mod books.
