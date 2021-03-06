package ru.betterend.blocks;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion;
import ru.betterend.blocks.basis.BlockPedestal;
import ru.betterend.blocks.entities.EternalPedestalEntity;
import ru.betterend.registry.EndBlocks;
import ru.betterend.registry.EndItems;
import ru.betterend.rituals.EternalRitual;

public class EternalPedestal extends BlockPedestal {
	public static final BooleanProperty ACTIVATED = BlockProperties.ACTIVE;
	
	public EternalPedestal() {
		super(EndBlocks.FLAVOLITE_RUNED_ETERNAL);
		this.setDefaultState(this.getDefaultState().with(ACTIVATED, false));
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		ActionResult result = super.onUse(state, world, pos, player, hand, hit);
		if (result.equals(ActionResult.SUCCESS)) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof EternalPedestalEntity) {
				EternalPedestalEntity pedestal = (EternalPedestalEntity) blockEntity;
				BlockState updatedState = world.getBlockState(pos);
				if (pedestal.isEmpty() && updatedState.get(ACTIVATED)) {
					if (pedestal.hasRitual()) {
						EternalRitual ritual = pedestal.getRitual();
						ritual.removePortal();
					}
					world.setBlockState(pos, updatedState.with(ACTIVATED, false));
				} else {
					ItemStack itemStack = pedestal.getStack(0);
					if (itemStack.getItem() == EndItems.ETERNAL_CRYSTAL) {
						world.setBlockState(pos, updatedState.with(ACTIVATED, true));
						if (pedestal.hasRitual()) {
							pedestal.getRitual().checkStructure();
						} else {
							EternalRitual ritual = new EternalRitual(world, pos);
							ritual.checkStructure();
						}
					}
				}
			}
		}
		return result;
	}
	
	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
		BlockState updated = super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
		if (!updated.isOf(this)) return updated;
		if (!this.isPlaceable(updated)) {
			return updated.with(ACTIVATED, false);
		}
		return updated;
	}
	
	@Override
	public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
		return 0.0F;
	}
	
	@Override
	public float getBlastResistance() {
		return Blocks.BEDROCK.getBlastResistance();
	}
	
	@Override
	public boolean shouldDropItemsOnExplosion(Explosion explosion) {
		return false;
	}
	
	@Override
	public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
		if (state.isOf(this)) {
			BlockProperties.PedestalState currentState = state.get(BlockProperties.PEDESTAL_STATE);
			if (currentState.equals(BlockProperties.PedestalState.BOTTOM) || currentState.equals(BlockProperties.PedestalState.PILLAR)) {
				return Lists.newArrayList();
			}
		}
		List<ItemStack> drop = Lists.newArrayList();
		BlockEntity blockEntity = builder.getNullable(LootContextParameters.BLOCK_ENTITY);
		if (blockEntity != null && blockEntity instanceof EternalPedestalEntity) {
			EternalPedestalEntity pedestal = (EternalPedestalEntity) blockEntity;
			if (!pedestal.isEmpty()) {
				drop.add(pedestal.getStack(0));
			}
		}
		return drop;
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
		super.appendProperties(stateManager);
		stateManager.add(ACTIVATED);
	}

	@Override
	public BlockEntity createBlockEntity(BlockView world) {
		return new EternalPedestalEntity();
	}
}
