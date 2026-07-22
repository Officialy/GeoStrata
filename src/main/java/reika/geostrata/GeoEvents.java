/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.geostrata;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.bus.api.Event;
import reika.geostrata.block.BlockGlowCrystal;
import reika.geostrata.block.BlockVent;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.RockShapes;
import reika.geostrata.registry.RockTypes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GeoEvents {

//	@SubscribeEvent
//	public void renderOpalFlecks(RenderLevelLastEvent evt) {
//		RenderSystem.disableDepthTest();
//		GeoClient.getOpalRender().renderFlecks(evt);
//        RenderSystem.enableDepthTest();
//	}

    public static class BlockColorEvents {

        // Opal tint source: uses world position for color variation
        private static final BlockTintSource OPAL_TINT = new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return GeoStrata.getOpalPositionColor(BlockPos.ZERO);
            }
            @Override
            public int colorInWorld(BlockState state,
                                    BlockAndTintGetter level,
                                    BlockPos pos) {
                return GeoStrata.getOpalPositionColor(pos);
            }
        };

        private static final BlockTintSource CRYSTAL_TINT = new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return BlockGlowCrystal.getRenderColor(BlockPos.ZERO, state.getValue(BlockGlowCrystal.COLOR_INDEX));
            }
            @Override
            public int colorInWorld(BlockState state,
                                    BlockAndTintGetter level,
                                    BlockPos pos) {
                return BlockGlowCrystal.getRenderColor(pos, state.getValue(BlockGlowCrystal.COLOR_INDEX));
            }
        };

        public static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
            List<BlockTintSource> opalSources = List.of(OPAL_TINT);
            RockShapes.filteredShapeList.forEach(rockShapes -> event.register(opalSources, RockTypes.OPAL.getID(rockShapes)));

            var opalSlabMapping = GeoBlocks.slabMapping.entrySet().stream().filter(entry -> entry.getValue().getLeft().equals(RockTypes.OPAL)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            var opalStairMapping = GeoBlocks.stairMapping.entrySet().stream().filter(entry -> entry.getValue().getLeft().equals(RockTypes.OPAL)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            var opalOreMapping = GeoBlocks.oreMapping.entrySet().stream().filter(entry -> entry.getValue().getLeft().equals(RockTypes.OPAL)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            opalSlabMapping.forEach((slabBlock, e) -> event.register(opalSources, slabBlock));
            opalStairMapping.forEach((stairBlock, e) -> event.register(opalSources, stairBlock));
            opalOreMapping.forEach((oreBlock, e) -> event.register(opalSources, oreBlock));

            // Connected opal: the dragonapi:connected_overlay model bakes tintindex-0 quads when its
            // blockstate sets "tint": true, so the same rainbow source applies.
            GeoBlocks.connectedBlockMapping.entrySet().stream()
                    .filter(entry -> entry.getValue().getLeft() == RockTypes.OPAL)
                    .forEach(entry -> event.register(opalSources, entry.getKey()));

            event.register(List.of(CRYSTAL_TINT), GeoBlocks.LUMINOUS_CRYSTAL.get());
        }

        // 26.x: item tints are data-driven — the item model JSON declares "tints" whose types are
        // codec-registered here. GeoModelProvider emits ItemModelUtils.tintedModel(...) with these
        // sources for opal items and the luminous crystal variants.
        public static void registerItemColors(RegisterColorHandlersEvent.ItemTintSources event) {
            event.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(GeoStrata.MODID, "opal"),
                    reika.geostrata.rendering.GeoItemTints.OpalItemTint.CODEC);
            event.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(GeoStrata.MODID, "crystal"),
                    reika.geostrata.rendering.GeoItemTints.CrystalItemTint.CODEC);
        }
    }

    public static void smokeVentAir(LivingDamageEvent.Pre evt) {
        if (evt.getSource() == evt.getEntity().damageSources().inWall()) { // todo: event source logic
            long last = evt.getEntity().getPersistentData().getLongOr(BlockVent.SMOKE_VENT_TAG, 0L);
            if (evt.getEntity().level().getGameTime() - last < 20) {
                evt.setNewDamage(0); // Cancel the damage
            }
        }
    }

    /**
     * Landing on a crystal spike hurts 50% more. Upstream keyed this on {@code DECOGEN} metadata 0
     * ({@code Types.CRYSTALSPIKE}) — which is exactly what the port registers as OCEAN_SPIKE, since
     * {@code DecoGenerator.OCEANSPIKE} placed DECOGEN meta 0 (hence its deco/0 texture).
     */
    public static void spikyFall(LivingFallEvent evt) {
        BlockPos c = evt.getEntity().blockPosition().below();
        Block b = evt.getEntity().level().getBlockState(c).getBlock();
        if (b == GeoBlocks.OCEAN_SPIKE.get())
            evt.setDistance(evt.getDistance() * 1.5F);
    }
}
