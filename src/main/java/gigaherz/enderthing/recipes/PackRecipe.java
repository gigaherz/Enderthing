package gigaherz.enderthing.recipes;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.items.ItemEnderthing;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class PackRecipe implements IRecipe
{
    public static final ItemStack[] PATTERN = {
            new ItemStack(Items.LEATHER), new ItemStack(Items.ENDER_EYE), new ItemStack(Items.LEATHER),
            new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE),
            new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE),
            new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE),
            new ItemStack(Items.LEATHER), new ItemStack(Blocks.ENDER_CHEST), new ItemStack(Items.LEATHER)
    };

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        if (inv.getSizeInventory() < 9)
            return false;

        for (int i = 0; i < 9; i++)
        {
            ItemStack pat = PATTERN[i];
            ItemStack stack = inv.getStackInSlot(i);

            if (pat == null)
            {
                if (stack != null)
                    return false;
            }
            else
            {
                if (stack == null ||
                        !(pat.getMetadata() == OreDictionary.WILDCARD_VALUE ?
                                pat.getItem() == stack.getItem() :
                                pat.isItemEqual(stack)) ||
                        !ItemStack.areItemStackTagsEqual(pat, stack))
                    return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        ItemStack wool1 = inv.getStackInSlot(3);
        ItemStack wool2 = inv.getStackInSlot(4);
        ItemStack wool3 = inv.getStackInSlot(5);

        int c1 = wool1.getMetadata();
        int c2 = wool2.getMetadata();
        int c3 = wool3.getMetadata();

        return ItemEnderthing.getItem(Enderthing.enderPack, c1, c2, c3, false);
    }

    @Override
    public int getRecipeSize()
    {
        return 9;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return new ItemStack(Enderthing.enderLock);
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv)
    {
        return new ItemStack[inv.getSizeInventory()];
    }
}
