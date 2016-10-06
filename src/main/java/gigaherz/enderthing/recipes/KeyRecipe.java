package gigaherz.enderthing.recipes;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.items.ItemEnderthing;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class KeyRecipe extends ShapedOreRecipe
{
    public KeyRecipe()
    {
        super(new ItemStack(Enderthing.enderKey),
                "o  ",
                "eoe",
                "www",
                'w', "wool",
                'o', Blocks.OBSIDIAN,
                'e', Items.ENDER_EYE);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        ItemStack wool1 = inv.getStackInSlot(6);
        ItemStack wool2 = inv.getStackInSlot(7);
        ItemStack wool3 = inv.getStackInSlot(8);

        assert wool1 != null;
        assert wool2 != null;
        assert wool3 != null;

        int c1 = wool1.getMetadata();
        int c2 = wool2.getMetadata();
        int c3 = wool3.getMetadata();

        return ItemEnderthing.getItem(Enderthing.enderKey, c1, c2, c3, false);
    }

    @Override
    public int getRecipeSize()
    {
        return 9;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return new ItemStack(Enderthing.enderKey);
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv)
    {
        return new ItemStack[inv.getSizeInventory()];
    }
}
