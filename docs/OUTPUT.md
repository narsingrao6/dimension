# Dimension Keys — Crystal Caverns

A Fabric mod for Minecraft 26.2 that adds the **Crystal Caverns**, a fully enclosed underground dimension: a gigantic, naturally carved crystal cavern with a very high rock ceiling, rolling multi-level floors full of potholes, huge cliff steps, rising mountain masses, full-height crystal pillars that brace the ceiling, hanging cliff rock, and rare floating islands and ledges — plus a **Crystal Portal** you open with a **Crystal Caverns Key**.

## What the mod does

- Craft/collect Crystal Fragments and the Ancient Crystal Relic, forge the **Crystal Caverns Key**.
- Build a Crystal Portal frame (no corner blocks required) and right-click it with the key to open the portal and travel to the Crystal Caverns.
- Explore a hand-built underground dimension: solid bedrock floor and roof, a very tall open cavern, giant floor-to-ceiling crystal pillars, hanging cliff masses, multi-level floors pitted with potholes and deep sinkholes, rising mountains, and rare floating islands/ledges.

## Components

### Blocks (`block/` and `block/custom/`)
| Block | Class | Notes |
| --- | --- | --- |
| Crystal Ore | `CrystalOreBlock` | Drops crystal fragments, has its own block entity for particle effects. |
| Crystal Block | `CrystalBlock` | Compressed crystal, shimmering electric particles. |
| Crystal Stone | `CrystalStoneBlock` | The dimension's default stone. |
| Ancient Crystal Rock | `AncientCrystalRockBlock` | Darker host rock, generates in formations. |
| Luminous Crystal | `LuminousCrystalBlock` | Light level 15, ambient glow. |
| Prism Crystal | `PrismCrystalBlock` | Special crystal, light level 15, throws rainbow dust rings + sparkles. |
| Crystal Portal | `CrystalPortalBlock` | Frame/activation only, no placeable item (like the Nether Portal). |

### Items (`item/`)
Crystal Fragment, Ancient Crystal Relic, Crystal Caverns Key, plus BlockItems for every block except the portal. Registered in `registry/ModItems.java`; blocks are added to the BUILDING_BLOCKS creative tab.

### Portal system (`portal/`, `dimension/`)
- `PortalFrameDetector` / `CrystalPortalShape` / `CrystalPortalManager` — find a frame, validate it (no corners required), light it, teleport.
- `CrystalTeleporter` / `CrystalPortalTeleporter` — spawn handling in the destination.
- `CrystalDimensions` / `CrystalDimensionBootstrap` — bootstrap the dimension and its type.

### World generation
- `world/generation/ModWorldGeneration` — intentionally empty; decoration is done via the biome's own `features` list (data-pack-native).
- `world/feature/CrystalVeinFeature` + config — programmatic vein placement.
- Density functions under `data/dimension-keys/worldgen/density_function/`, combined by `final_shape` (union via `max`, plus a small `roughness` detail noise so no surface is ever flat):
  - `terrain` — the cavern floors, walls, cliffs, and ledges. A `y_clamped_gradient` plus three noise layers (a huge `cavern_region` basin/plateau noise, medium rolling hills, and a fine rocky cliff noise) creates floors that wander from deep bedrock basins up to high plateaus, so chambers sit at very different heights with enormous cliff steps between them. The whole thing is then `min`-ed against a carve that punches **potholes** (medium bowls) and **large sinkholes** (deep shafts that can punch clean through the floor and open into the void below) into the surface.
  - `ceiling` — the high cavern roof. A `y_clamped_gradient` raised so the ceiling underside sits around y255–295 (bedrock roof stays at the world top), wobbled by the same `cavern_region` + detail noise so it stays correlated with the floor: tall chambers, taller than vanilla caves.
  - `pillars` — **giant floor-to-ceiling crystal pillars**. A single low-octave 2D noise above a high threshold picks sparse, enormous column locations; a slow 3D width-wobble tapers/bulges each column so they are never identical, but they always stay solid from floor to ceiling and visibly brace the roof.
  - `mountains` — **mountain ranges rising from the floor**. A large region mask selects ~a quarter of the map; inside those regions an organic 3D rock-noise mass rises up to ~y200 with cliff faces and spires.
  - `hanging_cliffs` — rough jagged rock (`jagged` × `hanging_mask`) hugging the ceiling plus `hanging_columns` (big cliff masses hanging from the roof down to ~y160).
  - `islands` — rare floating rock blobs suspended in the void (region-gated so they feel special).
  - `ledges` — uncommon horizontal rock plates/bridges floating in the open space.
  - `roughness` — fine surface detail so walls, floors, and pillars are never perfectly flat.
  - `final_shape` — combines ceiling, terrain, mountains, pillars, hanging cliffs, islands, and ledges into one enclosed, mostly-open cavern system.

### Crystal decoration (`worldgen/configured_feature` + `placed_feature`)
Spread through the Crystal Caverns biome's feature list:
- `crystal_ore_placed_deep` / `crystal_ore_placed_caverns` — normal ore across the full cavern height.
- `crystal_block_formation_placed` + `crystal_block_spread_placed` — normal crystal blocks across the whole dimension.
- `luminous_crystal_cluster_placed` + `luminous_crystal_spread_placed` — glowing crystals, the spread variant favoring the upper caverns.
- `hanging_crystal_placed` — glowing crystals concentrated on the high ceiling/roof region so ceilings sparkle.
- `giant_crystal_spire_placed` — rare giant spires (converts pillar/rock cores into glowing crystal).
- `ancient_rock_formation_placed` — dark rock patches.
- `prism_crystal_cluster_placed` — the special Prism Crystal in clusters everywhere.

## Registration map

- Blocks: `registry/ModBlocks.java` (uses `ResourceKey` + `Registry.register`).
- Items: `registry/ModItems.java` (+ creative-tab hooks).
- Block entities: `registry/ModBlockEntities.java`.
- Dimension: `dimension/CrystalDimensionBootstrap` + `data/dimension-keys/dimension_type/crystal_caverns.json`.

## Assets

- Every block has `blockstates/`, `models/block/`, `models/item/`, `items/` (1.21+ item definitions), and a texture under `textures/block/`.
- Lang: `assets/dimension-keys/lang/en_us.json`.
- Loot tables under `data/dimension-keys/loot_table/blocks/`.
- Mining tags: all crystal blocks are in `mineable/pickaxe`.

## Build

```bash
./gradlew build
```
