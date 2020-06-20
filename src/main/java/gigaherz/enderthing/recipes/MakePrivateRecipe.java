package gigaherz.enderthing.recipes;

import gigaherz.enderthing.KeyUtils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ObjectHolder;

public class MakePrivateRecipe extends SpecialRecipe implements IShapedRecipe<CraftingInventory>
{
    @ObjectHolder("enderthing:make_private")
    public static SpecialRecipeSerializer<MakePrivateRecipe> SERIALIZER = null;

    public MakePrivateRecipe(ResourceLocation id)
    {
        super(id);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn)
    {
        ItemStack centerSlot = inv.getStackInSlot(4);
        return inv.getStackInSlot(0).getCount() == 0
                && inv.getStackInSlot(1).getItem() == Items.GOLD_NUGGET
                && inv.getStackInSlot(2).getCount() == 0
                && inv.getStackInSlot(3).getItem() == Items.GOLD_NUGGET
                && inv.getStackInSlot(5).getItem() == Items.GOLD_NUGGET
                && inv.getStackInSlot(6).getCount() == 0
                && inv.getStackInSlot(7).getItem() == Items.GOLD_NUGGET
                && inv.getStackInSlot(8).getCount() == 0
                && centerSlot.getItem() instanceof KeyUtils.IKeyHolder
                && !KeyUtils.isPrivate(centerSlot);
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv)
    {
        ItemStack output = inv.getStackInSlot(4).copy();

        KeyUtils.setPrivate(output, true);

        return output;
    }

    @Override
    public boolean canFit(int width, int height)
    {
        return width >= 3 && height >= 3;
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return SERIALIZER;
    }

    @Override
    public IRecipeType<?> getType()
    {
        return IRecipeType.CRAFTING;
    }

    @Override
    public int getRecipeWidth()
    {
        return 3;
    }

    @Override
    public int getRecipeHeight()
    {
        return 3;
    }
}
