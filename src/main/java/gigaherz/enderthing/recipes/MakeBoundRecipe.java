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
import net.minecraftforge.registries.ObjectHolder;

public class MakeBoundRecipe extends SpecialRecipe
{
    @ObjectHolder("enderthing:make_bound")
    public static SpecialRecipeSerializer<MakeBoundRecipe> SERIALIZER = null;

    public MakeBoundRecipe(ResourceLocation id)
    {
        super(id);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn)
    {
        int holder = -1;
        int card = -1;
        for(int i=0;i<inv.getSizeInventory();i++)
        {
            ItemStack st = inv.getStackInSlot(i);
            if ((st.getItem() instanceof KeyUtils.IBindableKeyHolder) && KeyUtils.isPrivate(st))
            {
                if (holder < 0)
                    holder = i;
                else return false;
            }
            else if(st.getItem() == Enderthing.CARD)
            {
                if (card < 0)
                    card = i;
                else return false;
            }
            else if(st.getCount() > 0)
                return false;
        }
        // Make sure we found both.
        return holder >= 0 && card >= 0;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv)
    {
        ItemStack holder = ItemStack.EMPTY;
        ItemStack card = ItemStack.EMPTY;

        for(int i=0;i<inv.getSizeInventory();i++)
        {
            ItemStack st = inv.getStackInSlot(i);
            if ((st.getItem() instanceof KeyUtils.IBindableKeyHolder) && KeyUtils.isPrivate(st))
            {
                if (holder.getCount() == 0)
                    holder = st;
                else return ItemStack.EMPTY;
            }
            else if(st.getItem() == Enderthing.CARD)
            {
                if (card.getCount() == 0)
                    card = st;
                else return ItemStack.EMPTY;
            }
            else if(st.getCount() > 0)
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
