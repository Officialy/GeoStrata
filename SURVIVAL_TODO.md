# GeoStrata Survival-Readiness TODO

Audit of what's missing/broken for a full survival experience, vs the 1.7.10 original.
Findings only — nothing here has been fixed yet. Updated as the scan progresses.

Legend: **P1** = blocks survival progression/unobtainable content · **P2** = broken-but-playable · **P3** = polish/parity nit

## Scan progress

- [x] 1. Recipes (crafting/smelting)
- [x] 2. Registration diff vs 1.7.10 (blocks/items missing from the port)
- [x] 3. Loot tables + block/item tags coverage
- [x] 4. Worldgen (rock gen, features, biome modifiers)
- [x] 5. Assets (textures/models/lang still missing)
- [x] 6. Mechanics spot-check (shears, silk touch, right-click, growth)
- [x] 7. Config options
- [x] 8. Cross-mod hooks (RotaryCraft vents, RF crystal energy)

## Findings

### 1. Recipes — **P1: the mod has ZERO recipes**

`GeoRecipeProvider` does not exist (`GeoDataProviders.java:32` has it commented out) and no recipe
JSONs ship. Every decorative shape is uncraftable in survival; rocks are only obtainable as raw
worldgen drops. Upstream `GeoRecipes.java` (130 lines) defines, **per rock type**:

- [ ] Shape matrix (output counts matter): brick×4, round×4, fitted×2, tile×4, inscribed×3,
      engraved×4 (2 layouts), connected×8, connected2×8, etched×3, cubed×9, centered×5, lined×5,
      embossed×3, raised×4, fan×8, spiral×8, mossy×2, pillar×3
- [ ] Smelting: every non-smooth shape → smooth (0 XP)
- [ ] Slabs: 3×item → slab; 2×slab (vertical) → item back
- [ ] Stairs: standard 6-block stair layout (both mirror orientations)
- [ ] `BOXRECIPES` config-gated alternates (`GeoOptions.BOXRECIPES`)
- [ ] PARTIAL block ×24 recipe (`"BSB"/"SPS"/"gSg"` planks/stone/iron-bars/glass) — check PARTIAL
      block is even registered in the port (see §2)
- [ ] BuildCraft-gated recipe — skip (mod not in stack), note for parity

Also worth porting to modern conventions while at it: stonecutter entries for the shape matrix
(vanilla-parity convenience, not in 1.7.10 — optional).

### 2. Registration diff vs 1.7.10

Everything in the upstream `GeoBlocks` enum is ported **except**:

