package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import net.minecraft.item.Item;

public class ItemRegistered extends Item
{
    public ItemRegistered(String name)
    {
        setRegistryName(name);
        setUnlocalizedName(Enderthing.MODID + "." + name);
    }
}
