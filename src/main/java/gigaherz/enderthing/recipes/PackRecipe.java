package gigaherz.enderthing.recipes;

import gigaherz.enderthing.Enderthing;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class PackRecipe extends ShapedOreRecipe
{
    public PackRecipe()
    {
        super(new ItemStack(Enderthing.enderPack),
                "lel",
                "www",
                "lcl",
                'w', new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE),
                'l', Items.LEATHER,
                'c', Blocks.ENDER_CHEST,
                'e', Items.ENDER_EYE);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        ItemStack wool1 = inv.getStackInSlot(3);
        ItemStack wool2 = inv.getStackInSlot(4);
        ItemStack wool3 = inv.getStackInSlot(5);

        assert wool1 != null;
        assert wool2 != null;
        assert wool3 != null;

        int c1 = wool1.getMetadata();
        int c2 = wool2.getMetadata();
        int c3 = wool3.getMetadata();

        return Enderthing.getItem(Enderthing.enderPack, c1, c2, c3, false);
    }
}
