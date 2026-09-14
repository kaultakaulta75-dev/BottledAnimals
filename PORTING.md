# Porting notes

The Forge 1.7.10 implementation needs a feature-by-feature rewrite. Registries,
block entities, NBT handling, GUIs, fluids and Redstone Flux APIs all changed.

## Compatibility decisions

- Keep the original mod id: `bottledanimals`.
- Use NeoForge capabilities and Forge Energy instead of bundled CoFH RF classes.
- Use data components for animal identity and state on item stacks.
- Store entity type identifiers as registry resource locations.
- Start with vanilla farm animals; add data-driven modded-animal support later.
- Preserve gameplay timings initially, then rebalance after functional parity.

## Milestone 1

This branch establishes a buildable base and registers the original item set.
Items are placeholders until animal data and machines are implemented.
