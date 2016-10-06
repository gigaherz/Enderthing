package gigaherz.enderthing.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.blocks.BlockEnderKeyChest;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class ItemEnderthing extends ItemRegistered
{
    public ItemEnderthing(String name)
    {
        super(name);
        setMaxStackSize(16);
        setHasSubtypes(true);
        setCreativeTab(Enderthing.tabEnderthing);
    }

    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        subItems.add(new ItemStack(this, 1, 0));
        subItems.add(new ItemStack(this, 1, 1));
    }

    public static int getId(ItemStack stack)
    {
        int color1 = 0;
        int color2 = 0;
        int color3 = 0;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null)
        {
            color1 = tag.getByte("Color1");
            color2 = tag.getByte("Color2");
            color3 = tag.getByte("Color3");
        }

        return getId(color1, color2, color3)<<4;
    }

    private static int getId(int c1, int c2, int c3)
    {
        return c1 | (c2 << 4) | (c3 << 8);
    }

    public static int getBlockPrivateBit(boolean priv)
    {
        return priv ? 8 : 0;
    }

    public static int getItemPrivateBit(Item item, boolean priv)
    {
        if (item instanceof ItemBlock)
            return getBlockPrivateBit(priv);
        return priv ? 1 : 0;
    }

    public static ItemStack getItem(Item item, int c1, int c2, int c3, boolean priv)
    {
        if (item instanceof ItemBlock)
        {
            return BlockEnderKeyChest.getItem(getId(c1,c2,c3) >> 4, priv);
        }

        ItemStack key = new ItemStack(item, 1, getItemPrivateBit(item, priv));

        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Color1", (byte) c1);
        tag.setByte("Color2", (byte) c2);
        tag.setByte("Color3", (byte) c3);

        key.setTagCompound(tag);

        return key;
    }

    public static ItemStack getLock(int id, boolean priv)
    {
        int c1 = id & 15;
        int c2 = (id >> 4) & 15;
        int c3 = (id >> 8) & 15;

        return ItemEnderthing.getItem(Enderthing.enderLock, c1, c2, c3, priv);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> information, boolean advanced)
    {
        if ((stack.getMetadata() & 1) != 0)
        {
            information.add(ChatFormatting.BOLD + I18n.format("tooltip." + Enderthing.MODID + ".private"));
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
        {
            information.add(ChatFormatting.ITALIC + I18n.format("tooltip." + Enderthing.MODID + ".colorMissing"));
            return;
        }

        int color1 = tag.getByte("Color1");
        int color2 = tag.getByte("Color2");
        int color3 = tag.getByte("Color3");

        EnumDyeColor c1 = EnumDyeColor.byMetadata(color1);
        EnumDyeColor c2 = EnumDyeColor.byMetadata(color2);
        EnumDyeColor c3 = EnumDyeColor.byMetadata(color3);

        information.add(I18n.format("tooltip." + Enderthing.MODID + ".colors", c1.getName(), c2.getName(), c3.getName()));
    }

    public static boolean isPrivate(ItemStack input)
    {
        return input.getMetadata() != 0;
    }
}
