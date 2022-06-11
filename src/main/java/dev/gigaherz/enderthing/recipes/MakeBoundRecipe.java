package dev.gigaherz.enderthing.recipes;

import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ObjectHolder;

public class MakeBoundRecipe extends CustomRecipe
{
    public MakeBoundRecipe(ResourceLocation id)
    {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn)
    {
        int holder = -1;
        int card = -1;
        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            ItemStack st = inv.getItem(i);
            if ((st.getItem() instanceof KeyUtils.IBindableKeyHolder) && KeyUtils.isPrivate(st))
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
    public ItemStack assemble(CraftingContainer inv)
    {
        ItemStack holder = ItemStack.EMPTY;
        ItemStack card = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            ItemStack st = inv.getItem(i);
            if ((st.getItem() instanceof KeyUtils.IBindableKeyHolder) && KeyUtils.isPrivate(st))
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
