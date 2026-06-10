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
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.bus.api.Event;
import reika.geostrata.block.BlockGlowCrystal;
import reika.geostrata.block.BlockVent;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.RockShapes;
import reika.geostrata.registry.RockTypes;

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
            public int color(net.minecraft.world.level.block.state.BlockState state) {
                return GeoStrata.getOpalPositionColor(BlockPos.ZERO);
            }
            @Override
            public int colorInWorld(net.minecraft.world.level.block.state.BlockState state,
                                    net.minecraft.client.renderer.block.BlockAndTintGetter level,
                                    BlockPos pos) {
                return GeoStrata.getOpalPositionColor(pos);
            }
        };

        private static final BlockTintSource CRYSTAL_TINT = new BlockTintSource() {
            @Override
            public int color(net.minecraft.world.level.block.state.BlockState state) {
                return BlockGlowCrystal.getRenderColor(BlockPos.ZERO, state.getValue(BlockGlowCrystal.COLOR_INDEX));
            }
            @Override
            public int colorInWorld(net.minecraft.world.level.block.state.BlockState state,
                                    net.minecraft.client.renderer.block.BlockAndTintGetter level,
                                    BlockPos pos) {
                return BlockGlowCrystal.getRenderColor(pos, state.getValue(BlockGlowCrystal.COLOR_INDEX));
            }
        };

        public static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
            java.util.List<BlockTintSource> opalSources = java.util.List.of(OPAL_TINT);
            RockShapes.filteredShapeList.forEach(rockShapes -> event.register(opalSources, RockTypes.OPAL.getID(rockShapes)));

            var opalSlabMapping = GeoBlocks.slabMapping.entrySet().stream().filter(entry -> entry.getValue().getLeft().equals(RockTypes.OPAL)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            var opalStairMapping = GeoBlocks.stairMapping.entrySet().stream().filter(entry -> entry.getValue().getLeft().equals(RockTypes.OPAL)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            var opalOreMapping = GeoBlocks.oreMapping.entrySet().stream().filter(entry -> entry.getValue().getLeft().equals(RockTypes.OPAL)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            opalSlabMapping.forEach((slabBlock, e) -> event.register(opalSources, slabBlock));
            opalStairMapping.forEach((stairBlock, e) -> event.register(opalSources, stairBlock));
            opalOreMapping.forEach((oreBlock, e) -> event.register(opalSources, oreBlock));

            event.register(java.util.List.of(CRYSTAL_TINT), GeoBlocks.LUMINOUS_CRYSTAL.get());
        }

        // 26.1: item tint sources are declared in resource-pack JSON (item model files,
        // "tint_sources" field) — not in Java. The legacy RegisterColorHandlersEvent.Item path is
        // gone. We keep this empty handler stub for symmetry with the block-tint registration
        // above; opal + crystal item tints live in assets/geostrata/items/<name>.json.
        public static void registerItemColors(RegisterColorHandlersEvent.ItemTintSources event) {
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

    public static void spikyFall(LivingFallEvent evt) {
        BlockPos c = new BlockPos((int) evt.getEntity().position().x, (int) evt.getEntity().position().y, (int) evt.getEntity().position().z).offset(0, -1, 0);
        Block b = evt.getEntity().level().getBlockState(c).getBlock();
//        if (b == GeoBlocks.CRYSTAL_SPIKE.get())
//            evt.setDistance(evt.getDistance() * 1.5F);

    }
}
