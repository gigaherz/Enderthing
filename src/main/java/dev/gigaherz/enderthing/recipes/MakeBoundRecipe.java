package dev.gigaherz.enderthing.recipes;

import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class MakeBoundRecipe extends CustomRecipe
{
    public MakeBoundRecipe(CraftingBookCategory cat)
    {
        super(cat);
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn)
    {
        int holder = -1;
        int card = -1;
        for (int i = 0; i < inv.size(); i++)
        {
            ItemStack st = inv.getItem(i);
            if (st.is(KeyUtils.CAN_MAKE_BOUND) && KeyUtils.isPrivate(st))
            {
                if (holder < 0)
                    holder = i;
                else return false;
            }
            else if (st.getItem() == Enderthing.CARD.get())
            {
                if (card < 0)
                    card = i;
                else return false;
            }
            else if (st.getCount() > 0)
                return false;
        }
        // Make sure we found both.
        return holder >= 0 && card >= 0;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider lookup)
    {
        ItemStack holder = ItemStack.EMPTY;
        ItemStack card = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++)
        {
            ItemStack st = inv.getItem(i);
            if (st.is(KeyUtils.CAN_MAKE_BOUND) && KeyUtils.isPrivate(st))
            {
                if (holder.getCount() == 0)
                    holder = st;
                else return ItemStack.EMPTY;
            }
            else if (st.getItem() == Enderthing.CARD.get())
            {
                if (card.getCount() == 0)
                    card = st;
                else return ItemStack.EMPTY;
            }
            else if (st.getCount() > 0)
                return ItemStack.EMPTY;
        }

        // Make sure we found both.
        if (holder.getCount() > 0 && card.getCount() > 0)
        {
            return KeyUtils.setBound(holder.copy(), KeyUtils.getBound(card));
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return Enderthing.MAKE_BOUND.get();
    }

    @Override
    public RecipeType<?> getType()
    {
        return RecipeType.CRAFTING;
    }
}
