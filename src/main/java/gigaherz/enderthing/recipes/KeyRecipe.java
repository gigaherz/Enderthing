package gigaherz.enderthing.recipes;

import gigaherz.enderthing.Enderthing;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class KeyRecipe implements IRecipe
{
    ItemStack[] pattern = {
            new ItemStack(Blocks.OBSIDIAN), null, null,
            new ItemStack(Items.ENDER_EYE), new ItemStack(Blocks.OBSIDIAN), new ItemStack(Blocks.OBSIDIAN),
            new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE),
            new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE),
            new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE)
    };

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        if(inv.getSizeInventory() < 9)
            return false;

        for(int i=0;i<9;i++)
        {
            ItemStack pat = pattern[i];
            ItemStack stack = inv.getStackInSlot(i);

            if(pat == null)
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
        ItemStack key = new ItemStack(Enderthing.enderKey);

        ItemStack wool1 = inv.getStackInSlot(6);
        ItemStack wool2 = inv.getStackInSlot(7);
        ItemStack wool3 = inv.getStackInSlot(8);

        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Color1", (byte)wool1.getMetadata());
        tag.setByte("Color2", (byte)wool2.getMetadata());
        tag.setByte("Color3", (byte)wool3.getMetadata());

        key.setTagCompound(tag);

        return key;
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
