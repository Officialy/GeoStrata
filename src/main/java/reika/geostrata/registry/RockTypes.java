/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.geostrata.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;

import reika.geostrata.GeoStrata;
import reika.geostrata.block.BlockConnectedRock;

import net.minecraft.world.level.material.MapColor;
import reika.dragonapi.libraries.java.ReikaStringParser;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public enum RockTypes {
    //Generic makeup (1.7.10 Y): Igneous 0-24; Metamorphic 16-40; Sedimentary 40+.
    //Y-ranges are the 1.7.10 table remapped with newY = 2*oldY - 64 (1.7 underground was 0..64
    //bedrock->surface; modern is -64..64), which preserves each rock's relative depth and the
    //igneous/metamorphic/sedimentary layering the Legacy pattern relies on. Rarities are the
    //original 1.7.10 values (they drive both Legacy vein counts and the Simplex threshold).

    //NAME(BLAST_RESISTANCE, HARDNESS, LOWEST Y POS, HIGHEST Y POS, RARITY, TOOL_TIER, GPR_COLOR

    GRANITE(8, 8, -32, 32, 1F, ToolMaterial.IRON, 0xC4825E), //was 16-48: mid metamorphic band
    BASALT(7, 7, 32, 192, 1F, ToolMaterial.STONE, 0x252525), //was 48-128: high sedimentary band
    MARBLE(5, 5, -32, 0, 1F, ToolMaterial.STONE, 0xB4B4BC), //was 16-32
    LIMESTONE(3, 4, 32, 192, 1F, ToolMaterial.WOOD, 0xD0C4B3), //was 48-128
    SHALE(2, 2, 32, 64, 1F, ToolMaterial.WOOD, 0x676970), //was 48-64
    SANDSTONE(4, 4, 32, 192, 1F, ToolMaterial.WOOD, 0xD0AE90), //was 48-128
    PUMICE(1, 1, -60, -32, 0.6F, ToolMaterial.WOOD, 0xD6D4CB), //was 0-16: bottom igneous band
    SLATE(5, 5, 0, 32, 1F, ToolMaterial.STONE, 0x484B53), //was 32-48
    GNEISS(7, 7, -32, 0, 0.8F, ToolMaterial.IRON, 0x7A7B79), //was 16-32
    PERIDOTITE(7, 7, -60, -16, 0.6F, ToolMaterial.STONE, 0x485A4E), //was 0-24
    QUARTZ(7, 7, -60, 64, 0.5F, ToolMaterial.STONE, 0xCCD5DC), //was 0-64
    GRANULITE(8, 8, -32, 0, 0.7F, ToolMaterial.STONE, 0xC1BF9E), //was 16-32
    HORNFEL(8, 8, -60, 64, 0.8F, ToolMaterial.IRON, 0x7B7E87), //was 0-64
    MIGMATITE(7, 7, -60, -32, 0.6F, ToolMaterial.STONE, 0xA09F94), //was 0-16
    SCHIST(5, 5, -32, 32, 0.8F, ToolMaterial.STONE, 0x3C3C44), //was 16-48
    ONYX(8, 8, -60, -16, 1F, ToolMaterial.IRON, 0x111111), //was 0-24
    OPAL(5, 5, 0, 56, 0.125F, ToolMaterial.STONE, 0xffddff); //was 32-60

    public static final RockTypes[] rockList = RockTypes.values();

    static { //1.7.10 ran this from loadMappings(); without it Legacy vein density is never divided down
        for (RockTypes rock : rockList) {
            rock.calcCoincidentTypes();
        }
    }

    public final float blockHardness;
    public final float blastResistance;
    public final ToolMaterial harvestTool;
    public final int minY;
    public final int maxY;
    public final float rarity;
    public final int rockColor;
    private final HashSet<RockTypes> coincidentTypes = new HashSet<>();

    RockTypes(float blastresistance, float hardness, int lowYLevel, int highestYLevel, float rarity, ToolMaterial tool, int color) {
        blastResistance = blastresistance;
        blockHardness = hardness * 0.675F;
        harvestTool = tool;
        minY = lowYLevel;
        maxY = highestYLevel;
        this.rarity = rarity;
        rockColor = color;
    }

    public static RockTypes getTypeAtPos(BlockGetter world, BlockPos pos) {
        return getTypeFromID(world.getBlockState(pos).getBlock());
    }

    public static RockTypes getTypeFromID(Block id) {
        if (id instanceof BlockConnectedRock) {
            var pair = GeoBlocks.connectedBlockMapping.get(id);
            if (pair != null) {
                return pair.getKey();
            }
        }
        var pair = GeoBlocks.blockMapping.get(id);
        if (pair != null) {
            return pair.getKey();
        }
        // If we can't find the block in either mapping, return a default type to avoid NPE
        GeoStrata.LOGGER.warn("Could not find rock type for block: " + id);
        return RockTypes.GRANITE; // Default to a common type
    }

    public String getName() {
        return ReikaStringParser.capFirstChar(this.name());
    }

    public ToolMaterial getHarvestMin() {
        return harvestTool;
    }

    public Block getID(RockShapes shape) {
        return shape.getBlock(this);
    }

    public ItemStack getItem(RockShapes shape) {
        return new ItemStack(this.getID(shape), 1);
    }

    public static RockTypes getTypeAtCoords(BlockGetter world, int x, int y, int z) {
        return getTypeFromID(world.getBlockState(new BlockPos(x, y, z)).getBlock());
    }

    public static RockTypes getTypeAtCoords(BlockGetter world, BlockPos pos) {
        return getTypeFromID(world.getBlockState(pos).getBlock());
    }

    public Set<RockTypes> getCoincidentTypes() {
        return Collections.unmodifiableSet(coincidentTypes);
    }

    private void calcCoincidentTypes() {
        for (RockTypes rock : rockList) {
            if (rock != this)
                if (ReikaMathLibrary.doRangesOverLap(minY, maxY, rock.minY, rock.maxY))
                    coincidentTypes.add(rock);
        }
    }

    public boolean canGenerateAtXZ(LevelAccessor world, int x, int z, RandomSource r) {
        switch (this) {
            case BASALT, GRANITE, GNEISS, PUMICE, MIGMATITE, ONYX, SANDSTONE, LIMESTONE -> {
                return true;
            }
            case GRANULITE, SCHIST, OPAL, QUARTZ, MARBLE, PERIDOTITE -> {
            }
            case HORNFEL -> {
                return world.getBiome(new BlockPos(x, 0, z)).value().coldEnoughToSnow(new BlockPos(x, 0, z), world.getSeaLevel());//getEnableSnow();
            }
            case SHALE, SLATE -> {
                /*if (BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(x, z), Type.SANDY))
                    return false;
                if (BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(x, z), Type.DRY))
                    return false;
                if (BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(x, z), Type.SAVANNA))
                    return false;*/
                return true;
            }
        }
        return true;
    }

    public boolean canGenerateAtSkipXZ(LevelAccessor world, int x, int y, int z, RandomSource r) {
        if (y > maxY)
            return false;
        if (y < minY)
            return false;
        switch (this) {
            case BASALT, HORNFEL, LIMESTONE, SHALE, SLATE, SANDSTONE, PUMICE, MIGMATITE, GRANITE, GRANULITE, SCHIST, OPAL, QUARTZ, PERIDOTITE, MARBLE -> {
            }
            case GNEISS -> {
                return true;
            }
            case ONYX -> {
                return ReikaWorldHelper.checkForAdjMaterial(world, new BlockPos(x, y, z), MapColor.FIRE) != null; //todo lava
            }
        }
        return true;
    }

    public boolean canGenerateAt(LevelAccessor world, BlockPos pos, RandomSource r) {
        if (pos.getY() > maxY)
            return false;
        if (pos.getY() < minY)
            return false;
        switch (this) {
            case BASALT, SANDSTONE, PUMICE, MIGMATITE, LIMESTONE, GRANITE, GNEISS -> {
                return true;
            }
            case GRANULITE, OPAL, SCHIST, QUARTZ, PERIDOTITE, MARBLE -> {
            }
            case HORNFEL -> {
                return world.getBiome(pos).value().coldEnoughToSnow(pos, world.getSeaLevel());
            }
            case SHALE, SLATE -> {
                return world.getBiomeManager().getBiome(pos) != world.registryAccess().lookupOrThrow(Registries.BIOME).get(Biomes.DESERT).get() && world.getBiomeManager().getBiome(pos) != world.registryAccess().lookupOrThrow(Registries.BIOME).get(Biomes.BADLANDS).get() && world.getBiomeManager().getBiome(pos) != world.registryAccess().lookupOrThrow(Registries.BIOME).get(Biomes.SAVANNA).get();
            }
            case ONYX -> {
                return ReikaWorldHelper.checkForAdjMaterial(world, pos, MapColor.FIRE) != null; //todo lava
            }
        }
        return true;
    }
}
