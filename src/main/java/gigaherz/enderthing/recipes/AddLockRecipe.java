package gigaherz.enderthing.recipes;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.KeyUtils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ObjectHolder;

public class AddLockRecipe extends SpecialRecipe
{
    @ObjectHolder("enderthing:add_lock")
    public static SpecialRecipeSerializer<AddLockRecipe> SERIALIZER = null;

    public AddLockRecipe(ResourceLocation id)
    {
        super(id);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn)
    {
        int chest = -1;
        int lock = -1;
        for(int i=0;i<inv.getSizeInventory();i++)
        {
            ItemStack st = inv.getStackInSlot(i);
            if (st.getItem() == Items.ENDER_CHEST || st.getItem() == Enderthing.KEY_CHEST_ITEM)
            {
                if (chest < 0)
                    chest = i;
                else return false;
            }
            else if(st.getItem() == Enderthing.LOCK)
            {
                if (lock < 0)
                    lock = i;
                else return false;
            }
        }
        // Make sure we found both.
        return chest >= 0 && lock >= 0;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv)
    {
        ItemStack chest = ItemStack.EMPTY;
        ItemStack lock = ItemStack.EMPTY;

        for(int i=0;i<inv.getSizeInventory();i++)
        {
            ItemStack st = inv.getStackInSlot(i);
            if (st.getItem() == Items.ENDER_CHEST || st.getItem() == Enderthing.KEY_CHEST_ITEM)
            {
                if (chest.getCount() == 0)
                    chest = st;
                else return ItemStack.EMPTY;
            }
            else if(st.getItem() == Enderthing.LOCK)
            {
                if (lock.getCount() == 0)
                    lock = st;
                else return ItemStack.EMPTY;
            }
        }

        // Make sure we found both.
        if (chest.getCount() > 0 && lock.getCount() > 0)
        {
            return KeyUtils.setPrivate(KeyUtils.setKey(chest.copy(), KeyUtils.getKey(lock)), KeyUtils.isPrivate(lock));
        }

        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv)
    {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

        for(int i = 0; i < remaining.size(); ++i) {
            ItemStack st = inv.getStackInSlot(i);
            if (st.getItem() == Items.ENDER_CHEST || st.getItem() == Enderthing.KEY_CHEST_ITEM)
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
    public boolean canFit(int width, int height)
    {
        return width*height >= 2;
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
}
