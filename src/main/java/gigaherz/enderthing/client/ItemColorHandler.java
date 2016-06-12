package gigaherz.enderthing.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Random;

public abstract class ItemColorHandler implements IItemColor
{
    static Random rand = new Random();
    static long lastT = 0;
    static byte last1 = 0;
    static byte last2 = 0;
    static byte last3 = 0;

    protected abstract int getColor1(ItemStack stack);

    protected abstract int getColor2(ItemStack stack);

    protected abstract int getColor3(ItemStack stack);

    @Override
    public int getColorFromItemstack(ItemStack stack, int tintIndex)
    {
        long t = Minecraft.getMinecraft().theWorld.getTotalWorldTime();
        if ((t - lastT) > 20)
        {
            lastT = t;
            last1 = (byte) rand.nextInt(16);
            last2 = (byte) rand.nextInt(16);
            last3 = (byte) rand.nextInt(16);
        }

        switch (tintIndex)
        {
            case 1:
                EnumDyeColor c1 = EnumDyeColor.byMetadata(getColor1(stack));
                return c1.getMapColor().colorValue;
            case 2:
                EnumDyeColor c2 = EnumDyeColor.byMetadata(getColor2(stack));
                return c2.getMapColor().colorValue;
            case 3:
                EnumDyeColor c3 = EnumDyeColor.byMetadata(getColor3(stack));
                return c3.getMapColor().colorValue;
        }

        return 0xFFFFFFFF;
    }

    public static class ItemTag extends ItemColorHandler
    {

        @Override
        protected int getColor1(ItemStack stack)
        {
            NBTTagCompound tag = stack.getTagCompound();
            return tag != null ? tag.getByte("Color1") : last1;
        }

        @Override
        protected int getColor2(ItemStack stack)
        {
            NBTTagCompound tag = stack.getTagCompound();
            return tag != null ? tag.getByte("Color2") : last2;
        }

        @Override
        protected int getColor3(ItemStack stack)
        {
            NBTTagCompound tag = stack.getTagCompound();
            return tag != null ? tag.getByte("Color3") : last3;
        }
    }

    public static class BlockTag extends ItemColorHandler
    {
        @Override
        protected int getColor1(ItemStack stack)
        {
            NBTTagCompound tag = stack.getTagCompound();
            NBTTagCompound etag = null;
            if (tag != null)
                etag = tag.getCompoundTag("BlockEntityTag");

            return etag != null ? etag.getInteger("InventoryId") & 15 : last1;
        }

        @Override
        protected int getColor2(ItemStack stack)
        {
            NBTTagCompound tag = stack.getTagCompound();
            NBTTagCompound etag = null;
            if (tag != null)
                etag = tag.getCompoundTag("BlockEntityTag");

            return etag != null ? (etag.getInteger("InventoryId") >> 4) & 15 : last2;
        }

        @Override
        protected int getColor3(ItemStack stack)
        {
            NBTTagCompound tag = stack.getTagCompound();
            NBTTagCompound etag = null;
            if (tag != null)
                etag = tag.getCompoundTag("BlockEntityTag");

            return etag != null ? (etag.getInteger("InventoryId") >> 8) & 15 : last3;
        }
    }
}
