# Structure Loot Rebalance

A Fabric mod that gives explorers a **tool enchantment progression path** complementary to combat-focused mods. Level I mining enchants come from structure chests via **independent multi-pool rolls** (primary + optional Mending).

**Design docs:** [`plan/pool-model-v4.md`](plan/pool-model-v4.md) · [`plan/multi-pool-structure-clear.md`](plan/multi-pool-structure-clear.md)

## Features

- **Primary pool (all tiers):** Efficiency I, Unbreaking I, Protection I — themed structures add signature enchants at **2× weight** in the same roll
- **Tier 2:** **2 primary rolls / chest** at **4/3× tier-1 per-roll odds** (~6.7 books / clear)
- **Tier 3:** **3 primary rolls / chest** at **4/3× tier-1 per-roll odds** (~10 books / clear)
- **Tier 3:** adds Mending I bonus pool (where enabled)
- **Fortune I** — **`fortune` theme on buried treasure only** (~50% chance per find)
- **Feather Falling** and **Silk Touch** — villager-only (not structure loot)
- **Shipwreck** — `shipwreck_treasure` only; Tier 1 + underwater theme (~1 book / 4 ships)
- **End loot untouched**; ominous trial vaults excluded

### Structure-clear expectations

Typical books per **full structure clear** — not guaranteed; variance is fine.

| Tier | Primary | Mending |
|------|---------|---------|
| 1 | **~2.5 books** | — |
| 2 | **~6.7 books** (2 rolls, 4/3× tier-1 per-roll rate) | — |
| 3 | **~10.0 books** (3 rolls, 4/3× tier-1 per-roll rate) | **~0.75 Mending** / clear (independent roll) |

Ancient city: **~0.75 Mending** from outer chests + **~0.75** from ice box budget (~**1.5** / clear typical). Mending never reduces primary roll count.

Buried treasure: **~0.5 books / find** (fortune-focused primary pool).

Each pool rolls **independently** per chest. Rates use `expected ÷ n`; Mending uses P(at least one) per clear.

Enchanted **player armor** and **tools** from ancient city, bastion chests, and trial vault rare loot is **50% less common** — other vanilla loot unchanged. **Silk Touch** never drops from mod tables (librarian exclusive).

## Configuration

Config file: `config/structure_loot_rebalance/loot-modifications.json`

Config **v5** schema:

```json
{
  "schema_version": 7,
  "loot_modifications": {
    "minecraft:chests/shipwreck_treasure": {
      "tier": 1,
      "structure_id": "shipwreck",
      "structure_loot_events": 4,
      "mending_enabled": false,
      "theme": "underwater"
    },
    "minecraft:chests/buried_treasure": {
      "tier": 1,
      "structure_id": "buried_treasure",
      "structure_loot_events": 1,
      "mending_enabled": false,
      "rate_multiplier": 0.2,
      "base_weights": { "minecraft:fortune": 1 },
      "theme": "fortune"
    }
  }
}
```

Optional: `rate_multiplier`, `theme_weight_multiplier` (default 2), `base_weights` override.

Legacy configs without `"schema_version": 7` are replaced with code defaults on load.

### Mineshaft chest density

Vanilla mineshafts only roll chest minecarts at **1-in-100** per corridor section (twice per section), so most shafts have 1–2 chests. This mod raises that via `config/structure_loot_rebalance/worldgen.json`:

```json
{
  "mineshaft_chest_roll_bound": 20
}
```

Lower bound → more chests (`20` ≈ 5% per roll, ~5–6 chests per typical shaft). Only **newly generated** chunks use the new rate.

## Compatibility

- **Minecraft:** 1.21.11
- **Fabric Loader:** ≥ 0.19.3
- **Fabric API:** Required
- **Java:** 21+

## Build & test

```bash
./gradlew build    # compile + unit tests
./gradlew test     # unit tests only
```

The built JAR is in `build/libs/structure_loot_rebalance-<version>.jar`.

## Manual UAT

See [`plan/testing-checklist.md`](plan/testing-checklist.md) for AC-1 through AC-4.

## Releases and Modrinth

First release target: **`1.21.11-1.0.0`**

Publishing uses [`.github/workflows/publish.yml`](.github/workflows/publish.yml) on GitHub Release. Requires repo variables `MODRINTH_ID` and secret `MODRINTH_TOKEN`. Changelog file: `releases/{minecraft_version}-{mod_version}.md` (see [`releases/TEMPLATE.md`](releases/TEMPLATE.md)).

## Branch policy

Each supported Minecraft version has its own branch (e.g. `1.21.11`). Version branches are the source of truth.

```bash
git clone https://github.com/spicato-minecraft/structure_loot_rebalance.git
cd structure_loot_rebalance
git checkout 1.21.11
```
