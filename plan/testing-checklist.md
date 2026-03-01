# Mending Rebalance — Testing Checklist

A manual testing checklist for core mod functionality and playability. Run the mod with `./gradlew runClient` before testing.

**Interactive web app:** Open `plan/testing/index.html` in a browser for a checkable checklist with copy-to-clipboard for commands and progress tracking.

---

## 1. Startup & Load

| # | Test | Expected | Pass |
|---|------|----------|------|
| 1.1 | Launch game with mod | Game starts without crash | ☐ |
| 1.2 | Check latest.log | Line contains `Registered N loot table injection entries` (N > 0) | ☐ |
| 1.3 | Check latest.log | No `ERROR` or `WARN` related to mending_rebalance config | ☐ |

**Log location**: `run/logs/latest.log`

---

## 2. Config

| # | Test | Steps | Expected | Pass |
|---|------|-------|----------|------|
| 2.1 | Default config created | Delete `run/config/mending_rebalance/loot-injections.json` if it exists. Launch game. | File is created at `run/config/mending_rebalance/loot-injections.json` | ☐ |
| 2.2 | Config structure | Open `loot-injections.json` | Valid JSON with `entries` array; each entry has `item`, `weight`, `loot_table` | ☐ |
| 2.3 | Invalid config fallback | Corrupt config (e.g. add `"invalid":` or remove a bracket). Launch game. | Game loads; log shows "Failed to load loot injection config, using defaults"; mod uses built-in defaults | ☐ |
| 2.4 | Empty entries | Set `"entries": []` in config. Launch game. | Game loads; no crash; no mending in chests (see 3.x) | ☐ |

---

## 3. Loot Injection

Use these commands to spawn chests or give loot. Loot is generated when the chest is first opened or when `/loot give` runs.

### Commands Reference

```
# Give loot from a loot table directly (instant, no chest)
/loot give @s loot minecraft:chests/<loot_table_id>

# Place a chest with a specific loot table (loot generated on first open)
/setblock ~ ~ ~ chest{LootTable:"minecraft:chests/<loot_table_id>"} replace

# Common loot table IDs from default config:
# ancient_city_ice_box, stronghold_library, ancient_city
# bastion_treasure, bastion_other, bastion_bridge, bastion_hoglin_stable
# buried_treasure, underwater_ruin_big, underwater_ruin_small
# shipwreck_treasure, shipwreck_supply, shipwreck_map
```

### Primary Sources (weight 10)

| # | Test | Command | Expected | Pass |
|---|------|---------|----------|------|
| 3.1 | Ancient city ice box | `/loot give @s loot minecraft:chests/ancient_city_ice_box` (run 5–10 times) | At least one **Mending** enchanted book in results | ☐ |
| 3.2 | Stronghold library | `/loot give @s loot minecraft:chests/stronghold_library` (run 5–10 times) | At least one **Mending** enchanted book in results | ☐ |

### Mid-Tier Sources (weight 2)

| # | Test | Command | Expected | Pass |
|---|------|---------|----------|------|
| 3.3 | Ancient city | `/loot give @s loot minecraft:chests/ancient_city` (run 5–10 times) | Occasionally a **Mending** enchanted book | ☐ |
| 3.4 | Bastion treasure | `/loot give @s loot minecraft:chests/bastion_treasure` (run 5–10 times) | Occasionally a **Mending** enchanted book | ☐ |

### Rare Sources (weight 0.25–1)

| # | Test | Command | Expected | Pass |
|---|------|---------|----------|------|
| 3.5 | Buried treasure | `/loot give @s loot minecraft:chests/buried_treasure` (run 10+ times) | Rarely a **Mending** enchanted book | ☐ |
| 3.6 | Shipwreck | `/loot give @s loot minecraft:chests/shipwreck_treasure` (run 10+ times) | Very rarely a **Mending** enchanted book | ☐ |

### Excluded (no mending)

| # | Test | Command | Expected | Pass |
|---|------|---------|----------|------|
| 3.7 | End city | `/loot give @s loot minecraft:chests/end_city_treasure` (run a few times) | No mending books (vanilla behavior; not in config) | ☐ |

---

## 4. Config Customization

| # | Test | Steps | Expected | Pass |
|---|------|-------|----------|------|
| 4.1 | Add custom item | Add entry: `{"item":"minecraft:diamond","weight":5,"loot_table":"minecraft:chests/simple_dungeon"}`. Restart game. | `/loot give @s loot minecraft:chests/simple_dungeon` sometimes yields diamonds | ☐ |
| 4.2 | Disable injection | Remove all entries or set `"entries":[]`. Restart. | No mending in any structure chests | ☐ |
| 4.3 | Change weight | Set stronghold_library weight to `100`. Restart. | Mending appears much more often in stronghold library loot | ☐ |

---

## 5. Vanilla Loot Preservation

| # | Test | Steps | Expected | Pass |
|---|------|-------|----------|------|
| 5.1 | Stronghold library variety | `/loot give @s loot minecraft:chests/stronghold_library` (run 3–5 times) | Still contains books, paper, maps, compasses, etc. (vanilla loot intact) | ☐ |
| 5.2 | Bastion treasure variety | `/loot give @s loot minecraft:chests/bastion_treasure` (run 3–5 times) | Still contains netherite scrap, gold, diamond gear, etc. | ☐ |

---

## 6. Playability

| # | Test | Steps | Expected | Pass |
|---|------|-------|----------|------|
| 6.1 | Create new world | Main menu → Singleplayer → Create New World → Create | World creates and loads | ☐ |
| 6.2 | Join world | Enter world, move around | No lag spikes or crashes | ☐ |
| 6.3 | Find structure chest | Locate stronghold library or ancient city; open chest | Chest opens; loot generates; may include mending book | ☐ |
| 6.4 | Mending book usability | Obtain mending book; use on anvil with tool/armor | Enchantment applies correctly | ☐ |

---

## 7. Edge Cases

| # | Test | Steps | Expected | Pass |
|---|------|-------|----------|------|
| 7.1 | Missing config dir | Delete entire `run/config/mending_rebalance/` folder. Launch. | Config dir and file recreated; mod loads | ☐ |
| 7.2 | Malformed JSON | In config, change `"weight": 10` to `"weight": "ten"`. Launch. | Fallback to defaults; no crash | ☐ |
| 7.3 | Invalid loot table ID | Add entry with `"loot_table":"minecraft:chests/nonexistent"`. Launch. | Mod loads; invalid entry ignored or no crash | ☐ |

---

## Quick Smoke Test (minimal)

If time is limited, run at least:

1. **1.1** — Game launches
2. **2.1** — Config is created
3. **3.1** or **3.2** — Mending appears in primary source loot
4. **6.1** — New world creates
5. **6.3** — Structure chest opens and yields loot

---

## Notes

- **Villager trades**: The villager trade modifier (removing mending from librarians) is planned but not yet implemented. Librarians can still offer mending in the current version.
- **Config path**: In development, config is at `run/config/mending_rebalance/loot-injections.json`. In production, it is `config/mending_rebalance/loot-injections.json` next to the game directory.
- **Loot seed**: `/loot give` uses a random seed each time. Run commands multiple times to observe probability.
