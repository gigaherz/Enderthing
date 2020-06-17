package gigaherz.enderthing.recipes;

/*
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

        if (dye1.getCount() == 0)
        {
            dye1 = inv.getStackInSlot(3);
            dye2 = inv.getStackInSlot(4);
            dye3 = inv.getStackInSlot(5);
            input = inv.getStackInSlot(7);
        }
        else
        {
            dye2 = inv.getStackInSlot(1);
            dye3 = inv.getStackInSlot(2);
            input = inv.getStackInSlot(4);
        }

        int c1 = EnumDyeColor.getColor(dye1).getId();
        int c2 = EnumDyeColor.getColor(dye2).getId();
        int c3 = EnumDyeColor.getColor(dye3).getId();

        return Enderthing.getItem(input.getItem(), c1, c2, c3, Enderthing.isPrivate(input));
    }
}
*/