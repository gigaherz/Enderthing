package dev.gigaherz.enderthing.blocks;

import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import dev.gigaherz.enderthing.gui.Containers;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.Random;

public class EnderKeyChestBlock extends AbstractChestBlock<EnderKeyChestBlockEntity> implements SimpleWaterloggedBlock
{
    public static final DirectionProperty FACING = EnderChestBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public EnderKeyChestBlock(Properties properties)
    {
        super(properties, () -> EnderKeyChestBlockEntity.TYPE); // Material.ROCK
        registerDefaultState(this.getStateDefinition().any()
                .setValue(WATERLOGGED, false)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState p_225536_1_, Level p_225536_2_, BlockPos p_225536_3_, boolean p_225536_4_)
    {
        return DoubleBlockCombiner.Combiner::acceptNone;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
    {
        return SHAPE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
    {
        return level.isClientSide ? createTickerHelper(blockEntityType, this.blockEntityType(), EnderKeyChestBlockEntity::lidAnimationTick) : null;
    }

    public BlockEntityType<? extends EnderKeyChestBlockEntity> blockEntityType()
    {
        return this.blockEntityType.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        return new EnderKeyChestBlockEntity(blockPos, blockState);
    }

    @Deprecated
    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player)
    {
        return getItem(world, pos, Screen.hasShiftDown() || (player.getAbilities().instabuild && Screen.hasControlDown()));
    }

    @Override
    public FluidState getFluidState(BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.defaultFluidState() : Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit)
    {
        BlockEntity te = worldIn.getBlockEntity(pos);

        if (!(te instanceof EnderKeyChestBlockEntity))
            return InteractionResult.PASS;

        if (worldIn.getBlockState(pos.above()).isRedstoneConductor(worldIn, pos))
            return InteractionResult.FAIL;

        if (worldIn.isClientSide)
            return InteractionResult.SUCCESS;

        if (player.isShiftKeyDown())
        {
            ItemHandlerHelper.giveItemToPlayer(player, getItem(worldIn, pos, false));
            worldIn.setBlockAndUpdate(pos, Blocks.ENDER_CHEST.defaultBlockState()
                    .setValue(EnderKeyChestBlock.WATERLOGGED, state.getValue(EnderChestBlock.WATERLOGGED))
                    .setValue(EnderKeyChestBlock.FACING, state.getValue(EnderChestBlock.FACING)));
            return InteractionResult.SUCCESS;
        }

        EnderKeyChestBlockEntity chest = (EnderKeyChestBlockEntity) te;

        if (player instanceof ServerPlayer)
            Containers.openBlockGui((ServerPlayer) player, chest);

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(FACING, context.getPlayer().getDirection().getOpposite())
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        worldIn.setBlockAndUpdate(pos, state.setValue(FACING, placer.getDirection().getOpposite()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand)
    {
        for (int i = 0; i < 3; ++i)
        {
            int xOffset = rand.nextInt(2) * 2 - 1;
            int zOffset = rand.nextInt(2) * 2 - 1;
            double xPos = pos.getX() + 0.5 + xOffset * 0.25;
            double yPos = pos.getY() + rand.nextFloat();
            double zPos = pos.getZ() + 0.5 + zOffset * 0.25;
            double xSpeed = rand.nextFloat() * xOffset;
            double ySpeed = rand.nextFloat() * 0.125 - 0.0625;
            double zSpeed = rand.nextFloat() * zOffset;
            worldIn.addParticle(ParticleTypes.PORTAL, xPos, yPos, zPos, xSpeed, ySpeed, zSpeed);
        }
    }

    @Deprecated
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        if (stateIn.getValue(WATERLOGGED))
        {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }

        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Deprecated
    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type)
    {
        return false;
    }

    @Deprecated
    @Override
    public void tick(BlockState p_153203_, ServerLevel p_153204_, BlockPos p_153205_, Random p_153206_) {
        BlockEntity blockentity = p_153204_.getBlockEntity(p_153205_);
        if (blockentity instanceof EnderChestBlockEntity chestBE)
        {
            chestBE.recheckOpen();
        }
    }

    @Deprecated
    @Override
    public boolean hasAnalogOutputSignal(BlockState state)
    {
        return true;
    }

    @Deprecated
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos)
    {
        if (level.getBlockEntity(pos) instanceof EnderKeyChestBlockEntity be && be.hasInventory())
            return getRedstoneSignalFromContainer(be.getInventory());
        return 0;
    }

    public static int getRedstoneSignalFromContainer(@Nullable IItemHandler handler) {
        if (handler == null) {
            return 0;
        }

        int nonEmptyStacks = 0;
        float fullness = 0.0F;

        for(int j = 0; j < handler.getSlots(); ++j) {
            ItemStack itemstack = handler.getStackInSlot(j);
            if (!itemstack.isEmpty())
            {
                fullness += (float)itemstack.getCount() / (float)Math.min(handler.getSlotLimit(j), itemstack.getMaxStackSize());
                ++nonEmptyStacks;
            }
        }

        fullness /= (float)handler.getSlots();
        return Mth.floor(fullness * 14.0F) + (nonEmptyStacks > 0 ? 1 : 0);
    }

    @Deprecated
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Deprecated
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    private static ItemStack getItem(BlockGetter world, BlockPos pos, boolean asChest)
    {
        BlockEntity te = world.getBlockEntity(pos);

        if (te instanceof EnderKeyChestBlockEntity te1)
        {
            long id = te1.getKey();
            boolean priv = te1.isPrivate();

            return asChest ? KeyUtils.getKeyChest(id, priv, te1.getPlayerBound()) : KeyUtils.getLock(id, priv);
        }

        return asChest ? new ItemStack(Enderthing.KEY_CHEST) : new ItemStack(Enderthing.LOCK);
    }
}