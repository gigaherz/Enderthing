package gigaherz.enderthing.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import gigaherz.enderthing.Enderthing;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

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
        for (int i = 0; i < 16; i++)
        {
            subItems.add(getItem(this, i, i, i, false));
            subItems.add(getItem(this, i, i, i, true));
        }
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

        return (color1 << 4) | (color2 << 8) | (color3 << 12);
    }

    public static ItemStack getItem(Item item, int c1, int c2, int c3, boolean priv)
    {
        ItemStack key = new ItemStack(item, 1, priv ? 1 : 0);

        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Color1", (byte) c1);
        tag.setByte("Color2", (byte) c2);
        tag.setByte("Color3", (byte) c3);

        key.setTagCompound(tag);

        return key;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> information, boolean advanced)
    {
        if ((stack.getMetadata() & 1) != 0)
        {
            information.add(ChatFormatting.BOLD + StatCollector.translateToLocal("tooltip." + Enderthing.MODID + ".private"));
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
        {
            information.add(ChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + Enderthing.MODID + ".colorMissing"));
            return;
        }

        int color1 = tag.getByte("Color1");
        int color2 = tag.getByte("Color2");
        int color3 = tag.getByte("Color3");

        EnumDyeColor c1 = EnumDyeColor.byMetadata(color1);
        EnumDyeColor c2 = EnumDyeColor.byMetadata(color2);
        EnumDyeColor c3 = EnumDyeColor.byMetadata(color3);

        information.add(StatCollector.translateToLocalFormatted("tooltip." + Enderthing.MODID + ".colors", c1.getName(), c2.getName(), c3.getName()));
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
            color1 = tag.getByte("Color1");
            color2 = tag.getByte("Color2");
            color3 = tag.getByte("Color3");
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
