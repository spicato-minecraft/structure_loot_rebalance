# Structure Loot Rebalance

A Fabric mod that rebalances structure-based loot tables (chest loot from ancient cities, strongholds, bastions, shipwrecks, etc.). Add, remove, or modify loot via a single configuration file without replacing entire tables.

## Features

- **Add items** to specified loot tables with configurable weight and quantity
- **Remove items** from loot tables by matching item and optional criteria (enchantment, level)
- **Modify** existing entries via `exclude_enchantments`—filter enchantments from `enchant_randomly` pools so entries never produce the excluded enchantments (e.g. exclude mending from end city)

## Configuration

Config file: `config/structure_loot_rebalance/loot-modifications.json`

On first run, a default config is generated with all structure loot table keys scaffolded and mending book additions pre-populated. Edit the file to customize.

## Compatibility

- **Minecraft:** 1.21.x
- **Fabric Loader:** ≥ 0.18.4
- **Fabric API:** Required
- **Java:** 21+

## Build

```bash
./gradlew build
```

The built JAR will be in `build/libs/` as `structure_loot_rebalance-<version>.jar`.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) and [Fabric API](https://modrinth.com/mod/fabric-api) for Minecraft 1.21.x.
2. Copy the built JAR from `build/libs/` to your Minecraft `mods` folder.
3. Launch the game.

## Branch Policy

Each supported Minecraft version has its own long-lived branch named after the exact `minecraft_version` in `gradle.properties` (e.g. `1.21.10`). Version branches are the source of truth; `main` is not used.

- **Default branch:** latest supported version (`1.21.10`)
- **Feature/fix work:** branch from the target version as `{version}/feature-name`, merge back into the version branch
- **New MC version:** create `{version}` from the prior version branch, bump `gradle.properties`, push to origin
- **Retiring a version:** tag the final release, then archive the branch (do not delete)

```bash
git clone https://github.com/spicato-spicato/structure_loot_rebalance.git
cd structure_loot_rebalance
git checkout 1.21.10
```
