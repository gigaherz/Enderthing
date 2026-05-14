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

public class AddLockRecipe extends CustomRecipe
{
    public static final MapCodec<AddLockRecipe> CODEC = MapCodec.unit(AddLockRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, AddLockRecipe> STREAM_CODEC = StreamCodec.of((_, _) -> {}, (_) -> new AddLockRecipe());

    private static int getLockIndex(CraftingInput inv)
    {
        int chest = -1;
        int lock = -1;
        for (int i = 0; i < inv.size(); i++)
        {
            ItemStack st = inv.getItem(i);
            if (st.is(Items.ENDER_CHEST) || st.is(Enderthing.KEY_CHEST_ITEM))
            {
                if (chest >= 0)
                    return -1;
                chest = i;
            }
            else if (st.is(Enderthing.LOCK))
            {
                if (lock >= 0)
                    return -1;
                lock = i;
            }
            else if (!st.isEmpty())
                return -1;
        }
        // Make sure we found both.
        return chest >= 0 && lock >= 0 ? lock : -1;
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn)
    {
        return getLockIndex(inv) >= 0;
    }

    @Override
    public ItemStack assemble(CraftingInput inv)
    {
        int lock = getLockIndex(inv);

        if (lock < 0)
            return ItemStack.EMPTY;

        var lockStack = inv.getItem(lock);
        return KeyUtils.getKeyChest(KeyUtils.getKey(lockStack), KeyUtils.isPrivate(lockStack), KeyUtils.getBound(lockStack));
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv)
    {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.size(), ItemStack.EMPTY);

        for (int i = 0; i < remaining.size(); ++i)
        {
            ItemStack st = inv.getItem(i);
            if (st.getItem() == Enderthing.KEY_CHEST_ITEM.get())
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
    public RecipeSerializer<AddLockRecipe> getSerializer()
    {
        return Enderthing.ADD_LOCK.get();
    }
}
