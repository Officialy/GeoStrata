/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.geostrata.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import reika.rotarycraft.api.interfaces.EnvironmentalHeatSource;

import java.util.List;

public class BlockLavaRock extends Block implements EnvironmentalHeatSource {

	public static final IntegerProperty BLOCK_HEIGHT_STATE = IntegerProperty.create("height", 0, 3);

	public static final VoxelShape AABB = Block.box(0, 0, 0, 16, 14.75, 16);

	public static final VoxelShape AABB1 = Block.box(0, 0, 0, 16, 14.85, 16);

	public static final VoxelShape AABB2 = Block.box(0, 0, 0, 16, 15, 16);

	public static final VoxelShape AABB3 = Block.box(0, 0, 0, 16, 15.15, 16);

	public BlockLavaRock() {
		super(BlockBehaviour.Properties.copy(Blocks.STONE).lightLevel((p_50886_) -> 14).noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(BLOCK_HEIGHT_STATE, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BLOCK_HEIGHT_STATE);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext collisionContext) {
		return switch (state.getValue(BLOCK_HEIGHT_STATE)) {
			default -> AABB;
			case 1 -> AABB1;
			case 2 -> AABB2;
			case 3 -> AABB3;
		};
	}

	@Override
	public void stepOn(Level world, BlockPos pos, BlockState state, Entity e) {
		if (e instanceof ItemEntity || e instanceof ExperienceOrb || e instanceof Projectile)
			return;
		int blockHeight = state.getValue(BLOCK_HEIGHT_STATE);
		if (blockHeight == 3)
			return;
		boolean doEffect = true;
		if (e instanceof LivingEntity) {
			doEffect = !((LivingEntity)e).hasEffect(MobEffects.FIRE_RESISTANCE);
			if (e instanceof Player) {
				doEffect &= !((Player)e).isCreative();
			}
		}
		if (doEffect) {
			e.hurt(blockHeight == 0 ? DamageSource.LAVA : DamageSource.IN_FIRE, 3-blockHeight);
			if (blockHeight == 0) { //lava is 15
				e.setSecondsOnFire(8);
			}
			else if (blockHeight == 1) {
				e.setSecondsOnFire(4);
			}
		}
	}

	@Override
	public RenderShape getRenderShape(BlockState p_60550_) {
		return RenderShape.MODEL;
	}

	@Override
	public boolean canBeReplaced(BlockState target, BlockPlaceContext context) {
		return target.getBlock() == this || target.getBlock() == Blocks.STONE || target.canBeReplaced(context);// todo check || target.isReplaceableOreGen(world, pos, Blocks.STONE);
	}

	@Override
	public SourceType getSourceType(BlockGetter getter, BlockPos pos) {
		return SourceType.LAVA;
	}

	@Override
	public boolean isActive(BlockGetter getter, BlockPos pos) {
		return true;
	}

	private int getEffectiveTemperature(int blockHeight) { //0 is lava
		return 750-(blockHeight%4)*150;
	}

}
