package gigaherz.enderthing.recipes;

import gigaherz.enderthing.Enderthing;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class LockRecipe extends ShapedOreRecipe
{
    public LockRecipe()
    {
        super(new ItemStack(Enderthing.enderLock),
                " g ",
                "geg",
                "www",
                'w', new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE),
                'g', Items.GOLD_INGOT,
                'e', Items.ENDER_EYE);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        ItemStack wool1 = inv.getStackInSlot(6);
        ItemStack wool2 = inv.getStackInSlot(7);
        ItemStack wool3 = inv.getStackInSlot(8);

        int c1 = wool1.getMetadata();
        int c2 = wool2.getMetadata();
        int c3 = wool3.getMetadata();

        return Enderthing.getItem(Enderthing.enderLock, c1, c2, c3, false);
    }
}