- [ ] **P2: `PARTIAL` (BlockPartialBounds)** — TE-backed utility block with right-click interaction,
      craftable ×24 (see §1's PARTIAL recipe). Entirely absent from the port.
- [ ] **P2: `lowTempDiamonds` (ItemLowTempDiamonds)** — the icy ore vein's signature loot. Port
      substitutes plain diamonds (noted in `BlockOreVein` javadoc). Needs the item + icy-vein loot
      entry + whatever recipes/uses upstream gave it.
- [ ] **P3: `BlockOreTile` camouflage-ore divergence** — port replaced the single TE camouflage
      block with per-metal concrete ore blocks. Documented divergence; conversion worldgen was
      disabled in 1.7.10 too ([[geostrata-worldgen-audit]]), so parity is "ores don't generate" —
      but the port's per-metal blocks now exist with **no worldgen and no textures** (see §5).
      Decide: either finish them (textures + gated gen) or hide entirely until backed.

### 3. Loot tables + tags — tables exist for all 996 entries but several have wrong semantics

- [ ] **P1: Vents drop nothing in survival.** `BlockVent.canHarvestBlock` returns `false`
      (suppresses all drops) while the loot table says dropSelf — contradictory, and both wrong.
      Upstream: normal mining drops **cobblestone** (`Blocks.stone.getItemDropped`), silk touch
      drops the vent block (`canSilkHarvest = true`). Fix loot to silk-dispatch(vent, cobble) and
      remove the `canHarvestBlock` override.
- [ ] **P1: Luminous crystal NO-DROP.** Upstream default-dropped itself (metadata item). Port loot
      is `noDrop` (block is item-less; drop should be the matching `luminous_crystal_item_N` — or
      item_0 at minimum).
- [ ] **P2: RF crystal drops itself; upstream drops redstone** ×(1+rand(6))×(1+rand(1+fortune)),
      redstone-ore-style (silk had no self-drop — block had no item form upstream).
- [ ] **P2: Lava rock NO-DROP.** Upstream default-dropped its metadata item (height variant).
      Port has the 4 variant items but drops nothing.
- [ ] **P3: RF crystal seed** drops itself but loses the `activated` state (upstream preserved it
      as NBT when `RFACTIVATE` on; needs a data component on the drop).
- [ ] **P3: Void opal** — port dropSelf; upstream had **no item form at all** (null ItemBlock →
      no drop). Decide intent; self-drop is arguably an improvement, but verify the END progression
      (ChromatiCraft interop) doesn't depend on non-obtainability.
- [ ] **P3: Mining-speed tags** — vents, lava rock, icicle, void opals, rf crystals are not in
      `mineable/pickaxe` (they don't require tool so drops work, but pickaxes don't speed them up).

### 4. Worldgen — core is wired and healthy; two cleanups

All 11 features registered, 11 placed features + 13 biome modifiers generated; rock strata gen
(`geo_rock` → RockGenerator, legacy layering default) is live. Prior audit
([[geostrata-worldgen-audit]]) covers fidelity. Remaining:

- [ ] **P3: 7 stale hand-written worldgen JSONs** in `src/main/resources/data/geostrata/worldgen/`
      (placed_feature + configured_feature) that **all differ** from the generated authoritative
      copies. The generated ones win the resource merge today, but these are landmines — delete
      them.
- [ ] **P3: lava rock worldgen-time quench** — upstream skipped the water-conversion check during
      generation (`doingLavaRockGen` / chunk-finished guard, commented out in the port), so
      lava rock generated touching water may instantly turn to obsidian/cobble at gen time.
- [ ] (parity note, no action) 1.7.10 retrogen has no modern equivalent — features only appear in
      newly generated chunks.

### 5. Assets still missing

- [ ] **P1 (pairs with §2 ore decision): ALL ~20 `<rock>_<metal>_ore` block textures are absent**
      — every ore model logs "Missing textures". 1.7.10 composed host-rock texture + ore overlay
      at runtime via the camouflage TE. Options: layered model (base rock texture + ore overlay
      sprite — only if Reika's overlay art exists somewhere), or hide the blocks until then.
      NEVER fabricate the art ([[never-fabricate-assets]]).
- [ ] **P3: opal items untinted** in inventory (26.x item tints need `tint_sources` in the
      `items/*.json` definitions — none authored; world rendering works).
- [ ] **P3: connected opal untinted** — DragonAPI `OverlayConnectedModel` bakes NO_TINT quads and
      no tint handler is registered for `opal_connected*`. Needs tint support in the connected
      model codec.
- [ ] **P3: `steam_inside` "unused frames [20–31]"** log warning — mcmeta ping-pongs frames 0–19,
      PNG has 32. Cosmetic; original intent unverifiable (upstream repo has no assets).
- [ ] Lang: complete (997 entries, no missing-key evidence in logs). Verify oddballs in-game
      (vent names per type, lava rock variant names).
- [ ] **Verify in-game (recent fixes, not yet confirmed):** ocean-spike in-world texture,
      icicle dynamic model, vent inside animation, opal world tint, luminous crystal render,
      glowing-vines/rf-seed/void-opal textures, all item models from the asset pass.

### 6. Mechanics

- [ ] **P2: Glowing vines shearing is not actually wired.** `BlockGlowingVines.onSheared(Player,
      ItemStack, Level, BlockPos, int)` does NOT override NeoForge's
      `IShearable.onSheared(Player, ItemStack, Level, BlockPos)` (extra `fortune` param — wrong
      signature, silently never called), so shears fall through to default behavior. Upstream
      dropped one vine item per occupied side via its ShearablePlant path. DragonAPI's
      `ShearablePlant.shearAll/shearSide` have **zero callers** in the port — dead API; decide
      whether to wire it or fold into IShearable.
- [ ] **P2: `CRYSTALSPIKE` deco type unported** — upstream `spikyFall` multiplies fall damage
      ×1.5 on DECOGEN CRYSTALSPIKE; the port's `GeoEvents.spikyFall` is a stub referencing a
      commented-out `CRYSTAL_SPIKE` block. Needs the deco block + the event body.
- [ ] Verified OK: creepvine right-click seed harvest; ore-vein harvest cycles; RF crystal FE
      growth loop (seed charges from FE, grows crystal, redstone-signal discharge); vent
      plug/explode + entity effects (fixed 2026-07-21); glow crystals don't grow upstream either.

### 7. Config — OK

Port `GeoOptions` matches upstream minus obsolete biome-ID options (modern biomes are data-driven)
plus new `GEOORE`/`RETROGEN` toggles. No action.

### 8. Cross-mod — OK for current stack

RoC-aware vents (`BlockEntityVentRoC`) registered when RotaryCraft present; RF crystal FE uses
NeoForge energy capability; Chisel compat class present (inert without Chisel); ore-vein
ChromatiCraft end-distance interop deliberately gated out until CC is further along (javadoc'd).

---

## Suggested priority order for survival readiness

1. **Recipes** (§1) — the single biggest gap; nothing decorative is craftable.
2. **Vent + luminous crystal + lava rock + RF crystal drops** (§3) — obtainability/parity.
3. **Ore blocks decision** (§2/§5) — hide until textured+generated, or finish them.
4. **Shearing + PARTIAL block + lowTempDiamonds + CRYSTALSPIKE** (§2/§6).
5. **P3 cleanups** (stale worldgen JSONs, tags, tints, seed NBT).
