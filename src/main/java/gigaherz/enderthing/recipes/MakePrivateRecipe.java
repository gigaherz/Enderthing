package gigaherz.enderthing.recipes;

import gigaherz.enderthing.Enderthing;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class MakePrivateRecipe extends ShapedOreRecipe
{
    private MakePrivateRecipe(ItemStack out, ItemStack in)
    {
        super(out,
                " n ",
                "nkn",
                " n ",
                'n', Items.GOLD_NUGGET,
                'k', in);
    }

    public MakePrivateRecipe(Item which)
    {
        this(new ItemStack(which, 1, Enderthing.ITEM_PRIVATE_BIT), new ItemStack(which));
    }

    public MakePrivateRecipe(Block which)
    {
        this(new ItemStack(which, 1, Enderthing.BLOCK_PRIVATE_BIT), new ItemStack(which));
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        ItemStack out = super.getCraftingResult(inv);

        if (out == null || out.stackSize <= 0)
            return null;

        ItemStack itemStack = inv.getStackInSlot(4);

        assert itemStack != null;

        NBTTagCompound tag = itemStack.getTagCompound();
        if (tag != null)
            tag = tag.copy();

        out.setTagCompound(tag);

        return out;
    }
}
