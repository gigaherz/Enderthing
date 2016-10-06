package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

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

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced)
    {
        Enderthing.addStandardInformation(stack, player, tooltip, advanced);
    }
}
