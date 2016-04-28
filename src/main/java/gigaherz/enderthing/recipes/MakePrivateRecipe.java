package gigaherz.enderthing.recipes;

import gigaherz.enderthing.Enderthing;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class MakePrivateRecipe implements IRecipe
{
    public static final ItemStack[] PATTERN = {
            null, new ItemStack(Items.GOLD_NUGGET), null,
            new ItemStack(Items.GOLD_NUGGET),
            new ItemStack(Enderthing.enderKey),
            new ItemStack(Items.GOLD_NUGGET),
            null, new ItemStack(Items.GOLD_NUGGET), null
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
            else if (pat.getItem() == Enderthing.enderKey)
            {
                if ((stack.getItem() != Enderthing.enderKey
                        && stack.getItem() != Enderthing.enderLock
                        && stack.getItem() != Enderthing.enderPack) || (stack.getMetadata() & 1) != 0)
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
        ItemStack itemStack = inv.getStackInSlot(4);

        NBTTagCompound tag = itemStack.getTagCompound();
        if (tag != null)
            tag = (NBTTagCompound) tag.copy();

        ItemStack out = new ItemStack(itemStack.getItem(), 1, 1);

        out.setTagCompound(tag);

        return out;
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
