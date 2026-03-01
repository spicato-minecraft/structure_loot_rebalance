# Mending Rebalance — Villager Trade Modifier Plan

## Context

Mending is too easily obtainable by trading with librarian villagers, which undermines its value as a late-game enchantment. This plan covers removing mending from the librarian villager trade pool via configuration.

## Goal

- **Remove villager access**: Exclude mending from librarian villager trades entirely, configurable so it can be toggled or customized.

---

## Implementation Approach

### Concept

Modify tradable villagers' trade tables (the set of possible trades they might have) via a config such that mending can be removed from the librarian villager trade pool. The change should apply to both newly created librarians and any existing trade-cycling behavior.

### Config-Driven Behavior

- Config should allow removing mending from the librarian enchantment book trade pool.
- Other enchantment trades should remain unchanged.
- Librarians generate trades from a pool of enchantments; mending must be excluded from that pool when the config is enabled.

### Considerations

- The modification should intercept or alter the librarian trade generation so mending is never offered as an enchantment book trade.
- Other villager types and their trades should be unaffected.
- Config flexibility: consider allowing re-enabling mending for players who prefer vanilla behavior.

---

## Design Principles

- **Minimal invasiveness**: Modify only the librarian enchantment trade pool; leave other game systems untouched.
- **Configurability**: Use config to control whether mending is excluded, allowing server admins and players to tune the experience.
