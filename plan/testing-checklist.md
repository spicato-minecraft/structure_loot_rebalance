# Structure Loot Rebalance — Manual UAT Checklist

Run with `./gradlew runClient`. **Design reference:** [`pool-model-v4.md`](pool-model-v4.md)

---

## AC-1 — Tier 1 (primary only, no Mending)

| # | Test | Command | Expected | Pass |
|---|------|---------|----------|------|
| 1.1 | Mineshaft | `/loot give @s loot minecraft:chests/abandoned_mineshaft` ×20 | Eff / Unb / Prot I mod books; **no** Mending; **no** mod Fortune | ☐ |
| 1.2 | Pillager outpost | `/loot give @s loot minecraft:chests/pillager_outpost` ×20 | Same pool as 1.1; low‑n structures add **extra primary rolls** until per-roll odds stay ≤100% (outpost ≈3 rolls/chest) | ☐ |
| 1.3 | Village temple (rare) | `/loot give @s loot minecraft:chests/village_temple` ×30 | Mod books **very rare** (~0.6/clear); no Mending | ☐ |
| 1.4 | Shipwreck treasure | `/loot give @s loot minecraft:chests/shipwreck_treasure` ×20 | Underwater theme (Respiration / Depth Strider); supply/map **vanilla** | ☐ |
| 1.5 | Buried treasure | `/loot give @s loot minecraft:chests/buried_treasure` ×20 | **Fortune I** mod books (~50%); primary Fortune path — find more maps | ☐ |

---

## AC-2 — Tier 2 (2 primary rolls/chest, optional Mending)

| # | Test | Command | Expected | Pass |
|---|------|---------|----------|------|
| 2.1 | Desert pyramid | `/loot give @s loot minecraft:chests/desert_pyramid` ×20 | Eff / Unb / Prot I; up to **2** mod books/chest; **no** Mending | ☐ |
| 2.2 | Jungle temple | `/loot give @s loot minecraft:chests/jungle_temple` ×20 | Same tier‑2 pool; low **n** adds extra rolls (jungle ≈4 primary rolls/chest) | ☐ |
| 2.3 | Ancient city (outer) | `/loot give @s loot minecraft:chests/ancient_city` ×30 | Swift Sneak + base; **Mending I** independent roll (~0.75/clear, n≈18); full city + ice box **~1.5 Mending** typical | ☐ |
| 2.4 | Nether fortress | `/loot give @s loot minecraft:chests/nether_bridge` ×20 | Soul Speed + base; **2** primary rolls; **Mending** (~0.5/clear at n≈8, ~6.25%/chest) | ☐ |
| 2.5 | Trial supply | `/loot give @s loot minecraft:chests/trial_chambers/supply` ×30 | Tier **2** primary only (~6.7/clear); **no** Mending | ☐ |
| 2.6 | Trial corridor | `/loot give @s loot minecraft:chests/trial_chambers/corridor` ×30 | Same as 2.5 | ☐ |

---

## AC-3 — Tier 3 (3 primary rolls/chest + optional Mending)

| # | Test | Command | Expected | Pass |
|---|------|---------|----------|------|
| 3.1 | Stronghold library | `/loot give @s loot minecraft:chests/stronghold_library` ×30 | Up to **3** primary + **Mending** roll/chest (~0.75 Mending/clear) | ☐ |
| 3.2 | Ice box | `/loot give @s loot minecraft:chests/ancient_city_ice_box` ×20 | Tier 3 primary + **Mending** (~0.75/clear); vanilla gear halved | ☐ |
| 3.3 | Woodland mansion | `/loot give @s loot minecraft:chests/woodland_mansion` ×30 | Tier 3 primary + boosted **Mending** (~1.25/clear) | ☐ |
| 3.4 | Bastion treasure | `/loot give @s loot minecraft:chests/bastion_treasure` ×20 | Soul Speed theme + Mending; vanilla gear halved | ☐ |
| 3.5 | Trial reward vault | `/loot give @s loot minecraft:chests/trial_chambers/reward` ×30 | Tier 3 primary (~10/clear) + **Mending** (~0.75/clear) | ☐ |

---

## AC-4 — Excluded / vanilla tables

| # | Test | Command | Expected | Pass |
|---|------|---------|----------|------|
| 4.1 | End city | `/loot give @s loot minecraft:chests/end_city_treasure` ×10 | Vanilla only; no mod books | ☐ |
| 4.2 | Ominous vault | `/loot give @s loot minecraft:chests/trial_chambers/reward_ominous` ×10 | Vanilla only (not modded) | ☐ |
| 4.3 | Shipwreck supply | `/loot give @s loot minecraft:chests/shipwreck_supply` ×10 | Vanilla books possible; no mod strip | ☐ |
| 4.4 | Village armorer | `/loot give @s loot minecraft:chests/village/village_armorer` ×10 | Vanilla only | ☐ |

---

## AC-5 — Cross-mod rules (with VTR)

| # | Test | Expected | Pass |
|---|------|----------|------|
| 5.1 | Librarian trades | No Mending, Fortune, Respiration, Depth Strider, Swift Sneak, Soul Speed | ☐ |
| 5.2 | Librarian trades | Silk Touch, Feather Falling, Eff/Unb/Prot **available** | ☐ |
| 5.3 | Mod book tag | `/data get entity @s SelectedItem` on mod book shows `structure_loot_rebalance` marker | ☐ |

---

## Startup smoke

| # | Test | Expected | Pass |
|---|------|----------|------|
| S.1 | Game launches | No crash | ☐ |
| S.2 | Config | `loot-modifications.json` with `"schema_version": 7` | ☐ |
| S.3 | Log | Registered loot table count ≈ 24 | ☐ |

---

## Notes

- **Primary, Mending, and (buried) Fortune are separate pools** — they never compete; Tier 3 chests can drop 3 primary + 1 Mending.
- **Fortune I** comes from **buried treasure** only (structure loot); upgrade via villagers/anvil elsewhere.
- **Vanilla enchanted armor/tools** drop at **50%** rate on **all modded** structure chests.
- **Vanilla enchanted books** are stripped from modded tables.
- **Low‑n structures** (few chests per clear) get **extra primary rolls per chest** so tier targets still land without exceeding 100% per roll.
- **Silk Touch** never drops from mod tables (librarian-only via VTR).
