/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.geostrata.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reika.dragonapi.instantiable.data.blockstruct.BlockArray;
import reika.dragonapi.instantiable.data.blockstruct.CurvedTrajectory;
import reika.dragonapi.instantiable.data.immutable.BlockBox;
import reika.dragonapi.instantiable.data.immutable.DecimalPosition;
import reika.dragonapi.instantiable.data.immutable.ReikaBlockPosHelper;
import reika.dragonapi.libraries.java.ReikaJavaLibrary;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.dragonapi.libraries.mathsci.ReikaMathLibrary;
import reika.dragonapi.modinteract.power.ReikaEnergyStorage;
import reika.geostrata.GeoStrata;
import reika.geostrata.registry.GeoBlockEntities;
import reika.geostrata.registry.GeoBlocks;
import reika.geostrata.registry.GeoOptions;

import java.util.*;


public class BlockRFCrystalSeed extends BlockRFCrystal {
    public BlockRFCrystalSeed() {
        super();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileRFCrystal(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState p_153213_, BlockEntityType<T> p_153214_) {
        return level.isClientSide() ? null : ((level1, pPos, pState1, pBlockEntity) -> ((BlockRFCrystalSeed.TileRFCrystal) pBlockEntity).updateEntity());
    }


    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (level.getBlockEntity(pos) != null && level.getBlockEntity(pos) instanceof TileRFCrystal) {
            ((TileRFCrystal) level.getBlockEntity(pos)).breakEntireCrystal(false);
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        ItemStack is = new ItemStack(this);
        if (GeoOptions.RFACTIVATE.getState()) {
            BlockEntity te = builder.getLevel().getBlockEntity(new BlockPos((int) builder.getParameter(LootContextParams.ORIGIN).x, (int) builder.getParameter(LootContextParams.ORIGIN).y, (int) builder.getParameter(LootContextParams.ORIGIN).z));
            if (te instanceof TileRFCrystal && ((TileRFCrystal) te).isActivated) {
                is.getOrCreateTag().putBoolean("activated", true);
            }
        }
        return ReikaJavaLibrary.makeListFrom(is);
    }

    public static class TileRFCrystal extends BlockEntity implements CurvedTrajectory.TrailShape, CurvedTrajectory.InitialAngleProvider {

        //private SimplexNoiseGenerator XYCrystalShape;
        //private SimplexNoiseGenerator XZCrystalShape;
        //private SimplexNoiseGenerator YZCrystalShape;

        //private final Simplex3DGenerator crystalShape = new Simplex3DGenerator(0);

        //private HashSet<BlockPos> crystalShape;

//        private final Simplex3DGenerator crystalShapeA = new Simplex3DGenerator(0);
//        private final Simplex3DGenerator crystalShapeB = new Simplex3DGenerator(0);

        private HashSet<BlockPos> crystalShape;
        private boolean isActivated = false;
        public final ReikaEnergyStorage energy;
        private final LazyOptional<IEnergyStorage> energyStorageLazyOptional;
        private final BlockArray crystal = new BlockArray();

        public TileRFCrystal(BlockPos p_155229_, BlockState p_155230_) {
            super(GeoBlockEntities.RF_CRYSTAL_SEED.get(), p_155229_, p_155230_);
            energy = new ReikaEnergyStorage(this.getCapacity(), this.getCapacity(), 0, this) {
                @Override
                public int receiveEnergy(int maxReceive, boolean simulate) {
                    int amt = Math.min(Math.min(getCapacity() - energy, Integer.MAX_VALUE), maxReceive);
                    if (!simulate)
                        energy += amt;
                    return amt;
                }

                @Override
                public int extractEnergy(int maxExtract, boolean simulate) {
                    int amt = Math.min(Math.min(energy, Integer.MAX_VALUE), maxExtract);
                    if (!simulate)
                        energy -= amt;
                    return amt;
                }

                @Override
                public int getEnergyStored() {
                    return Math.min(energy, Integer.MAX_VALUE);
                }

                @Override
                public int getMaxEnergyStored() {
                    return Math.min(getCapacity(), Integer.MAX_VALUE);
                }
            };
            energyStorageLazyOptional = LazyOptional.of(() -> energy);
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return cap == ForgeCapabilities.ENERGY ? energyStorageLazyOptional.cast() : super.getCapability(cap, side);
        }

        @Override
        public void setRemoved() {
            super.setRemoved();
            energyStorageLazyOptional.invalidate();
        }

        public void breakEntireCrystal(boolean skipSelf) {
            ArrayList<BlockPos> li = new ArrayList<>(crystal.keySet());
            crystal.clear();
            if (skipSelf)
                crystal.addBlockCoordinate(getBlockPos());
            for (BlockPos c : li) {
                if (skipSelf && c.equals(getBlockPos()))
                    continue;
                GeoStrata.LOGGER.info("Breaking " + c);
                ReikaWorldHelper.dropAndDestroyBlockAt(level, c, null, true, true);
            }
        }

        public void updateEntity() {
            if (crystalShape == null && !level.isClientSide()) {
                CurvedTrajectory cv = new CurvedTrajectory(getBlockPos());
                cv.trailCount = 9;
                cv.trailForkChance = 0;//0.01F;
                cv.bounds = BlockBox.block(this).expand(48, 24, 48);
                cv.generatePaths(level.getServer().getWorldData().worldGenOptions().seed() ^ worldPosition.hashCode(), this, this);
                crystalShape = cv.getLocations();
            }

            if (!level.isClientSide()) {
                if (isActivated) {
                    int cap = this.getCapacity();
                    if (energy.getEnergyStored() > cap)
                        energy.setEnergy(cap);
                    //ReikaJavaLibrary.pConsole(String.format("%.4f", energy/(float)cap)+" @ "+crystal.getSize()+" : "+energy+" / "+cap);
                    if (energy.getEnergyStored() > cap * 4 / 5 && crystal.getSize() < 2000) {
                        this.growNewCrystal();
                    }

                    if (energy.getEnergyStored() > 0 && level.hasNeighborSignal(getBlockPos())) {
                        BlockEntity te = level.getBlockEntity(getBlockPos().below());
                        if (te != null && te.getCapability(ForgeCapabilities.ENERGY).isPresent()) {
                            IEnergyStorage ies = te.getCapability(ForgeCapabilities.ENERGY).orElse(null);
                            if (ies != null) {
                                /*int amt = ies.receiveEnergy(energy.getEnergyStored(), true);
                                if (amt > 0) {
                                    energy.extractEnergy(ies.receiveEnergy(amt, false), false);
                                }*/
                                int pushable = Math.min(energy.getEnergyStored(), ies.receiveEnergy(this.getCapacity(), true));
                                if (pushable > 0) {
                                    pushable = ies.receiveEnergy(this.getCapacity(), false);
                                    energy.setEnergy(energy.getEnergyStored() - pushable);
//                                    GeoStrata.LOGGER.info("Pushing " + pushable + " RF to " + te);
                                }
                            }
                        }
                    }
                } else {
                    if (crystal.getSize() > 1) {
                        this.breakEntireCrystal(true);
                    }
                }
            }
        }

        /*
        Checks if the crystal is in a valid position to grow, then proceeds to grow a new crystal block
         */
        private void growNewCrystal() {
            BlockPos loc = null;
            ArrayList<BlockPos> li = new ArrayList<>(crystal.list());
            li.add(worldPosition);
            Collections.shuffle(li);
            for (BlockPos c : li) {
                for (BlockPos c2 : ReikaBlockPosHelper.getAdjacentCoordinates(c)) {
                    if (this.canGrowInto(level, c2) && this.isValidLocation(c2)) {
                        loc = c2;
                        break;
                    }
                }
            }
            if (loc != null) {
                //loc.setBlock(level, GeoBlocks.RFCRYSTAL.get());
                //crystal.addBlockCoordinate(loc.xCoord, loc.yCoord, loc.zCoord);
                place(level, loc, this);
                energy.setEnergy(Math.max(energy.getEnergyStored() - this.getGrowthCost(), 0));
            }
        }

        /*
        Growing a crystal block costs energy, this method returns the amount of energy required to grow a crystal block. The cost is based on the number of crystal blocks, capped at 600000.
         */
        private int getGrowthCost() {
            return Mth.clamp(crystal.getSize() * 1000, 2000, 600000);
        }

        /*
        Checks if the crystal can grow into the block at the given location.
        Args: Level, BlockPos - the Level/World of where the checked position is - and the position of the block to check.
         */
        private boolean canGrowInto(Level level, BlockPos c2) {
            BlockState b = level.getBlockState(c2);
            return b.getBlock() == Blocks.AIR || b.getBlock() == Blocks.WATER || b.isAir();
        }

        /*
        Checks if the crystal can grow into the given location.
        Args: BlockPos of the location to check
         */
        private boolean isValidLocation(BlockPos c) {
            if (crystal.getSize() < 12)
                return true;
            return crystalShape.contains(c);
        }

        @Override
        protected void saveAdditional(CompoundTag NBT) {
            super.saveAdditional(NBT);

            NBT.putInt("energy", energy.getEnergyStored());
            crystal.saveAdditional("blocks", NBT);
            NBT.putBoolean("activated", isActivated);
        }

        @Override
        public void load(CompoundTag NBT) {
            super.load(NBT);

            energy.setEnergy(NBT.getInt("energy"));
            crystal.load("blocks", NBT);
            isActivated = NBT.getBoolean("activated") || !GeoOptions.RFACTIVATE.getState();
        }

        @Nullable
        @Override
        public Packet<ClientGamePacketListener> getUpdatePacket() {
            CompoundTag NBT = new CompoundTag();
            this.saveAdditional(NBT);
            return ClientboundBlockEntityDataPacket.create(this, (blockEntity) -> NBT);
        }

        @Override
        public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
            load(pkt.getTag());
        }

        /*
            Returns the maximum amount of energy the crystal can store.
         */
        public int getCapacity() {
            int s = crystal.getSize();
            double sigmoid = ReikaMathLibrary.cosInterpolation(0, 200, Math.min(s, 200));
            double linear = 100000 * Math.pow(1.03125, s);//ReikaMathLibrary.roundUpToX(1000, (int)(100000*Math.pow(1.03125, s)));
            double factor = Math.min(1, 0.8 * ReikaMathLibrary.logbase(2 + s, 64));
            int round = 1000 * ReikaMathLibrary.intpow2(10, 1 + (int) (Math.log10(1 + s)));
            return (int) (2500 + 1000000000 * sigmoid * factor + linear);
//            return ReikaMathLibrary.roundUpToX(round, (int)(2500+1000000000*sigmoid*factor+linear));
        }

        @Override
        public Collection<BlockPos> getBlocks(DecimalPosition pos) {
            ArrayList<BlockPos> li = new ArrayList<>();
            BlockPos c = pos.getCoordinate();
            li.add(c);
            li.addAll(ReikaBlockPosHelper.getAdjacentCoordinates(c));
            return li;
        }

        @Override
        public double getInitialTheta(Random rand, int trail) {
            return -5 + rand.nextDouble() * 10 + rand.nextDouble() * 80 - 40;
        }

        @Override
        public double getInitialPhi(Random rand, int trail) {
            double base = 360D / 9 * trail;
            return base - 10 + rand.nextDouble() * 20;
        }

        public void addLocation(BlockPos c) {
            crystal.addBlockCoordinate(c);
        }

        public void removeLocation(BlockPos c) {
            if (crystal.hasBlock(c)) {
                crystal.remove(c);
                BlockArray b = new BlockArray();
                b.recursiveMultiAddWithBounds(level, c.getX(), c.getY(), c.getZ(), crystal.getMinX(), crystal.getMinY(), crystal.getMinZ(), crystal.getMaxX(), crystal.getMaxY(), crystal.getMaxZ(), this.getBlockState().getBlock(), GeoBlocks.RF_CRYSTAL.get());
                List<BlockPos> li = new ArrayList<>(crystal.list());
                for (BlockPos c2 : li) {
                    if (!b.hasBlock(c2)) {
                        crystal.remove(c2);
                        ReikaWorldHelper.dropAndDestroyBlockAt(level, c2, null, true, true);
                    }
                }
            }
        }

        public void activate() {
            isActivated = true;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

    }

}
