package gigaherz.enderthing.blocks;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.KeyUtils;
import gigaherz.enderthing.client.ClientEvents;
import gigaherz.enderthing.gui.Containers;
import gigaherz.enderthing.util.ILongAccessor;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class EnderKeyChestBlock extends Block
{
    public static final DirectionProperty FACING = EnderChestBlock.FACING;
    protected static final VoxelShape SHAPE = Block.makeCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public boolean isPrivate()
    {
        return false;
    }

    public static class Private extends EnderKeyChestBlock
    {
        public static final BooleanProperty BOUND = BooleanProperty.create("bound");

        @Override
        public boolean isPrivate()
        {
            return true;
        }

        public Private(Properties properties)
        {
            super(properties);
            setDefaultState(this.getDefaultState()
                    .with(BOUND, false)
                    .with(FACING, Direction.NORTH));
        }

        @Override
        protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
        {
            builder.add(FACING, BOUND);
        }

        @Nullable
        @Override
        public TileEntity createTileEntity(BlockState state, IBlockReader world)
        {
            return new EnderKeyChestTileEntity.Private();
        }
    }

    public EnderKeyChestBlock(Properties properties)
    {
        super(properties); // Material.ROCK
        setDefaultState(this.getStateContainer().getBaseState()
                .with(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        return SHAPE;
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new EnderKeyChestTileEntity();
    }

    @Deprecated
    @Override
    public BlockRenderType getRenderType(BlockState state)
    {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
    {
        return getItem(world, pos);
    }

    private void getDrops(NonNullList<ItemStack> drops, @Nullable TileEntity te)
    {
        drops.add(new ItemStack(Blocks.OBSIDIAN, 8));
        drops.add(KeyUtils.getLock(KeyUtils.getKey(te), isPrivate()));
    }

    // Copy of super except it calls a custom getDrops with the TE param, and it supports not breaking itself based on config
    /*@Override
    public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack)
    {
        player.addStat(Stats.BLOCK_MINED.get(this));
        player.addExhaustion(0.005F);
        if (!Enderthing.breakChestOnHarvest
                || this.canSilkHarvest(state, worldIn, pos, player) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0)
        {
            NonNullList<ItemStack> items = NonNullList.create();
            ItemStack itemstack = this.getSilkTouchDrop(state);
            if (!itemstack.isEmpty()) items.add(itemstack);
            net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, worldIn, pos, state, 0, 1.0f, true, player);
            items.forEach(e -> spawnAsEntity(worldIn, pos, e));
        }
        else
        {
            harvesters.set(player);
            int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
            //state.dropBlockAsItem(worldIn, pos, fortune);
            if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots)
            { // do not drop items while restoring blockstates, prevents item dupe
                NonNullList<ItemStack> drops = NonNullList.create();
                getDrops(drops, te);
                float chancePerItem = ForgeEventFactory.fireBlockHarvesting(drops, worldIn, pos, state, fortune, 1.0f, false, harvesters.get());
                for (ItemStack _stack : drops)
                {
                    if (worldIn.rand.nextFloat() <= chancePerItem)
                        spawnAsEntity(worldIn, pos, _stack);
                }
            }

            harvesters.set(null);
        }
    }*/

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        TileEntity te = worldIn.getTileEntity(pos);

        if (!(te instanceof EnderKeyChestTileEntity))
            return true;

        if (worldIn.getBlockState(pos.up()).isNormalCube(worldIn, pos))
            return true;

        if (worldIn.isRemote)
            return true;

        EnderKeyChestTileEntity chest = (EnderKeyChestTileEntity) te;

        if (player instanceof ServerPlayerEntity)
            Containers.openBlockGui((ServerPlayerEntity) player, chest);

        return true;
    }

    @Deprecated
    //@Override
    public boolean canSilkHarvest()
    {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return this.getDefaultState().with(FACING, context.getPlayer().getHorizontalFacing().getOpposite());
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        worldIn.setBlockState(pos, state.with(FACING, placer.getHorizontalFacing().getOpposite()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand)
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

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    private static ItemStack getItem(IBlockReader world, BlockPos pos)
    {
        BlockState state = world.getBlockState(pos);
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof EnderKeyChestTileEntity)
        {
            long id = ((EnderKeyChestTileEntity) te).getKey();

            return KeyUtils.getItem(
                    (state.getBlock() instanceof EnderKeyChestBlock) && ((EnderKeyChestBlock) state.getBlock()).isPrivate()
                            ? Enderthing.enderLockPrivate
                            : Enderthing.enderLock, id);
        }

        return new ItemStack(Enderthing.enderKeyChest);
    }

    public static class AsItem extends BlockItem
    {
        private final boolean isPrivate;

        public AsItem(Block block, boolean isPrivate, Item.Properties properties)
        {
            super(block, properties.setTEISR(() -> ClientEvents::getKeyChestRenderer));
            this.isPrivate = isPrivate;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
        {
            tooltip.add(new TranslationTextComponent("tooltip.enderthing.ender_key_chest.right_click").applyTextStyle(TextFormatting.ITALIC));

            Enderthing.Client.addStandardInformation(stack, tooltip);
        }

        private void openPasscodeScreen(PlayerEntity playerIn, ItemStack stack)
        {
            Containers.openPasscodeScreen((ServerPlayerEntity) playerIn, new ILongAccessor()
            {
                @Override
                public long get()
                {
                    return KeyUtils.getKey(stack);
                }

                @Override
                public void set(long value)
                {
                    KeyUtils.setKey(stack, value);
                }
            }, stack.copy());
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand)
        {
            ItemStack stack = playerIn.getHeldItem(hand);

            long oldId = KeyUtils.getBlockKey(stack);

            if (oldId < 0)
            {
                if (!worldIn.isRemote)
                    openPasscodeScreen(playerIn, stack);
                return ActionResult.newResult(ActionResultType.SUCCESS, stack);
            }

            if (playerIn.isSneaking())
            {
                ItemStack oldStack = KeyUtils.getLock(oldId, isPrivate());

                if (!playerIn.inventory.addItemStackToInventory(oldStack))
                {
                    playerIn.dropItem(oldStack, false);
                }

                if (stack.getCount() > 1)
                {
                    ItemStack newStack = new ItemStack(Blocks.ENDER_CHEST);
                    if (!playerIn.inventory.addItemStackToInventory(newStack))
                    {
                        playerIn.dropItem(newStack, false);
                    }

                    stack.grow(-1);
                    return ActionResult.newResult(ActionResultType.SUCCESS, stack);
                }

                return ActionResult.newResult(ActionResultType.SUCCESS, new ItemStack(Blocks.ENDER_CHEST));
            }

            return super.onItemRightClick(worldIn, playerIn, hand);
        }

        public boolean isPrivate()
        {
            return isPrivate;
        }

        public boolean isBound()
        {
            return isPrivate;
        }
    }
}
