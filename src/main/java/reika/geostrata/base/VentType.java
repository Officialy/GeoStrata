package reika.geostrata.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import reika.dragonapi.instantiable.Interpolation;
import reika.dragonapi.instantiable.event.BlockTickEvent;
import reika.dragonapi.libraries.ReikaAABBHelper;
import reika.dragonapi.libraries.ReikaEntityHelper;
import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.dragonapi.libraries.java.ReikaRandomHelper;
import reika.dragonapi.libraries.level.ReikaWorldHelper;
import reika.dragonapi.libraries.registry.ReikaParticleHelper;
import reika.geostrata.GeoStrata;
import reika.geostrata.block.BlockVent;
import reika.geostrata.block.entity.BlockEntityVent;
import reika.geostrata.registry.GeoBlocks;

import java.util.Random;

public enum VentType implements StringRepresentable {
    STEAM("steam", 1),
    SMOKE("smoke", 0),
    FIRE("fire", 2),
    LAVA("lava", 8),
    GAS("gas", 0),
    WATER("water", 0),
    ENDER("ender", 0),
    PYRO("pyro", 15),
    CRYO("cryo", 4);

    public static final VentType[] list = values();

    static {
        STEAM.heightCurve.addPoint(4, 0);
        STEAM.heightCurve.addPoint(24, 40);

        SMOKE.heightCurve.addPoint(4, 0);
        SMOKE.heightCurve.addPoint(24, 40);

        FIRE.heightCurve.addPoint(4, 20);
        FIRE.heightCurve.addPoint(24, 40);
        FIRE.heightCurve.addPoint(32, 0);

        LAVA.heightCurve.addPoint(4, 70);
        LAVA.heightCurve.addPoint(10, 40);
        LAVA.heightCurve.addPoint(14, 30);
        LAVA.heightCurve.addPoint(20, 0);

        GAS.heightCurve.addPoint(4, 10);
        GAS.heightCurve.addPoint(16, 20);
        GAS.heightCurve.addPoint(24, 10);
        GAS.heightCurve.addPoint(32, 0);

        WATER.heightCurve.addPoint(24, 0);
        WATER.heightCurve.addPoint(30, 10);
        WATER.heightCurve.addPoint(40, 20);
        WATER.heightCurve.addPoint(60, 30);

        CRYO.heightCurve.addPoint(40, 0);
        CRYO.heightCurve.addPoint(50, 5);
        CRYO.heightCurve.addPoint(60, 20);


        STEAM.heightCurveNether.addPoint(40, 0);
        STEAM.heightCurveNether.addPoint(60, 40);
        STEAM.heightCurveNether.addPoint(120, 40);

        SMOKE.heightCurveNether.addPoint(10, 40);
        SMOKE.heightCurveNether.addPoint(120, 40);

        FIRE.heightCurveNether.addPoint(10, 60);
        FIRE.heightCurveNether.addPoint(30, 80);
        FIRE.heightCurveNether.addPoint(80, 80);
        FIRE.heightCurveNether.addPoint(110, 20);

        LAVA.heightCurveNether.addPoint(4, 80);
        LAVA.heightCurveNether.addPoint(30, 80);
        LAVA.heightCurveNether.addPoint(40, 50);
        LAVA.heightCurveNether.addPoint(70, 40);
        LAVA.heightCurveNether.addPoint(100, 10);

        GAS.heightCurveNether.addPoint(4, 20);
        GAS.heightCurveNether.addPoint(20, 50);
        GAS.heightCurveNether.addPoint(30, 40);
        GAS.heightCurveNether.addPoint(40, 20);
        GAS.heightCurveNether.addPoint(110, 20);

        PYRO.heightCurveNether.addPoint(4, 30);
        PYRO.heightCurveNether.addPoint(20, 45);
        PYRO.heightCurveNether.addPoint(30, 30);
        PYRO.heightCurveNether.addPoint(40, 10);
        PYRO.heightCurveNether.addPoint(80, 0);
    }

    public final int damage;
    private final Interpolation heightCurve = new Interpolation(false);
    private final Interpolation heightCurveNether = new Interpolation(false);
    private final String name;

