package gigaherz.enderthing.blocks;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.gui.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Particles;
import net.minecraft.item.*;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockEnderKeyChest extends Block
{
    public static final DirectionProperty FACING = BlockEnderChest.FACING;
    protected static final VoxelShape SHAPE = Block.makeCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public boolean isPrivate()
    {
        return false;
    }

    public static class Private extends BlockEnderKeyChest
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
                    .with(FACING, EnumFacing.NORTH));
        }

        @Override
        public ItemStack getPickBlock(IBlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, EntityPlayer player)
        {
            return new ItemStack(Enderthing.enderKeyChestPrivate);
        }

        @Override
        public IItemProvider getItemDropped(IBlockState state, World worldIn, BlockPos pos, int fortune)
        {
            return Enderthing.enderKeyChestPrivate;
        }

        @Override
        protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder)
        {
            builder.add(FACING, BOUND);
        }

        @Nullable
        @Override
        public TileEntity createTileEntity(IBlockState state, IBlockReader world)
        {
            return new TileEnderKeyChest.Private();
        }
    }

    public BlockEnderKeyChest(Properties properties)
    {
        super(properties); // Material.ROCK
        setDefaultState(this.getStateContainer().getBaseState()
                .with(FACING, EnumFacing.NORTH));
    }

    @Deprecated
    @Override
    public VoxelShape getShape(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        return SHAPE;
    }

    @Deprecated
    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(IBlockState state, IBlockReader world)
    {
        return new TileEnderKeyChest();
    }

    @Deprecated
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, EntityPlayer player)
    {
        return getItem(world, pos);
    }

    private void getDrops(NonNullList<ItemStack> drops, @Nullable TileEntity te)
    {
        drops.add(new ItemStack(Blocks.OBSIDIAN, 8));
        drops.add(Enderthing.getLock(Enderthing.getIdFromTE(te), isPrivate()));
    }

    // Copy of super except it calls a custom getDrops with the TE param, and it supports not breaking itself based on config
    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        player.addStat(StatList.BLOCK_MINED.get(this));
        player.addExhaustion(0.005F);
        if (!Enderthing.breakChestOnHarvest
                || this.canSilkHarvest(state, worldIn, pos, player) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
            NonNullList<ItemStack> items = NonNullList.create();
            ItemStack itemstack = this.getSilkTouchDrop(state);
            if (!itemstack.isEmpty()) items.add(itemstack);
            net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, worldIn, pos, state, 0, 1.0f, true, player);
            items.forEach(e -> spawnAsEntity(worldIn, pos, e));
        } else {
            harvesters.set(player);
            int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
            //state.dropBlockAsItem(worldIn, pos, fortune);
            if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
                NonNullList<ItemStack> drops = NonNullList.create();
                getDrops(drops, te);
                float chancePerItem = ForgeEventFactory.fireBlockHarvesting(drops, worldIn, pos, state, fortune, 1.0f, false, harvesters.get());
                for (ItemStack _stack : drops) {
                    if (worldIn.rand.nextFloat() <= chancePerItem)
                        spawnAsEntity(worldIn, pos, _stack);
                }
            }

            harvesters.set(null);
        }

    }

    @Override
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity te = worldIn.getTileEntity(pos);

        if (!(te instanceof TileEnderKeyChest))
            return true;

        if (worldIn.getBlockState(pos.up()).isNormalCube())
            return true;

        if (worldIn.isRemote)
            return true;

        TileEnderKeyChest chest = (TileEnderKeyChest) te;

        ItemStack heldItem = player.getHeldItem(hand);
        EnumDyeColor color = EnumDyeColor.getColor(heldItem);
        if (side == EnumFacing.UP && color != null
                && (chest.getPlayerBound() == null || chest.getPlayerBound().equals(player.getUniqueID())))
        {
            int meta = color.getId();

            //  5, 8, 11; +-1.5
            // 3.5..6.5, 6.5..9.5,9.5..12.5

            float z = hitZ;
            float x = hitX;

            switch (state.get(FACING))
            {
                case EAST:
                    x = hitZ;
                    z = 1 - hitX;
                    break;
                case SOUTH:
                    x = 1 - hitX;
                    z = 1 - hitZ;
                    break;
                case WEST:
                    x = 1 - hitZ;
                    z = hitX;
                    break;
            }

            x *= 16;
            z *= 16;

            boolean hitSuccess = false;
            int oldId = chest.getInventoryId();
            int id = oldId;
            if (z >= 1 && z <= 8)
            {
                int color1 = id & 15;
                int color2 = (id >> 4) & 15;
                int color3 = (id >> 8) & 15;

                if (x >= 3.5 && x < 6.5)
                {
                    color3 = meta;
                    hitSuccess = true;
                }
                else if (x >= 6.5 && x < 9.5)
                {
                    color2 = meta;
                    hitSuccess = true;
                }
                else if (x >= 9.5 && x < 12.5)
                {
                    color1 = meta;
                    hitSuccess = true;
                }

                id = (color1) | (color2 << 4) | (color3 << 8);
            }

            if (oldId != id)
            {
                if (!player.isCreative())
                    heldItem.grow(-1);
                chest.setInventoryId(id);
            }

            if (hitSuccess)
            {
                return true;
            }
        }

        if (player instanceof EntityPlayerMP)
            GuiHandler.openKeyGui(pos, (EntityPlayerMP) player, chest.getInventoryId(), isPrivate());

        return true;
    }

    @Deprecated
    @Override
    public boolean canSilkHarvest()
    {
        return true;
    }

    @Nullable
    @Override
    public IBlockState getStateForPlacement(BlockItemUseContext context)
    {
        return this.getDefaultState().with(FACING, context.getPlayer().getHorizontalFacing().getOpposite());
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        worldIn.setBlockState(pos, state.with(FACING, placer.getHorizontalFacing().getOpposite()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
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
            worldIn.spawnParticle(Particles.PORTAL, xPos, yPos, zPos, xSpeed, ySpeed, zSpeed);
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder)
    {
        builder.add(FACING);
    }

    private static ItemStack getItem(IBlockReader world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEnderKeyChest)
        {
            int id = ((TileEnderKeyChest) te).getInventoryId();

            return Enderthing.getItem(id, (state.getBlock() instanceof BlockEnderKeyChest) && ((BlockEnderKeyChest)state.getBlock()).isPrivate());
        }

        return new ItemStack(Enderthing.enderKeyChest);
    }

    public static class AsItem extends ItemBlock
    {
        private final boolean isPrivate;
        private final boolean isBound;

        public AsItem(Block block, boolean isPrivate, boolean isBound, Item.Properties properties)
        {
            super(block, properties);
            this.isPrivate = isPrivate;
            this.isBound = isBound;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
        {
            tooltip.add(new TextComponentTranslation("tooltip." + Enderthing.MODID + ".ender_key_chest.rightClick").applyTextStyle(TextFormatting.ITALIC));

            Enderthing.addStandardInformation(stack, tooltip, flagIn, isPrivate);
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
        {
            ItemStack itemStackIn = playerIn.getHeldItem(hand);
            if (playerIn.isSneaking())
            {
                int oldId = Enderthing.getIdFromBlock(itemStackIn);

                ItemStack oldStack = Enderthing.getLock(oldId, isPrivate());

                if (!playerIn.inventory.addItemStackToInventory(oldStack))
                {
                    playerIn.dropItem(oldStack, false);
                }

                if (itemStackIn.getCount() > 1)
                {
                    ItemStack stack = new ItemStack(Blocks.ENDER_CHEST);
                    if (!playerIn.inventory.addItemStackToInventory(stack))
                    {
                        playerIn.dropItem(stack, false);
                    }

                    itemStackIn.grow(-1);
                    return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
                }

                return ActionResult.newResult(EnumActionResult.SUCCESS, new ItemStack(Blocks.ENDER_CHEST));
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
