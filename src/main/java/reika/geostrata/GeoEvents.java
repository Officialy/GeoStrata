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
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.Event;
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

        public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {

            RockShapes.filteredShapeList.forEach(rockShapes -> event.getBlockColors().register((state, access, pos, tintIndex) -> {
                if(pos != null){
                    return GeoStrata.getOpalPositionColor(pos);
                }
                return 0;
            }, RockTypes.OPAL.getID(rockShapes)));
            var opalSlabMapping = GeoBlocks.slabMapping.entrySet().stream().filter(entry -> entry.getValue().getLeft().equals(RockTypes.OPAL)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            var opalStairMapping = GeoBlocks.stairMapping.entrySet().stream().filter(entry -> entry.getValue().getLeft().equals(RockTypes.OPAL)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            var opalOreMapping = GeoBlocks.oreMapping.entrySet().stream().filter(entry -> entry.getValue().getLeft().equals(RockTypes.OPAL)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


            opalSlabMapping.forEach((slabBlock, e) -> event.register((state, access, pos, tintIndex) -> {
                if(pos != null){
                    return GeoStrata.getOpalPositionColor(pos);
                }
                return 0;
            },  slabBlock));
            opalStairMapping.forEach((stairBlock, e) -> event.register((state, access, pos, tintIndex) -> {
                if(pos != null){
                    return GeoStrata.getOpalPositionColor(pos);
                }
                return 0;
            },  stairBlock));
            opalOreMapping.forEach((oreBlock, e) -> event.register((state, access, pos, tintIndex) -> {
                if(pos != null){
                    return GeoStrata.getOpalPositionColor(pos);
                }
                return 0;
            },  oreBlock));

            event.register((state, access, pos, tintIndex) -> {
                if(pos != null){
                    return BlockGlowCrystal.getRenderColor(pos, state.getValue(BlockGlowCrystal.COLOR_INDEX));
                }
                return 0;
            }, GeoBlocks.LUMINOUS_CRYSTAL.get());
        }

        public static void registerItemColors(RegisterColorHandlersEvent.Item event) {

            final ItemColor itemColor = (pStack, pTintIndex) -> GeoStrata.getOpalPositionColor(Minecraft.getInstance().player.getOnPos());
            RockShapes.filteredShapeList.forEach(rockShapes -> event.getItemColors().register(itemColor, RockTypes.OPAL.getID(rockShapes)));
            var opalSlabMapping = GeoBlocks.slabMapping.entrySet().stream().filter(entry -> entry.getValue().getLeft().equals(RockTypes.OPAL)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            var opalStairMapping = GeoBlocks.stairMapping.entrySet().stream().filter(entry -> entry.getValue().getLeft().equals(RockTypes.OPAL)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            opalSlabMapping.forEach((slabBlock, e) -> event.getItemColors().register(itemColor, slabBlock));
            opalStairMapping.forEach((stairBlock, e) -> event.getItemColors().register(itemColor, stairBlock));


            final ItemColor crystalItemColor = (pStack, pTintIndex) -> BlockGlowCrystal.getRenderColor(Minecraft.getInstance().player.getOnPos(), 1); //todo color index
            event.getItemColors().register(crystalItemColor, GeoBlocks.LUMINOUS_CRYSTAL.get());
        }
    }

    public static void smokeVentAir(LivingDamageEvent evt) {
        if (evt.getSource() == evt.getEntity().damageSources().inWall()) {
            long last = evt.getEntity().serializeNBT().getLong(BlockVent.SMOKE_VENT_TAG);
            if (evt.getEntity().level.getGameTime() - last <= 8) {
                evt.setResult(Event.Result.ALLOW);
            }
        }
    }

    public static void spikyFall(LivingFallEvent evt) {
        BlockPos c = new BlockPos((int) evt.getEntity().position().x, (int) evt.getEntity().position().y, (int) evt.getEntity().position().z).offset(0, -1, 0);
        Block b = evt.getEntity().level.getBlockState(c).getBlock();
//        if (b == GeoBlocks.CRYSTAL_SPIKE.get())
//            evt.setDistance(evt.getDistance() * 1.5F);

    }
}
