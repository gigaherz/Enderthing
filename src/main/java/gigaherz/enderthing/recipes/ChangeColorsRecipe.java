package gigaherz.enderthing.recipes;

import gigaherz.enderthing.items.ItemEnderthing;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ChangeColorsRecipe extends ShapedOreRecipe
{
    private ChangeColorsRecipe(ItemStack out, ItemStack in)
    {
        super(out,
            "ddd",
            " k ",
            'd', "dye",
            'k', in);
    }

    public ChangeColorsRecipe(Item which, boolean priv)
    {
        this(new ItemStack(which, 1, priv ? 1 : 0), new ItemStack(which, 1, priv ? 1 : 0));
    }

    public ChangeColorsRecipe(Block which, boolean priv)
    {
        this(new ItemStack(which, 1, priv ? 8 : 0), new ItemStack(which, 1, priv ? 8 : 0));
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        ItemStack dye1 = inv.getStackInSlot(0);
        ItemStack dye2, dye3, input;

        if (dye1 == null)
        {
            dye1 = inv.getStackInSlot(3);
            dye2 = inv.getStackInSlot(4);
            dye3 = inv.getStackInSlot(5);
            input = inv.getStackInSlot(7);
            assert dye1 != null;
        }
        else
        {
            dye2 = inv.getStackInSlot(1);
            dye3 = inv.getStackInSlot(2);
            input = inv.getStackInSlot(4);
        }
        assert dye2 != null;
        assert dye3 != null;
        assert input != null;

        int c1 = EnumDyeColor.byDyeDamage(dye1.getMetadata()).getMetadata();
        int c2 = EnumDyeColor.byDyeDamage(dye2.getMetadata()).getMetadata();
        int c3 = EnumDyeColor.byDyeDamage(dye3.getMetadata()).getMetadata();

        return ItemEnderthing.getItem(input.getItem(), c1, c2, c3, ItemEnderthing.isPrivate(input));
    }
}
