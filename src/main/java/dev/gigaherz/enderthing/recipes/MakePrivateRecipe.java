package dev.gigaherz.enderthing.recipes;

import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.IShapedRecipe;

public class MakePrivateRecipe extends CustomRecipe implements IShapedRecipe<CraftingContainer>
{
    public MakePrivateRecipe(CraftingBookCategory cat)
    {
        super(cat);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn)
    {
        ItemStack centerSlot = inv.getItem(4);
        return inv.getItem(0).getCount() == 0
                && inv.getItem(1).getItem() == Items.GOLD_NUGGET
                && inv.getItem(2).getCount() == 0
                && inv.getItem(3).getItem() == Items.GOLD_NUGGET
                && inv.getItem(5).getItem() == Items.GOLD_NUGGET
                && inv.getItem(6).getCount() == 0
                && inv.getItem(7).getItem() == Items.GOLD_NUGGET
                && inv.getItem(8).getCount() == 0
                && centerSlot.is(KeyUtils.CAN_MAKE_PRIVATE)
                && !KeyUtils.isPrivate(centerSlot);
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, HolderLookup.Provider lookup)
    {
        ItemStack output = inv.getItem(4).copy();

        KeyUtils.setPrivate(output, true);

        return output;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return Enderthing.MAKE_PRIVATE.get();
    }

    @Override
    public RecipeType<?> getType()
    {
        return RecipeType.CRAFTING;
    }

    @Override
    public int getWidth()
    {
        return 3;
    }

    @Override
    public int getHeight()
    {
        return 3;
    }
}
