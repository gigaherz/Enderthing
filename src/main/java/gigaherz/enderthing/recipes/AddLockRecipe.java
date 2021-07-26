package gigaherz.enderthing.recipes;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.KeyUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ObjectHolder;

public class AddLockRecipe extends CustomRecipe
{
    @ObjectHolder("enderthing:add_lock")
    public static SimpleRecipeSerializer<AddLockRecipe> SERIALIZER = null;

    public AddLockRecipe(ResourceLocation id)
    {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn)
    {
        int chest = -1;
        int lock = -1;
        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            ItemStack st = inv.getItem(i);
            if (st.getItem() == Items.ENDER_CHEST || st.getItem() == Enderthing.KEY_CHEST_ITEM)
            {
                if (chest < 0)
                    chest = i;
                else return false;
            }
            else if (st.getItem() == Enderthing.LOCK)
            {
                if (lock < 0)
                    lock = i;
                else return false;
            }
            else if (st.getCount() > 0)
                return false;
        }
        // Make sure we found both.
        return chest >= 0 && lock >= 0;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv)
    {
        ItemStack chest = ItemStack.EMPTY;
        ItemStack lock = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            ItemStack st = inv.getItem(i);
            if (st.getItem() == Items.ENDER_CHEST || st.getItem() == Enderthing.KEY_CHEST_ITEM)
            {
                if (chest.getCount() == 0)
                    chest = st;
                else return ItemStack.EMPTY;
            }
            else if (st.getItem() == Enderthing.LOCK)
            {
                if (lock.getCount() == 0)
                    lock = st;
                else return ItemStack.EMPTY;
            }
            else if (st.getCount() > 0)
                return ItemStack.EMPTY;
        }

        // Make sure we found both.
        if (chest.getCount() > 0 && lock.getCount() > 0)
        {
            return KeyUtils.getKeyChest(KeyUtils.getKey(lock), KeyUtils.isPrivate(lock), KeyUtils.getBound(lock));
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
            if (st.getItem() == Enderthing.KEY_CHEST_ITEM)
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
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType()
    {
        return RecipeType.CRAFTING;
    }
}
