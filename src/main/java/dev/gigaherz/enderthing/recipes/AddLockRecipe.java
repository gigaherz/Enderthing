package dev.gigaherz.enderthing.recipes;

import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
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

public class AddLockRecipe extends CustomRecipe
{
    public AddLockRecipe(CraftingBookCategory cat)
    {
        super(cat);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn)
    {
        boolean chestHasLock = false;
        int chest = -1;
        int lock = -1;
        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            ItemStack st = inv.getItem(i);
            if (st.getItem() == Items.ENDER_CHEST)
            {
                if (chest >= 0)
                    return false;
                chest = i;
            }
            else if (st.getItem() == Enderthing.KEY_CHEST_ITEM.get())
            {
                if (chest >= 0)
                    return false;
                chest = i;
                chestHasLock = true;
            }
            else if (st.getItem() == Enderthing.LOCK.get())
            {
                if (lock >= 0)
                    return false;
                lock = i;
            }
            else if (st.getCount() > 0)
                return false;
        }
        // Make sure we found either both, or a chest with lock.
        return chest >= 0 && (chestHasLock || lock >= 0);
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, HolderLookup.Provider lookup)
    {
        ItemStack chest = ItemStack.EMPTY;
        ItemStack lock = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            ItemStack st = inv.getItem(i);
            if (st.getItem() == Items.ENDER_CHEST || st.getItem() == Enderthing.KEY_CHEST_ITEM.get())
            {
                if (chest.getCount() > 0)
                    return ItemStack.EMPTY;
                chest = st;
            }
            else if (st.getItem() == Enderthing.LOCK.get())
            {
                if (lock.getCount() > 0)
                    return ItemStack.EMPTY;
                lock = st;
            }
            else if (st.getCount() > 0)
                return ItemStack.EMPTY;
        }

        // Make sure we found both.
        if (chest.getCount() > 0)
        {
            if (lock.getCount() > 0)
                return KeyUtils.getKeyChest(KeyUtils.getKey(lock), KeyUtils.isPrivate(lock), KeyUtils.getBound(lock));
            else
                return new ItemStack(Items.ENDER_CHEST);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv)
    {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

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
    public boolean canCraftInDimensions(int width, int height)
    {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return Enderthing.ADD_LOCK.get();
    }

    @Override
    public RecipeType<?> getType()
    {
        return RecipeType.CRAFTING;
    }
}