    VentType(String name, int dmg) {
        damage = dmg;
        heightCurve.addPoint(0, 0);
        heightCurveNether.addPoint(0, 0);
        heightCurve.addPoint(72, 0);
        heightCurveNether.addPoint(128, 0);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean dealsDamage() {
        return damage > 0;
    }

    public double getSpawnWeight(int y, boolean nether) {
        return nether ? heightCurveNether.getValue(y) : heightCurve.getValue(y);
    }

    public boolean canGenerateInOverworld() {
        return switch (this) {
            case ENDER, PYRO -> false;
            default -> true;
        };
    }

    public boolean canGenerateInNether() {
        return switch (this) {
            case STEAM, SMOKE, FIRE, LAVA, GAS, PYRO -> true;
            default -> false;
        };
    }

    public AABB getEffectBox(BlockEntityVent te) {
        return switch (this) {
            case WATER, SMOKE -> te.getEffectBox();
            case GAS, PYRO -> ReikaAABBHelper.getBlockAABB(te).expandTowards(3, 3, 3).move(0, 2, 0);
            case ENDER -> ReikaAABBHelper.getBlockAABB(te).expandTowards(2, 2, 2).move(0, 1, 0);
            default -> null;
        };
    }

    public void applyEntityEffect(LivingEntity e) {
        switch (this) {
            case WATER:
                if (e instanceof EnderMan) {
                    e.hurt(e.damageSources().drown(), 1);
                    e.randomTeleport(1, 1, 1, true);
                } else {
                    e.clearFire();
                }
                break;
            case SMOKE:
                e.serializeNBT().putLong(BlockVent.SMOKE_VENT_TAG, e.level().getGameTime());
                break;
            case GAS:
                e.addEffect(new MobEffectInstance(MobEffects.POISON, 20 + BlockEntityVent.rand.nextInt(200), BlockEntityVent.rand.nextInt(4) == 0 ? 1 : 0));
                break;
            case ENDER:
                double ox = e.getX();
                double oy = e.getY();
                double oz = e.getZ();
                boolean flag = true;
                int tries = 0;
                while (flag && tries < 40) {
                    double rx = ReikaRandomHelper.getRandomPlusMinus(ox, 6);
                    double ry = ReikaRandomHelper.getRandomPlusMinus(oy, 1);
                    double rz = ReikaRandomHelper.getRandomPlusMinus(oz, 6);
                    e.teleportTo(rx, ry, rz); //setpositionandupdate
                    for (VoxelShape voxelshape : e.level().getBlockCollisions(e, e.getBoundingBox())) {
                        if (!voxelshape.isEmpty()) {
                            flag = e.level().containsAnyLiquid(e.getBoundingBox());
                        }
                    }
                    tries++;
                }
                if (!flag) {
//                    ReikaSoundHelper.playSoundFromServer(e.getLevel(), e.getX(), e.getX(), e.getZ(), SoundEvents.ENDERMAN_TELEPORT.getLocation(), 1, 1, true);
                    e.level().playSound(null, ox, oy, oz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                break;
            case PYRO:
                e.setSecondsOnFire(60);
                ReikaEntityHelper.damageArmor(e, 4);
                break;
            default:
                break;
        }
    }

    private int getHorizontalAoERange() {
        return 6;
    }

    public void doAoE(Level world, BlockPos pos, Random rand) {
        int r = this.getHorizontalAoERange();
        if (!world.hasChunksAt(pos.getX() - r - 2, pos.getY() - r - 2, pos.getZ() - r - 2, pos.getX() + r + 2, pos.getY() + r + 2, pos.getZ() + r + 2)) { //hasChunksAt
            return;
        }
        switch (this) {
            case WATER -> {
                if (rand.nextInt(20) == 0) {
                    int rx = ReikaRandomHelper.getRandomPlusMinus(pos.getX(), r);
                    int ry = ReikaRandomHelper.getRandomPlusMinus(pos.getY(), 1);
                    int rz = ReikaRandomHelper.getRandomPlusMinus(pos.getZ(), r);
                    BlockState b = world.getBlockState(new BlockPos(rx, ry, rz));
                    if (b.getBlock() == Blocks.FARMLAND) {
                        world.setBlock(new BlockPos(rx, ry, rz), Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7), 3);
                    }
                    if (rand.nextInt(3) == 0) {
                        BlockTickEvent.fire(b, (ServerLevel) world, new BlockPos(rx, ry, rz), world.random, BlockTickEvent.UpdateFlags.getForcedUnstoppableTick() + BlockTickEvent.UpdateFlags.NATURAL.flag);
                    } //todo serverlevel casting, bad!!
                }
            }
            case PYRO -> {
                if (rand.nextInt(10) == 0) {
                    int rx = ReikaRandomHelper.getRandomPlusMinus(pos.getX(), r);
                    int ry = ReikaRandomHelper.getRandomPlusMinus(pos.getY(), 1);
                    int rz = ReikaRandomHelper.getRandomPlusMinus(pos.getZ(), r);
                    if (ReikaWorldHelper.isExposedToAir(world, rx, ry, rz)) {
                        Block b = world.getBlockState(new BlockPos(rx, ry, rz)).getBlock();
                        if (b == Blocks.STONE || b == Blocks.COBBLESTONE || b == Blocks.STONE_BRICKS || b == Blocks.DEEPSLATE || b == Blocks.DEEPSLATE_BRICKS || b == Blocks.COBBLED_DEEPSLATE) {
                            world.setBlock(new BlockPos(rx, ry, rz), Blocks.LAVA.defaultBlockState(), 3);
                        }
                    }
                }
            }
            case CRYO -> {
                if (rand.nextInt(20) == 0) {
                    int rx = ReikaRandomHelper.getRandomPlusMinus(pos.getX(), r);
                    int ry = ReikaRandomHelper.getRandomPlusMinus(pos.getY(), 1);
                    int rz = ReikaRandomHelper.getRandomPlusMinus(pos.getZ(), r);
                    BlockState b = world.getBlockState(new BlockPos(rx, ry, rz));
//                    if (b.is(Blocks.SNOW)) {
//                        GeoStrata.LOGGER.info(b.getValue(SnowLayerBlock.LAYERS));
//                    }
                    if (b.getBlock() == Blocks.WATER) {
                        world.setBlock(new BlockPos(rx, ry, rz), Blocks.ICE.defaultBlockState(), 3);
                    } else if (Block.isFaceFull(b.getCollisionShape(world, new BlockPos(rx, ry, rz).below()), Direction.UP) && b.getBlock() != GeoBlocks.CRYO_VENT.get() && world.getBlockState(new BlockPos(rx, ry, rz).above()).getBlock() == Blocks.AIR) {
                        world.setBlock(new BlockPos(rx, ry + 1, rz), Blocks.SNOW.defaultBlockState(), 3);
                    }
                }
            }
            default -> {
            }
        }
    }

    public int getTemperature() {
        return switch (this) {
            case FIRE -> 400;
            case LAVA -> 800;
            case PYRO -> 1200;
            case WATER -> 15;
            case CRYO -> -40;
            default -> 25;
        };
    }

    public DamageSource getDamageSrc(LivingEntity e) {
        return switch (this) {
            case STEAM, FIRE -> e.damageSources().inFire();
            case LAVA, PYRO -> e.damageSources().lava();
            default ->  e.damageSources().generic();
        };
    }

    public ReikaParticleHelper getParticle() {
        return switch (this) {
            case STEAM -> ReikaParticleHelper.CLOUD;
            case SMOKE -> ReikaParticleHelper.LARGESMOKE;
            case FIRE -> ReikaParticleHelper.FLAME;
            case LAVA, PYRO -> ReikaParticleHelper.LAVA;
            case GAS -> ReikaParticleHelper.AMBIENTMOBSPELL;
            case WATER -> ReikaParticleHelper.RAIN;
            case ENDER -> ReikaParticleHelper.PORTAL;
            case CRYO -> ReikaParticleHelper.FIREWORK;
        };
    }

    public float getExplosionSizeFactor() {
        return switch (this) {
            case WATER, CRYO -> 1.5F;
            case FIRE -> 3;
            case LAVA -> 3.5F;
            case STEAM, GAS -> 4;
            case PYRO -> 6;
            default -> 2;
        };
    }

    public double getParticleYOffset() {
        return this == ENDER ? 0.5 : 0;
    }

    public int getParticleRate() {
        return this == PYRO ? 3 : 1;
    }

    public int getSoundInterval() {
        return switch (this) {
            case PYRO -> 2;
            case ENDER -> 20;
            default -> 4;
        };
    }

    public boolean isSelfLit() {
        return this == FIRE || this == LAVA || this == PYRO;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
