# Pool Model v4–v6

**Status:** Implemented

## Mental model

Every modded chest rolls **independent pools** that never compete:

1. **Primary** — Efficiency, Unbreaking, Protection I (+ themed enchants at **2× weight** when `theme` is set)
2. **Mending** — separate per-chest roll when `mending_enabled` (does not consume primary rolls)

**Tier meaning:**

| Tier | Primary books / clear | Rolls / chest | Per-roll vs tier 1 | Mending |
|------|----------------------|---------------|--------------------|---------|
| 1 | **~2.5** | 1 (2 if n≤2) | budget ÷ (n×rolls) | — |
| 2 | **~6.7** | **2** | budget ÷ (n×rolls) | optional (`mending_expected`) |
| 3 | **~10.0** | **3** | budget ÷ (n×rolls) | optional (default **~0.75** / clear) |

Fortune is **not** a tier-wide bonus pool. It appears only via the **`fortune` theme** on **buried treasure** — find more maps to hunt Fortune I.

### Mending targets (v7)

| Structure | Mending / clear | Notes |
|-----------|-----------------|-------|
| Ancient city (outer) | **~0.75** | Tier 2, n≈18 outer chests / clear |
| Ancient city ice box | **~0.75** | Tier 3 bonus room |
| Woodland mansion | **~1.25** | Boosted for clear effort |
| Nether fortress | **~0.5** | Tier 2; n≈8 chests / clear (~6.25%/chest) |
| Trial reward / other T3 | **~0.75** | default |

Trial **supply/corridor** are Tier **2** primary (~6.7/clear each); reward stays Tier 3.

## Rate formula

```
primary_expected = tier budget × rate_multiplier
primary_per_roll = primary_expected ÷ (n × primary_rolls_per_chest)
primary_rolls_per_chest = 2 when tier 1 and n ≤ 2, else tier default
mending_per_chest = mending_expected ÷ n × rate_multiplier
```

## Themes

| ID | Enchants (2× weight in primary) | Structures |
|----|----------------------------------|------------|
| `underwater` | Respiration, Depth Strider | Shipwreck treasure, underwater ruins |
| `swift_sneak` | Swift Sneak | Ancient city |
| `soul_speed` | Soul Speed | Nether fortress, bastion |
| `fortune` | Fortune | Buried treasure |

Vanilla enchanted **armor/tools** are halved on **all** modded structure chests.

## Config v7

Root `schema_version: 7`. Legacy configs without it are replaced with code defaults on load.

```json
{
  "schema_version": 7,
  "loot_modifications": {
    "minecraft:chests/ancient_city": {
      "tier": 2,
      "structure_id": "ancient_city",
      "structure_loot_events": 18,
      "mending_enabled": true,
      "mending_expected": 0.75,
      "theme": "swift_sneak"
    }
  }
}
```
