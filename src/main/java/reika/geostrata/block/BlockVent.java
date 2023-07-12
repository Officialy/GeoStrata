package reika.geostrata.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import reika.dragonapi.ModList;
import reika.dragonapi.libraries.io.ReikaSoundHelper;
import reika.dragonapi.libraries.registry.ReikaItemHelper;
import reika.dragonapi.libraries.registry.ReikaParticleHelper;
import reika.geostrata.base.VentType;
import reika.geostrata.block.entity.BlockEntityVent;
import reika.geostrata.block.entity.BlockEntityVentRoC;
import reika.geostrata.registry.GeoBlockEntities;

public class BlockVent extends Block implements EntityBlock {

    public final VentType type;
    public static final String SMOKE_VENT_TAG = "geosmoked";

    public BlockVent(Properties properties, VentType type) {
        super(properties.noOcclusion().randomTicks());
        this.type = type;
    }

    @Override
    public boolean isRandomlyTicking(BlockState p_49921_) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        BlockEntityVent te = (BlockEntityVent) level.getBlockEntity(pos);
        if (!level.hasNeighborSignal(pos) && te.canFire())
            te.activate();
//        GeoStrata.LOGGER.info(te.canFire());
        level.sendBlockUpdated(pos, state, state, 3); //todo this.tickRate(level)+rand.nextInt(2400)
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return switch (type) {
            case STEAM -> new BlockEntityVent(GeoBlockEntities.STEAM_VENT.get(), VentType.STEAM, pPos, pState);
            case PYRO -> new BlockEntityVent(GeoBlockEntities.PYRO_VENT.get(), VentType.PYRO, pPos, pState);
            case CRYO -> new BlockEntityVent(GeoBlockEntities.CRYO_VENT.get(), VentType.CRYO, pPos, pState);
            case FIRE -> new BlockEntityVent(GeoBlockEntities.FIRE_VENT.get(), VentType.FIRE, pPos, pState);
            case WATER -> new BlockEntityVent(GeoBlockEntities.WATER_VENT.get(), VentType.WATER, pPos, pState);
            case LAVA -> new BlockEntityVent(GeoBlockEntities.LAVA_VENT.get(), VentType.LAVA, pPos, pState);
            case GAS -> new BlockEntityVent(GeoBlockEntities.GAS_VENT.get(), VentType.GAS, pPos, pState);
            case ENDER -> new BlockEntityVent(GeoBlockEntities.ENDER_VENT.get(), VentType.ENDER, pPos, pState);
            case SMOKE -> new BlockEntityVent(GeoBlockEntities.SMOKE_VENT.get(), VentType.SMOKE, pPos, pState);
        };
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        var te = (BlockEntityVent) level.getBlockEntity(pos);
        var ventType = this != null ? te.getVentType() : VentType.STEAM; //Default to steam just incase ventType is null

//        GeoStrata.LOGGER.info("Vent update timer: "+ te.activeTimer);
        ventParticles(te.isActive(), te, ventType, level, pos, rand);
//        ventSounds(te.isActive(), te.activeTimer, state, te, ventType, level, pos, rand);
//        GeoStrata.LOGGER.info(Mth.roundToward(te.activeTimer, 2));
        ventSounds(te.isActive(), te.activeTimer, state, te, ventType, level, pos, rand);
        ventSounds(te.isActive(), Mth.roundToward(te.activeTimer, 2), state, te, ventType, level, pos, rand);
    }

    public void ventParticles(boolean isActive, BlockEntityVent te, VentType ventType, Level level, BlockPos pos, RandomSource rand) {
        var p = ventType.getParticle();
        if (isActive) {
            if (p != null) {
                int n = p == ReikaParticleHelper.FLAME ? 3 : p == ReikaParticleHelper.RAIN ? 8 : 1;
                n *= ventType.getParticleRate();
                for (int i = 0; i < n; i++) {
                    double px = pos.getX() + rand.nextDouble();
                    double py = pos.getY() + 0.5 + rand.nextDouble();
                    double pz = pos.getZ() + rand.nextDouble();
                    if (p == ReikaParticleHelper.AMBIENTMOBSPELL || p == ReikaParticleHelper.LAVA || p == ReikaParticleHelper.RAIN)
                        py += rand.nextDouble() * 2;
                    double vx = 0;
                    double vz = 0;
                    double vy = 0.25 + rand.nextDouble() / 2;
                    p.spawnAt(level, px, py + ventType.getParticleYOffset(), pz, vx, vy, vz);
                }
            }
        }
    }

    public void ventSounds(boolean isActive, int timer, BlockState state, BlockEntityVent te, VentType ventType, Level level, BlockPos pos, RandomSource rand) {
        if (isActive) {
            if (timer > 0 && timer % ventType.getSoundInterval() == 0) {
                level.sendBlockUpdated(pos, state, state, 2);
                switch (ventType) {
                    case FIRE, LAVA, PYRO ->
                            ReikaSoundHelper.playSoundAtBlock(level, pos, SoundEvents.GHAST_SHOOT, 0.25F, 1);
                    case STEAM, GAS ->
                            ReikaSoundHelper.playSoundAtBlock(level, pos, SoundEvents.LAVA_EXTINGUISH, 0.25F, 1.5F);
                    case SMOKE ->
                            ReikaSoundHelper.playSoundAtBlock(level, pos, SoundEvents.LAVA_EXTINGUISH, 0.25F, 0.25F);
                    case WATER -> {
                        if (timer % 32 == 0) {
                            ReikaSoundHelper.playSoundAtBlock(level, pos, SoundEvents.WATER_AMBIENT, 2F, 1F);
                            ReikaSoundHelper.playSoundAtBlock(level, pos, SoundEvents.WATER_AMBIENT, 2F, 1F);
                        }
                    }
                    case ENDER ->
                            ReikaSoundHelper.playSoundAtBlock(level, pos, SoundEvents.PORTAL_AMBIENT, 0.25F, rand.nextFloat() * 0.4F + 0.8F);
                    case CRYO ->
                            ReikaSoundHelper.playSoundAtBlock(level, pos, SoundEvents.SILVERFISH_STEP, 0.25F, 0.25F);
                }
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : ((level1, pPos, pState1, pBlockEntity) -> {
                ((BlockEntityVent) pBlockEntity).updateEntity(level1, pPos);
        });
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos pos2, boolean bool) {
        BlockEntityVent b = (BlockEntityVent) level.getBlockEntity(pos);
        b.checkPlug(pos, level);
    }

    /**
     * Determines if the player can harvest this block, obtaining it's drops when the block is destroyed.
     *
     * @param state
     * @param world  The current world
     * @param pos    The block's current position
     * @param player The player damaging the block
     * @return True to spawn the drops
     */
    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
        if (EnchantmentHelper.getEnchantments(player.getMainHandItem()).equals(Enchantments.SILK_TOUCH)) {
            player.awardStat(Stats.BLOCK_MINED.get(this), 1);
            player.causeFoodExhaustion(0.025F);
            ReikaItemHelper.dropItem((Level) world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(this, 1));
        }
        return false;
    }

}
