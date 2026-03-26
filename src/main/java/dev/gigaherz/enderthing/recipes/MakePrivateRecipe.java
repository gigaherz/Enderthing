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
import net.neoforged.neoforge.common.Tags;

public class MakePrivateRecipe extends CustomRecipe
{
    public static final MapCodec<MakePrivateRecipe> CODEC = MapCodec.unit(MakePrivateRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, MakePrivateRecipe> STREAM_CODEC = StreamCodec.of((_, _) -> {}, (_) -> new MakePrivateRecipe());

    @Override
    public boolean matches(CraftingInput inv, Level worldIn)
    {
        if (inv.width() != 3 || inv.height() != 3)
            return false;

        ItemStack centerSlot = inv.getItem(1, 1);
        return inv.getItem(0, 0).getCount() == 0
                && inv.getItem(0, 1).is(Tags.Items.NUGGETS_GOLD)
                && inv.getItem(0, 2).getCount() == 0
                && inv.getItem(1, 0).is(Tags.Items.NUGGETS_GOLD)
                && inv.getItem(1, 2).is(Tags.Items.NUGGETS_GOLD)
                && inv.getItem(2, 0).getCount() == 0
                && inv.getItem(2, 1).is(Tags.Items.NUGGETS_GOLD)
                && inv.getItem(2, 2).getCount() == 0
                && centerSlot.is(KeyUtils.CAN_MAKE_PRIVATE)
                && !KeyUtils.isPrivate(centerSlot);
    }

    @Override
    public ItemStack assemble(CraftingInput inv)
    {
        if (inv.width() != 3 || inv.height() != 3)
            return ItemStack.EMPTY;

        return KeyUtils.setPrivate(inv.getItem(1, 1).copyWithCount(1), true);
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer()
    {
        return Enderthing.MAKE_PRIVATE.get();
    }
}
