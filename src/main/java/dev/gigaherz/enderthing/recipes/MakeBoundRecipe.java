package dev.gigaherz.enderthing.recipes;

import com.mojang.serialization.MapCodec;
import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class MakeBoundRecipe extends CustomRecipe
{
    public static final MapCodec<MakeBoundRecipe> CODEC = MapCodec.unit(MakeBoundRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, MakeBoundRecipe> STREAM_CODEC = StreamCodec.of((_, _) -> {}, (_) -> new MakeBoundRecipe());

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
    public ItemStack assemble(CraftingInput inv)
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
    public RecipeSerializer<MakeBoundRecipe> getSerializer()
    {
        return Enderthing.MAKE_BOUND.get();
    }
}
