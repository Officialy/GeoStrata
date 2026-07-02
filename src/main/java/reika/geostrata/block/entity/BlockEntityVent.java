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
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.geostrata.base.VentType;
//import reika.rotarycraft.api.interfaces.EnvironmentalHeatSource;

import java.util.List;
import java.util.Random;

public class BlockEntityVent extends BlockEntity /*MinerBlock, */ {

    public static final Random rand = new Random();
    private VentType ventType;
    private boolean plugged;
    public int activeTimer = 0;

    public BlockEntityVent(BlockEntityType<?> type, VentType ventType, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.ventType = ventType;
    }

    public void activate() {
//        GeoStrata.LOGGER.info("Activating vent");
        activeTimer = 40 + rand.nextInt(600);
        setChanged();
        level.sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), 2);
        ReikaSoundHelper.playSoundAtBlock(level, worldPosition, SoundEvents.FLINTANDSTEEL_USE);
        if (this.getVentType() == VentType.GAS) {
            if (level.getBlockState(worldPosition.above()).getBlock() == Blocks.FIRE || level.getBlockState(worldPosition.above()).getBlock() == Blocks.LAVA) {
                this.explode(1.5F, level, worldPosition);
            }
        }
    }

    public void updateEntity(Level level, BlockPos pos) {
        if (ventType != null) {
            if (this.isActive()) {
                activeTimer--;
                setChanged();
                level.sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), 2);
                if (activeTimer == 0) {
                    level.sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), 2);
                }
//                GeoStrata.LOGGER.info("Activating vent at " + pos + " with type " + ventType + " timer " + activeTimer); //for debugging
                if (this.getVentType() == VentType.GAS) {
                    if (level.getBlockState(pos.above()).getBlock() == Blocks.FIRE || level.getBlockState(pos).getBlock() == Blocks.LAVA) {
                        this.explode(1.5F, level, pos);
                    }
                }

                if (!level.isClientSide()) {
                    if (ventType.dealsDamage()) {
                        AABB box = this.getEffectBox();
                        List<LivingEntity> li = level.getEntitiesOfClass(LivingEntity.class, box);
                        for (LivingEntity e : li) {
                            e.hurt(ventType.getDamageSrc(e), ventType.damage);
                            if (ventType == VentType.FIRE || ventType == VentType.LAVA || ventType == VentType.PYRO)
                                e.igniteForSeconds(ventType.damage);
                        }
                    }

                    AABB box = ventType.getEffectBox(this);
                    if (level.getBlockState(pos).getBlock() instanceof FarmlandBlock) {
                        List<LivingEntity> li = level.getEntitiesOfClass(LivingEntity.class, box);
                        for (LivingEntity e : li) {
                            ventType.applyEntityEffect(e);
                        }
                    } else if (ventType == VentType.SMOKE) {
                        assert false;
                        List<LivingEntity> li = level.getEntitiesOfClass(LivingEntity.class, box);
                        for (LivingEntity e : li) {
                            e.setAirSupply(Math.max(0, e.getAirSupply() - 1));
                        }
                    }
                    ventType.doAoE(level, pos, rand);

                    if (rand.nextInt(20) == 0) {
                        int temp = ventType.getTemperature();
                        for (int i = 1; i < 5; i++) {
                            ReikaWorldHelper.temperatureEnvironment(level, new BlockPos(pos.getX(), pos.getY() + i, pos.getZ()), temp);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        tag.putInt("ventType", ventType.ordinal());
        tag.putInt("activeTimer", activeTimer);
        return tag;
    }

    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt("ventType", ventType.ordinal());
        output.putInt("activeTimer", activeTimer);
    }

    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        ventType = VentType.values()[input.getIntOr("ventType", 0)];
        activeTimer = input.getIntOr("activeTimer", 0);
    }

    public AABB getEffectBox() {
        int i;
        for (i = 1; i < 4; i++) {
            if (this.isBlocking(level, worldPosition.above()))
                break;
        }
        return new AABB(worldPosition.above().getX(), worldPosition.above().getY(), worldPosition.above().getZ(), worldPosition.offset(1, i + 1, 1).getX(), worldPosition.offset(1, i + 1, 1).getY(), worldPosition.offset(1, i + 1, 1).getZ()); //todo test
    }

    public boolean canFire() {
        return canTick(level, worldPosition) && !this.isBlocking(level, worldPosition.above());
    }

    public boolean isActive() {
        return activeTimer > 0 && canTick(level, getBlockPos());
    }

    private static boolean canTick(Level world, BlockPos pos) {
        return ReikaWorldHelper.isRadiusLoaded(world, pos, 2);
    }

    public void checkPlug(BlockPos pos, Level level) {
        boolean last = plugged;
        plugged = this.isBlocking(level, pos.above());
        if (plugged && !last) { //just got plugged, firing
            this.explode(1, level, pos);
        }
    }

    private void explode(float factor, Level level, BlockPos pos) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 0);
        boolean fire = ventType == VentType.FIRE || ventType == VentType.LAVA || ventType == VentType.PYRO;
        level.explode(null, pos.getX(), pos.getY(), pos.getZ(), factor * ventType.getExplosionSizeFactor(), fire, Level.ExplosionInteraction.BLOCK);
    }

    private boolean isBlocking(Level world, BlockPos pos) {
        return world.getBlockState(pos).isSolid();
    }

    public VentType getVentType() {
        return ventType;
    }
}
