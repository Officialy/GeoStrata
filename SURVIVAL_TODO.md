# GeoStrata Survival-Readiness TODO

Audit of what's missing/broken for a full survival experience, vs the 1.7.10 original.
Updated as work lands.

Legend: **P1** = blocks survival progression/unobtainable content · **P2** = broken-but-playable · **P3** = polish/parity nit

## Status: P1, P2 and P3 all cleared except two items blocked on missing art

Everything below is either **DONE** or explicitly **BLOCKED** with the reason.

---

## DONE

### P1 — Recipes (`53ab7a0d`)
The mod had **zero** recipes; `GeoRecipeProvider` now emits **1841**, ported verbatim from
upstream `GeoRecipes.java`:
- Shape matrix, 323 (17 types x 19): brick x4, round x4, fitted x2, tile x4, inscribed x3,
  engraved x4 (both diagonals), connected x8, connected2 x8, etched x3, cubed x9, centered x5,
  lined x5, embossed x3, raised x4, fan x8, spiral x8, mossy x2 (vines), pillar x3
- Smelting, 289: every non-smooth shape back to smooth, 0 XP
- Slabs, 612: 3 -> 6 slabs, and 2 stacked slabs -> 1 block
- Stairs, 612: 4 per 6 blocks, both mirror layouts
- Deco bricks, 5: `4 * legacy recipeMultiplier` from a 2x2 of the source material

Notes: CONNECTED/CONNECTED2 resolve via `connectedBlockMapping` (no stairs/slabs, as upstream);
added `RockShapes.getStair/getSlab`. Recipe ids are foldered to stay distinct from auto-derived
defaults. `BOXRECIPES` is a runtime config so datagen emits its default (OFF) layout.

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

## BLOCKED — needs art that was never shipped (do NOT fabricate)

### P1 — Ore block textures → ores hidden behind one flag (`60d46a38`)
1.7.10 **never shipped ore art**. `OreRenderer` composited the host rock icon with an overlay
**generated at runtime** by clipping the stone background out of the vanilla/modded ore icon
(`ReikaIconHelper.clipFrom(oreIcon, stoneIcon, ...)`), then drew two layers: rock underneath,
clipped ore overlay on top.

Because there is no overlay sprite to reference, the ~20 `<rock>_<metal>_ore` blocks would render
as missing-texture cubes — and nothing generates them anyway — so they are hidden from creative/JEI
behind `GeoTabs.ORES_HAVE_TEXTURES = false`.

- [ ] Rebuild the two-layer scheme: a layered block model (base = host rock texture, overlay =
      alpha-clipped ore sprite) plus a **sprite source / runtime texture generator** that produces
      the clipped overlay from the vanilla ore texture. Then flip `ORES_HAVE_TEXTURES` to true.
- [ ] Decide whether ores should generate at all afterwards — conversion worldgen was disabled in
      1.7.10 too, so "they don't spawn" is parity. The modded-metal tag gate is already in place.

### P2 — `lowTempDiamonds` (icy ore vein's signature loot)
- [ ] Needs texture `geostrata:lowtempdiamonds` — **not on disk** (`textures/item/` contains only
      `creepvine_seeds.png`). Item itself is trivial (`ItemLowTempDiamonds` is a plain 64-stack
      Item). Port once the art exists; the icy vein currently substitutes plain diamonds.

---

## REMAINING — not blocked, just not done

### P2 — `PARTIAL` block (`BlockPartialBounds`)
- [ ] TE-backed utility block with right-click interaction and an icon-delegating renderer
      (`IconDelegateAccess`), crafted x24 (`"BSB"/"SPS"/"gSg"` — iron bars / stone tag / planks /
      stick, BuildCraft wooden gear when present — skip the BC branch). Texture
      `block/partialfencegroove.png` **does exist**. Substantial port (TE + custom render), hence
      deferred; its recipe is intentionally absent until the block lands.

### P3 — Small parity gaps
- [ ] **Nether quartz bricks** (`QUARTZBRICKS`) is the one deco brick not registered in the port
      (upstream: 1.2F/5F hardness, multiplier 2, from `Blocks.quartz_block`). Add block + recipe.
- [ ] **Void opal** self-drops; upstream had no item form (null ItemBlock → no drop). Verify the
      END/ChromatiCraft progression doesn't rely on non-obtainability.
- [ ] `steam_inside` "unused frames [20-31]" log warning — mcmeta ping-pongs 0-19, PNG has 32.
      Cosmetic; original intent unverifiable (upstream repo ships no assets).
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
8. Ore tab: a mod-provided metal shows (once `ORES_HAVE_TEXTURES` is on); ores currently hidden.
