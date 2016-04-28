package gigaherz.enderthing.blocks;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.gui.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
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
    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool PRIVATE = PropertyBool.create("private");
    public static final PropertyBool BOUND = PropertyBool.create("bound");
    protected static final AxisAlignedBB ENDER_CHEST_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.875D, 0.9375D);

    public static final String INVENTORY_ID_KEY = "InventoryId";

    public BlockEnderKeyChest(String name)
    {
        super(name, Material.rock);
        setDefaultState(this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(PRIVATE, false)
                .withProperty(BOUND, false));
        setCreativeTab(Enderthing.tabEnderthing);
        setHardness(22.5F);
        setResistance(1000.0F);
        setStepSound(soundTypeStone);
        setLightLevel(0.5F);
        setBlockBounds( 1 / 16F,  0 / 16F,  1 / 16F,
                       15 / 16F, 14 / 16F, 15 / 16F);
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean isFullCube()
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
    public int getRenderType()
    {
        return 2;
    }

    @Override
    public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        //If it will harvest, delay deletion of the block until after getDrops
        return willHarvest || super.removedByPlayer(world, pos, player, false);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player)
    {
        return getAsItem(world, pos);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        ArrayList<ItemStack> ret = Lists.newArrayList();

        ret.add(new ItemStack(Blocks.obsidian, 8));
        ret.add(getLock(getId(world, pos), state.getValue(PRIVATE)));

        return ret;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te)
    {
        player.triggerAchievement(StatList.mineBlockStatArray[getIdFromBlock(this)]);
        player.addExhaustion(0.025F);

        if (this.canSilkHarvest(worldIn, pos, state, player) && EnchantmentHelper.getSilkTouchModifier(player))
        {
            List<ItemStack> items = Lists.newArrayList();
            ItemStack itemstack = this.getAsItem(worldIn, pos);

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
            int i = EnchantmentHelper.getFortuneModifier(player);
            this.dropBlockAsItem(worldIn, pos, state, i);
            harvesters.set(null);
        }

        worldIn.setBlockToAir(pos);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity te = worldIn.getTileEntity(pos);

        if (te == null || !(te instanceof TileEnderKeyChest))
            return true;

        if (worldIn.getBlockState(pos.up()).getBlock().isNormalCube(worldIn, pos.up()))
            return true;

        if (worldIn.isRemote)
            return true;

        TileEnderKeyChest chest = (TileEnderKeyChest) te;

        ItemStack heldItem = playerIn.getHeldItem();

        if (side == EnumFacing.UP && heldItem != null && heldItem.getItem() == Items.dye)
        {
            int meta = EnumDyeColor.byDyeDamage(heldItem.getMetadata()).getMetadata();

            //  5, 8, 11; +-1.5
            // 3.5..6.5, 6.5..9.5,9.5..12.5

            float z = hitZ;
            float x = hitX;

            switch (state.getValue(FACING))
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
                if(!playerIn.capabilities.isCreativeMode)
                    heldItem.stackSize--;
                chest.setInventoryId(id);
            }

            if (hitSuccess)
            {
                return true;
            }
        }


        //noinspection PointlessBitwiseExpression
        int id = chest.getInventoryId() << 4 | (state.getValue(PRIVATE) ? GuiHandler.GUI_KEY_PRIVATE : GuiHandler.GUI_KEY);

        playerIn.openGui(Enderthing.instance, id, worldIn, pos.getX(), pos.getY(), pos.getZ());

        return true;
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
    public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
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
            worldIn.spawnParticle(EnumParticleTypes.PORTAL, xPos, yPos, zPos, xSpeed, ySpeed, zSpeed);
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3))
                .withProperty(PRIVATE, (meta & 8) != 0)
                .withProperty(BOUND, (meta & 12) == 12);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(FACING).getHorizontalIndex()
                | (state.getValue(PRIVATE) ? 8 : 0)
                | (state.getValue(BOUND) ? 4:0);
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, FACING, PRIVATE);
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

    private ItemStack getAsItem(IBlockAccess world, BlockPos pos)
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
        public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn)
        {
            if (playerIn.isSneaking())
            {
                int oldId = BlockEnderKeyChest.getId(itemStackIn);

                ItemStack oldStack = getLock(oldId, itemStackIn.getMetadata() != 0);

                if (!playerIn.inventory.addItemStackToInventory(oldStack))
                {
                    playerIn.dropItem(oldStack, true, false);
                }

                if (itemStackIn.stackSize > 1)
                {
                    ItemStack stack = new ItemStack(Blocks.ender_chest);
                    if (!playerIn.inventory.addItemStackToInventory(stack))
                    {
                        playerIn.dropItem(stack, true, false);
                    }

                    itemStackIn.stackSize--;
                    return itemStackIn;
                }

                return new ItemStack(Blocks.ender_chest);
            }

            return super.onItemRightClick(itemStackIn, worldIn, playerIn);
        }

        @Override
        public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced)
        {
            tooltip.add(ChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + Enderthing.MODID + ".enderKeyChest.rightClick"));

            if ((stack.getMetadata() & 8) != 0)
            {
                tooltip.add(ChatFormatting.BOLD + StatCollector.translateToLocal("tooltip." + Enderthing.MODID + ".private"));
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
                tooltip.add(ChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + Enderthing.MODID + ".colorMissing"));
                return;
            }

            int color1 = id & 15;
            int color2 = (id >> 4) & 15;
            int color3 = (id >> 8) & 15;

            EnumDyeColor c1 = EnumDyeColor.byMetadata(color1);
            EnumDyeColor c2 = EnumDyeColor.byMetadata(color2);
            EnumDyeColor c3 = EnumDyeColor.byMetadata(color3);

            tooltip.add(StatCollector.translateToLocalFormatted("tooltip." + Enderthing.MODID + ".colors", c1.getName(), c2.getName(), c3.getName()));
        }

        @Override
        public int getColorFromItemStack(ItemStack stack, int tintIndex)
        {
            int color1 = 0;
            int color2 = 0;
            int color3 = 0;

            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null)
            {
                NBTTagCompound etag = tag.getCompoundTag("BlockEntityTag");
                if (etag != null)
                {
                    int id = etag.getInteger("InventoryId");

                    color1 = id & 15;
                    color2 = (id >> 4) & 15;
                    color3 = (id >> 8) & 15;
                }
            }

            EnumDyeColor c1 = EnumDyeColor.byMetadata(color1);
            EnumDyeColor c2 = EnumDyeColor.byMetadata(color2);
            EnumDyeColor c3 = EnumDyeColor.byMetadata(color3);

            switch (tintIndex)
            {
                case 1:
                    return c1.getMapColor().colorValue;
                case 2:
                    return c2.getMapColor().colorValue;
                case 3:
                    return c3.getMapColor().colorValue;
            }

            return 0xFFFFFFFF;
        }

    }
}
