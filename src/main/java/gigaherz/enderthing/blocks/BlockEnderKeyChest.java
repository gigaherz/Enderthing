package gigaherz.enderthing.blocks;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.gui.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockEnderKeyChest
        extends BlockRegistered
{
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyBool PRIVATE = PropertyBool.create("private");
    protected static final AxisAlignedBB ENDER_CHEST_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.875D, 0.9375D);

    public static final String INVENTORY_ID_KEY = "InventoryId";

    public BlockEnderKeyChest(String name)
    {
        super(name, Material.ROCK);
        setDefaultState(this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(PRIVATE, false));
        setCreativeTab(Enderthing.tabEnderthing);
        setHardness(22.5F);
        setResistance(1000.0F);
        setSoundType(SoundType.STONE);
        setLightLevel(0.5F);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

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

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        if (state.getValue(PRIVATE))
            return new TileEnderKeyChest.Private();
        return new TileEnderKeyChest();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        //If it will harvest, delay deletion of the block until after getDrops
        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        return getItem(world, pos);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        ArrayList<ItemStack> ret = Lists.newArrayList();

        ret.add(new ItemStack(Blocks.OBSIDIAN, 8));
        ret.add(getLock(getId(world, pos), state.getValue(PRIVATE)));

        return ret;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack)
    {
        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.025F);

        if (this.canSilkHarvest(worldIn, pos, state, player) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0)
        {
            java.util.List<ItemStack> items = new java.util.ArrayList<ItemStack>();
            ItemStack itemstack = this.getItem(worldIn, pos);

            if (itemstack != null)
            {
                items.add(itemstack);
            }

            net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, worldIn, pos, state, 0, 1.0f, true, player);
            for (ItemStack item : items)
            {
                spawnAsEntity(worldIn, pos, item);
            }
        }
        else
        {
            harvesters.set(player);
            int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
            this.dropBlockAsItem(worldIn, pos, state, i);
            harvesters.set(null);
        }

        worldIn.setBlockToAir(pos);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity te = worldIn.getTileEntity(pos);

        if (te == null || !(te instanceof TileEnderKeyChest))
            return true;

        if (worldIn.getBlockState(pos.up()).isNormalCube())
            return true;

        if (worldIn.isRemote)
            return true;

        TileEnderKeyChest chest = (TileEnderKeyChest) te;

        //noinspection PointlessBitwiseExpression
        int id = chest.getInventoryId() << 4 | (state.getValue(PRIVATE) ? GuiHandler.GUI_KEY_PRIVATE : GuiHandler.GUI_KEY);

        playerIn.openGui(Enderthing.instance, id, worldIn, pos.getX(), pos.getY(), pos.getZ());
        playerIn.addStat(StatList.ENDERCHEST_OPENED);

        return true;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return ENDER_CHEST_AABB;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        return true;
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite())
                .withProperty(PRIVATE, (meta & 8) != 0);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite())
                .withProperty(PRIVATE, (stack.getMetadata() & 8) != 0), 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        for (int i = 0; i < 3; ++i)
        {
            int j = rand.nextInt(2) * 2 - 1;
            int k = rand.nextInt(2) * 2 - 1;
            double d0 = (double) pos.getX() + 0.5D + 0.25D * (double) j;
            double d1 = (double) ((float) pos.getY() + rand.nextFloat());
            double d2 = (double) pos.getZ() + 0.5D + 0.25D * (double) k;
            double d3 = (double) (rand.nextFloat() * (float) j);
            double d4 = ((double) rand.nextFloat() - 0.5D) * 0.125D;
            double d5 = (double) (rand.nextFloat() * (float) k);
            worldIn.spawnParticle(EnumParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        EnumFacing enumfacing = EnumFacing.getFront(meta & 7);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing)
                .withProperty(PRIVATE, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(FACING).getIndex() | (state.getValue(PRIVATE) ? 8 : 0);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING, PRIVATE);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced)
    {
        tooltip.add(ChatFormatting.ITALIC + I18n.translateToLocal("tooltip." + Enderthing.MODID + ".enderKeyChest.rightClick"));

        if ((stack.getMetadata() & 8) != 0)
        {
            tooltip.add(ChatFormatting.BOLD + I18n.translateToLocal("tooltip." + Enderthing.MODID + ".private"));
        }

        int id = 0;
        boolean idFound = false;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null)
        {
            NBTTagCompound etag = tag.getCompoundTag("BlockEntityTag");
            if (etag != null)
            {
                idFound = true;
                id = etag.getInteger("InventoryId");
            }
        }

        if (!idFound)
        {
            tooltip.add(ChatFormatting.ITALIC + I18n.translateToLocal("tooltip." + Enderthing.MODID + ".colorMissing"));
            return;
        }

        int color1 = id & 15;
        int color2 = (id >> 4) & 15;
        int color3 = (id >> 8) & 15;

        EnumDyeColor c1 = EnumDyeColor.byMetadata(color1);
        EnumDyeColor c2 = EnumDyeColor.byMetadata(color2);
        EnumDyeColor c3 = EnumDyeColor.byMetadata(color3);

        tooltip.add(I18n.translateToLocalFormatted("tooltip." + Enderthing.MODID + ".colors", c1.getName(), c2.getName(), c3.getName()));
    }

    int getId(IBlockAccess world, BlockPos pos)
    {
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEnderKeyChest)
        {
            return ((TileEnderKeyChest) te).getInventoryId();
        }

        return 0;
    }

    private ItemStack getItem(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEnderKeyChest)
        {
            int id = ((TileEnderKeyChest) te).getInventoryId();

            return getItem(id, state.getValue(PRIVATE));
        }

        return new ItemStack(Enderthing.blockEnderKeyChest);
    }

    public static ItemStack getItem(int id, boolean priv)
    {
        ItemStack stack = new ItemStack(Enderthing.blockEnderKeyChest, 1, priv ? 8 : 0);

        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound etag = new NBTTagCompound();
        etag.setInteger(INVENTORY_ID_KEY, id);
        tag.setTag("BlockEntityTag", etag);

        stack.setTagCompound(tag);

        return stack;
    }

    public static int getId(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null)
        {
            NBTTagCompound etag = tag.getCompoundTag("BlockEntityTag");
            if (etag != null)
            {
                return etag.getInteger("InventoryId");
            }
        }

        return 0;
    }

    private static ItemStack getLock(int oldId, boolean value)
    {
        int oldColor1 = oldId & 15;
        int oldColor2 = (oldId >> 4) & 15;
        int oldColor3 = (oldId >> 8) & 15;

        ItemStack oldStack = new ItemStack(Enderthing.enderLock, 1, value ? 1 : 0);

        NBTTagCompound oldTag = new NBTTagCompound();
        oldTag.setByte("Color1", (byte) oldColor1);
        oldTag.setByte("Color2", (byte) oldColor2);
        oldTag.setByte("Color3", (byte) oldColor3);

        oldStack.setTagCompound(oldTag);
        return oldStack;
    }

    @Override
    public ItemBlock createItemBlock()
    {
        return new AsItem(this);
    }

    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        for (int i = 0; i < 16; i++)
        {
            int id = i | (i << 4) | (i << 8);

            list.add(getItem(id, false));
            list.add(getItem(id, true));
        }
    }

    public static class AsItem extends ItemBlock
    {
        public AsItem(Block block)
        {
            super(block);
            setHasSubtypes(true);
            setRegistryName(block.getRegistryName());
        }

        @Override
        public int getMetadata(int damage)
        {
            return damage;
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
        {
            if (playerIn.isSneaking())
            {
                int oldId = BlockEnderKeyChest.getId(itemStackIn);

                ItemStack oldStack = getLock(oldId, itemStackIn.getMetadata() != 0);

                if (!playerIn.inventory.addItemStackToInventory(oldStack))
                {
                    playerIn.dropItem(oldStack, false);
                }

                if (itemStackIn.stackSize > 1)
                {
                    ItemStack stack = new ItemStack(Blocks.ENDER_CHEST);
                    if (!playerIn.inventory.addItemStackToInventory(stack))
                    {
                        playerIn.dropItem(stack, false);
                    }

                    itemStackIn.stackSize--;
                    return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
                }

                return ActionResult.newResult(EnumActionResult.SUCCESS, new ItemStack(Blocks.ENDER_CHEST));
            }

            return super.onItemRightClick(itemStackIn, worldIn, playerIn, hand);
        }
    }
}
