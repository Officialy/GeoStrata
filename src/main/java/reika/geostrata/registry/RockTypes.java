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
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.ForgeRegistries;
import reika.dragonapi.libraries.java.ReikaStringParser;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public enum RockTypes {
    //Generic makeup: Igneous 0-24; Metamorphic 16-40; Sedimentary 40+;

    //NAME(BLAST_RESISTANCE, HARDNESS, LOWEST Y POS, HIGHEST Y POS, RARITY, TOOL_TIER, GPR_COLOR

    GRANITE(8, 8, -60, 80, 0.8F, Tiers.IRON, 0xC4825E),
    BASALT(7, 7, 0, 16, 0.6F, Tiers.STONE, 0x252525),
    MARBLE(5, 5, -30, 60, 0.5F, Tiers.STONE, 0xB4B4BC),
    LIMESTONE(3, 4, -30, 75, 0.7F, Tiers.WOOD, 0xD0C4B3),
    SHALE(2, 2, -60, 75, 0.6F, Tiers.WOOD, 0x676970),
    SANDSTONE(4, 4, -60, 75, 0.7F, Tiers.WOOD, 0xD0AE90),
    PUMICE(1, 1, -60, 319, 0.2F, Tiers.WOOD, 0xD6D4CB),
    SLATE(5, 5, -30, 80, 0.5F, Tiers.STONE, 0x484B53),
    GNEISS(7, 7, -60, 100, 0.6F, Tiers.IRON, 0x7A7B79),
    PERIDOTITE(7, 7, -60, 80, 0.4F, Tiers.STONE, 0x485A4E),
    QUARTZ(7, 7, -30, 75, 0.6F, Tiers.STONE, 0xCCD5DC),
    GRANULITE(8, 8, -60, 100, 0.5F, Tiers.STONE, 0xC1BF9E),
    HORNFEL(8, 8, -60, 90, 0.5F, Tiers.IRON, 0x7B7E87),
    MIGMATITE(7, 7, -60, 100, 0.5F, Tiers.STONE, 0xA09F94),
    SCHIST(5, 5, -60, 100, 0.6F, Tiers.STONE, 0x3C3C44),
    ONYX(8, 8, -60, 24, 0.3F, Tiers.IRON, 0x111111),
    OPAL(5, 5, 16, 60, 0.2F, Tiers.STONE, 0xffddff);

    public static final RockTypes[] rockList = RockTypes.values();
    public final float blockHardness;
    public final float blastResistance;
    public final Tiers harvestTool;
    public final int minY;
    public final int maxY;
    public final float rarity;
    public final int rockColor;
    private final HashSet<RockTypes> coincidentTypes = new HashSet<>();

    RockTypes(float blastresistance, float hardness, int lowYLevel, int highestYLevel, float rarity, Tiers tool, int color) {
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
        return GeoBlocks.blockMapping.get(id).getKey();
    }

    public String getName() {
        return ReikaStringParser.capFirstChar(this.name());
    }

    public Tiers getHarvestMin() {
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
        switch(this) {
            case BASALT:
            case GRANITE:
            case GNEISS:
            case PUMICE:
            case MIGMATITE:
            case ONYX:
            case SANDSTONE:
            case LIMESTONE:
                return true;
            case GRANULITE:
            case SCHIST:
            case OPAL:
            case QUARTZ:
            case MARBLE:
            case PERIDOTITE:
                break;
            case HORNFEL:
                return world.getBiome(new BlockPos(x, 0, z)).value().coldEnoughToSnow(new BlockPos(x, 0, z));//getEnableSnow();
            case SHALE:
            case SLATE:
                /*if (BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(x, z), Type.SANDY))
                    return false;
                if (BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(x, z), Type.DRY))
                    return false;
                if (BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(x, z), Type.SAVANNA))
                    return false;*/
                return true;
        }
        return true;
    }

    public boolean canGenerateAtSkipXZ(LevelAccessor world, int x, int y, int z, RandomSource r) {
        if (y > maxY)
            return false;
        if (y < minY)
            return false;
        switch(this) {
            case BASALT:
            case HORNFEL:
            case LIMESTONE:
            case SHALE:
            case SLATE:
            case SANDSTONE:
            case PUMICE:
            case MIGMATITE:
            case GRANITE:
            case GRANULITE:
            case SCHIST:
            case OPAL:
            case QUARTZ:
            case PERIDOTITE:
            case MARBLE:
                break;
            case GNEISS:
                return true;
            case ONYX:
                return ReikaWorldHelper.checkForAdjMaterial(world, new BlockPos(x, y, z), Material.LAVA) != null;
        }
        return true;
    }

    public boolean canGenerateAt(LevelAccessor world, BlockPos pos, RandomSource r) {
        if (pos.getY() > maxY)
            return false;
        if (pos.getY() < minY)
            return false;
        switch(this) {
            case BASALT:
            case SANDSTONE:
            case PUMICE:
            case MIGMATITE:
            case LIMESTONE:
            case GRANITE:
            case GNEISS:
                return true;
            case GRANULITE:
            case OPAL:
            case SCHIST:
            case QUARTZ:
            case PERIDOTITE:
            case MARBLE:
                break;
            case HORNFEL:
                return world.getBiome(pos).value().coldEnoughToSnow(pos);
            case SHALE:
            case SLATE:
                return world.getBiomeManager().getBiome(pos) != world.registryAccess().registryOrThrow(Registries.BIOME).getHolder(Biomes.DESERT).get() && world.getBiomeManager().getBiome(pos) != world.registryAccess().registryOrThrow(Registries.BIOME).getHolder(Biomes.BADLANDS).get() && world.getBiomeManager().getBiome(pos) != world.registryAccess().registryOrThrow(Registries.BIOME).getHolder(Biomes.SAVANNA).get();
            case ONYX:
                return ReikaWorldHelper.checkForAdjMaterial(world, pos, Material.LAVA) != null;
        }
        return true;
    }
}
