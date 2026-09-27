# GeoStrata Survival-Readiness TODO

Audit of what's missing/broken for a full survival experience, vs the 1.7.10 original.
Updated as work lands.

Legend: **P1** = blocks survival progression/unobtainable content · **P2** = broken-but-playable · **P3** = polish/parity nit

## Current status (2026-09-24)

The earlier "all cleared except missing art" assessment was wrong. The historical audit notes
below are preserved for context; the historical blocker notes are superseded by this status.

This pass adds datagenerated layered ore models and 32 clipped/recoloured overlay sprites for the
17 x 32 ore matrix, enables the ore creative tab, restores the icy vein's `lowtempdiamonds` loot
(its item PNG was already on disk), adds Jade in place of WAILA, and ports the missing partial
bounds block with its shape/cover/fence/book controls and renderer. It also restores RF crystal
energy capability/growth/output, the arctic and smoke air effects, exposed arctic cold damage,
Nether/End vent placement, configured cross-dimension rock generation, quartz bricks, void-opal
physical properties, common item tags, and the two original village buying offers. Client/server
datagen and compilation pass. A 26.2 development client reached the main menu with Jade loaded and
the generated resources baked. Its first run exposed a missing Jade component translation and a
missing particle texture reference for the partial block; both were fixed and a second run showed
no GeoStrata model warnings beyond the unused steam-animation frames, which were then corrected to
play the full 32-frame loop. A fresh-world
playtest is still required for visual and gameplay confirmation.

Follow-up fixes in the same pass: ore models now put the transparent inclusion and host-rock
mask over an opaque smooth-rock cube, so holes in the source host texture cannot show the world
through the block. The Ores tab contains the complete ore matrix, ordered by rock type, while the
main tab keeps low-temperature diamonds and creepvine seeds first. Vent redstone activation is
restored, and datagenerated dimension variants use End Stone or Netherrack as appropriate. Jade
headings and vent names are capitalized; lava-rock items and the original Low-Temperature
Diamonds item have display names. Lava rock heats its surroundings on random ticks instead of
placement. Partial blocks hide fully occluded shared faces and emit cover-textured break
particles. Player-placed RF crystal branches attach to an adjacent seed or existing branch.
RotaryCraft now registers the original Rotational Dynamo with RF output for testing crystal
charging. The automatic compile and client/server datagen checks pass, and all 15 RotaryCraft
GameTests pass, including a shaft-power-to-RF test for the dynamo. A fresh-world visual and
RF transfer test is still needed.

Known remaining parity and validation work:

- [ ] Visually inspect ore layering (especially opal tint), the partial bounds cover/fence render,
      Jade panels, RF crystal transfer/growth, and arctic air/cold in a client world.
- [ ] Replace recoloured vanilla inclusion patterns with each mod's actual ore art when those mod
      textures are available. 1.7.10 used the source ore icon directly; no ore overlay art shipped.
- [ ] Port the ChromatiCraft ED commodity-price hook when `UATrades` lands in its 26.2 slice. The
      static villager buying offers currently serve as the fallback for all professions.
- [ ] Finish biome painting/packed-ice variants and ice-worm ambience when the original assets
      and modern biome hooks are available. Minecraft 26.2 already renders the air meter on land
      when air is below its maximum, so the old custom HUD replacement is no longer needed.
- [x] Restore `BOXRECIPES`: both original brick layouts are datagenerated with a NeoForge
      condition, and the config selects one when recipes load.
- [ ] Decide whether the port-only `GEOORE`/`RETROGEN` config controls should be exposed while
      their full flows are absent (the shipped 1.7.10 ore-conversion path was disabled).
- [ ] Verify/restore optional RotaryCraft, Chisel and ChromatiCraft integration paths, and decide
      whether void opal's current self-drop should match the original item-less block.

---

## DONE

### P1 — Recipes (`53ab7a0d`)
The mod had **zero** recipes; `GeoRecipeProvider` now emits **1849**, based on
upstream `GeoRecipes.java`:
- Shape matrix, 323 (17 types x 19): brick x4, round x4, fitted x2, tile x4, inscribed x3,
  engraved x4 (both diagonals), connected x8, connected2 x8, etched x3, cubed x9, centered x5,
  lined x5, embossed x3, raised x4, fan x8, spiral x8, mossy x2 (vines), pillar x3
- Smelting, 289: every non-smooth shape back to smooth, 0 XP
- Slabs, 612: 3 -> 6 slabs, and 2 stacked slabs -> 1 block
- Stairs, 612: 4 per 6 blocks, both mirror layouts
- Deco bricks, 12: six `4 * legacy recipeMultiplier` 2x2 recipes and six
  `8 * legacy recipeMultiplier` ring variants (mutually exclusive by `BOXRECIPES`)
- Partial bounds block, 1: 24 blocks from the original iron-bar/stone/plank/stick pattern

Notes: CONNECTED/CONNECTED2 resolve via `connectedBlockMapping` (no stairs/slabs, as upstream);
added `RockShapes.getStair/getSlab`. Recipe ids are foldered to stay distinct from auto-derived
defaults. `BOXRECIPES` now emits both layouts with mutually exclusive load-time conditions.

### P1/P2 — Drops (`bbada3c2`)
- **Vents dropped nothing**: `canHarvestBlock` returned false, suppressing everything (and
  contradicting the dropSelf table). Now cobblestone normally, vent itself on silk touch.
