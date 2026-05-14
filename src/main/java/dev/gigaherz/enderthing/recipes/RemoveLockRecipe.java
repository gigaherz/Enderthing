package dev.gigaherz.enderthing.recipes;

import com.mojang.serialization.MapCodec;
import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class RemoveLockRecipe extends CustomRecipe
{
    public static final MapCodec<RemoveLockRecipe> CODEC = MapCodec.unit(RemoveLockRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveLockRecipe> STREAM_CODEC = StreamCodec.of((_, _) -> {}, (_) -> new RemoveLockRecipe());

    private static int getChestIndex(CraftingInput inv)
    {
        int chest = -1;
        for (int i = 0; i < inv.size(); i++)
        {
            ItemStack st = inv.getItem(i);
            if (st.is(Enderthing.KEY_CHEST_ITEM))
            {
                if (chest >= 0)
                    return -1;
                chest = i;
            }
            else if (!st.isEmpty())
                return -1;
        }
        return chest;
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn)
    {
        return getChestIndex(inv) >= 0;
    }

    @Override
    public ItemStack assemble(CraftingInput inv)
    {
        int chest = getChestIndex(inv);

        if (chest < 0)
            return ItemStack.EMPTY;

        return new ItemStack(Items.ENDER_CHEST);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv)
    {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.size(), ItemStack.EMPTY);

        for (int i = 0; i < remaining.size(); ++i)
        {
            ItemStack st = inv.getItem(i);
            if (st.is(Enderthing.KEY_CHEST_ITEM))
            {
                remaining.set(i, KeyUtils.getLock(
                        KeyUtils.getKey(st),
                        KeyUtils.isPrivate(st),
                        KeyUtils.getBound(st)
                ));
            }
        }

        return remaining;
    }

    @Override
    public RecipeSerializer<RemoveLockRecipe> getSerializer()
    {
        return Enderthing.REMOVE_LOCK.get();
    }
}
