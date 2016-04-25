package gigaherz.enderthing.blocks;

import com.google.common.collect.Lists;
import gigaherz.enderthing.Enderthing;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class BlockSharedChest
        extends BlockRegistered
{
    public static final String INVENTORY_ID_KEY = "InventoryId";

    public BlockSharedChest(String name)
    {
        super(name, Material.ROCK);
        setCreativeTab(CreativeTabs.DECORATIONS);
        setHardness(3.0F);
        setSoundType(SoundType.METAL);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
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
        return new TileSharedChest();
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

        ItemStack stack = getItem(world, pos);

        ret.add(stack);

        return ret;
    }

    private ItemStack getItem(IBlockAccess world, BlockPos pos)
    {
        TileEntity te = world.getTileEntity(pos);

        ItemStack stack = new ItemStack(this);

        if (te instanceof TileSharedChest)
        {
            NBTTagCompound tag = new NBTTagCompound();

            NBTTagCompound etag = new NBTTagCompound();

            etag.setInteger(INVENTORY_ID_KEY, ((TileSharedChest) te).getInventoryId());

            tag.setTag("BlockEntityTag", etag);

            stack.setTagCompound(tag);
        }
        return stack;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack)
    {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.setBlockToAir(pos);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced)
    {
        super.addInformation(stack, player, tooltip, advanced);

        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null)
        {
            NBTTagCompound etag = tag.getCompoundTag("BlockEntityTag");
            if (etag != null)
            {
                tooltip.add("Inventory Id: " + etag.getInteger("InventoryId"));
                return;
            }
        }

        tooltip.add("Inventory Unbound, place to generate new inventory");
    }
}