- **Luminous crystal** now drops `luminous_crystal_item_0` (was noDrop).
- **Lava rock** now drops the height-matching `lava_rock_item_0..3` via block-state conditions.
- **RF crystal** now drops 1-6 redstone with a uniform fortune bonus (was dropSelf).

### P2 — Mechanics (`60d46a38`, `1987d00b`, earlier `2197141`)
- **Shearing never worked**: `onSheared` had an extra `fortune` param so it never overrode
  NeoForge's `IShearable` contract. Implemented `isShearable`/`onSheared` with legacy shearAll
  semantics (one vine per occupied face, block removed).
- **CRYSTALSPIKE was not actually missing content**: upstream keyed `spikyFall` on DECOGEN meta 0
  (`Types.CRYSTALSPIKE`), which is precisely what the port registers as `OCEAN_SPIKE`
  (`DecoGenerator.OCEANSPIKE` placed DECOGEN meta 0 — hence its `deco/0` texture). Fall damage
  x1.5 restored with no new block.
- **Game events were dead in singleplayer**: `smokeVentAir` + `spikyFall` were registered only
  under `Dist.DEDICATED_SERVER`. They run on the logical server (which singleplayer also has), so
  they are now registered unconditionally.
- Vent entity effects + ender teleport (fixed earlier, `2197141`).

### P3 — Cleanups (`3557cbb`, `6370b7cc` in DragonAPI)
- Deleted 7 stale hand-written configured/placed-feature JSONs that all **differed** from the
  generated authoritative copies (biomes kept — no generated counterpart).
- `mineable/pickaxe` now covers vents, lava rock, ore veins, ocean spike, icicle, void opals,
  luminous crystal, RF crystals (mining **speed**; they never gated drops on a tool).
- **Item tints**: registered `geostrata:opal` / `geostrata:crystal` `ItemTintSource` codecs and
  emit `tintedModel` for opal blocks/stairs/slabs/ore/connected + the 4 crystal variants.
- **Connected opal now tints in-world**: added an optional `tint` flag to DragonAPI's
  `connected_overlay` codec (bakes tintindex 0; default false so nothing else changes).
- **Lava rock** no longer quenches at worldgen time (conversion now requires a `ServerLevel`,
  restoring the legacy `doingLavaRockGen`/chunk-finished guard).
- **RF crystal seed** drops now carry `activated` via CUSTOM_DATA and restore it in `setPlacedBy`.

---

## Historical blockers (resolved in the 2026-09-24 pass)

### P1 — Ore block textures → ores hidden behind one flag (`60d46a38`)
1.7.10 shipped no separate ore overlay art. `OreRenderer` drew the source ore icon and host rock
in two passes. Its `clipFrom` routine was present but disabled by an unconditional early return.
The 26.2 port instead performs this clipping in client datagen, with a recoloured vanilla pattern
for ores whose source mod texture is not available in the local build.

The full 17-rock × 32-ore (544-block) matrix now has generated block models, item models and
overlay PNGs, and is exposed in the creative tab.

- [x] Rebuild the two-layer scheme and flip `ORES_HAVE_TEXTURES` to true.
- [x] Keep ore conversion worldgen disabled, matching the shipped 1.7.10 path.

### P2 — `lowTempDiamonds` (icy ore vein's signature loot)
- [x] `textures/item/lowtempdiamonds.png` was already present in the workspace. The plain
      64-stack item is registered and the icy vein now yields it.

---

## REMAINING — not blocked, just not done

### P2 — `PARTIAL` block (`BlockPartialBounds`)
- [x] TE-backed utility block with right-click interaction and an icon-delegating renderer
      (`IconDelegateAccess`), crafted x24 (`"BSB"/"SPS"/"gSg"` — iron bars / stone tag / planks /
      stick, BuildCraft wooden gear when present — skip the BC branch). Texture
      `block/partialfencegroove.png` exists. The block, renderer, and recipe now land together;
      in-world visual validation remains on the current-status checklist.

### P3 — Small parity gaps
- [x] **Nether quartz bricks** (`QUARTZBRICKS`) was the one deco brick not registered in the port
      (upstream: 1.2F/5F hardness, multiplier 2, from `Blocks.quartz_block`). Add block + recipe.
- [ ] **Void opal** self-drops; upstream had no item form (null ItemBlock → no drop). Verify the
      END/ChromatiCraft progression doesn't rely on non-obtainability.
- [x] `steam_inside` "unused frames [20-31]" log warning — the PNG has a complete 32-frame loop
      (frame 31 repeats frame 0); animation metadata now plays the full loop.
- [ ] DragonAPI `@OnlyIn` load warning — informational; NeoForge no longer strips members. Cleanup
      is mod-wide and cosmetic.

---

## Needs in-game verification (cannot be confirmed from the build)

Accumulated across recent fixes — worth one pass in a fresh world:
1. Stairs/slabs show rock textures; **recipes** appear in the recipe book / craft correctly.
2. Vents: textures + animated inside face, drop cobble (vent on silk), erupt-and-plug explosion,
   **ender teleport** (stand near an *erupting* ender vent), gas poison, pyro ignite.
3. Lava rock: quenches **only** next to water, drops its height variant.
4. Ocean spike (BER) renders; **landing on one deals 1.5x fall damage**.
5. Icicle renders as a spike (DynamicBlockStateModel), not a cube.
6. Opal rainbow: world blocks, stairs/slabs, **connected** opal, and **items in inventory**.
7. Luminous crystal renders + drops; glowing vines shear into vine items.
8. Ore tab: a mod-provided metal shows with the generated inclusion overlay and host-rock texture.
