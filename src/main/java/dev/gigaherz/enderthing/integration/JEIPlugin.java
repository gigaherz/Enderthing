package dev.gigaherz.enderthing.integration;

import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin
{
    private static final ResourceLocation ID = Enderthing.location("jei_plugin");

    @Override
    public ResourceLocation getPluginUid()
    {
        return ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {
        registration.addRecipes(RecipeTypes.CRAFTING, List.of(
                dummyPrivateRecipe(Enderthing.KEY, Enderthing.location("dummy_makeprivate_key")),
                dummyPrivateRecipe(Enderthing.KEY_CHEST_ITEM, Enderthing.location("dummy_makeprivate_key_chest")),
                dummyPrivateRecipe(Enderthing.LOCK, Enderthing.location("dummy_makeprivate_lock")),
                dummyPrivateRecipe(Enderthing.PACK, Enderthing.location("dummy_makeprivate_pack")),

                dummyBoundRecipe(Enderthing.KEY_CHEST_ITEM, Enderthing.location("dummy_makebound_key_chest")),
                dummyBoundRecipe(Enderthing.LOCK, Enderthing.location("dummy_makebound_lock")),

                dummyAddLockRecipe(false, Enderthing.location("dummy_addlock")),
                dummyAddLockRecipe(true, Enderthing.location("dummy_addlock_private")),
                dummySwapLockRecipe(Ingredient.of(
                        Enderthing.KEY_CHEST_ITEM.makeStack(false),
                        Enderthing.KEY_CHEST_ITEM.makeStack(true)
                ), false, Enderthing.location("dummy_swaplock")),
                dummySwapLockRecipe(Ingredient.of(
                        Enderthing.KEY_CHEST_ITEM.makeStack(false),
                        Enderthing.KEY_CHEST_ITEM.makeStack(true)
                ), true, Enderthing.location("dummy_swaplock_private")),
                dummyRemoveLockRecipe(Enderthing.location("dummy_removelock"))
        ));
    }

    private <T extends Item> CraftingRecipe dummyRemoveLockRecipe(ResourceLocation id)
    {
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, Ingredient.of(
                Enderthing.KEY_CHEST_ITEM.makeStack(false),
                Enderthing.KEY_CHEST_ITEM.makeStack(true)
        ));

        ItemStack result = new ItemStack(Items.ENDER_CHEST);

        return new ShapelessRecipe(id, id.getNamespace() + "." + id.getPath().replace("/", "."), result, inputs);
    }

    private <T extends Item> CraftingRecipe dummyAddLockRecipe(boolean isPrivate, ResourceLocation id)
    {
        return dummySwapLockRecipe(Ingredient.of(Items.ENDER_CHEST), isPrivate, id);
    }

    private <T extends Item> CraftingRecipe dummySwapLockRecipe(Ingredient inputIngredient, boolean isPrivate, ResourceLocation id)
    {
        Ingredient lock = Ingredient.of(Enderthing.LOCK.makeStack(isPrivate));

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, inputIngredient, lock);

        ItemStack result = Util.make(new ItemStack(Enderthing.KEY_CHEST_ITEM), r -> {
            if (r.getItem() instanceof KeyUtils.IKeyHolder holder)
                holder.setPrivate(r, isPrivate);
        });

        return new ShapelessRecipe(id, id.getNamespace() + "." + id.getPath().replace("/", "."), result, inputs);
    }

    private <T extends Item & KeyUtils.IBindableKeyHolder> CraftingRecipe dummyBoundRecipe(T item, ResourceLocation id)
    {
        Ingredient card = Ingredient.of(Enderthing.CARD);

        ItemStack result = Util.make(new ItemStack(item), r -> {
            if (r.getItem() instanceof KeyUtils.IKeyHolder holder)
                holder.setPrivate(r, true);
        });

        Ingredient inputIngredient = Ingredient.of(result.copy());

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, inputIngredient, card);

        item.setBound(result, Util.NIL_UUID);

        return new ShapelessRecipe(id, id.getNamespace() + "." + id.getPath().replace("/", "."), result, inputs);
    }

    private <T extends Item & KeyUtils.IKeyHolder> CraftingRecipe dummyPrivateRecipe(T item, ResourceLocation id)
    {
        Ingredient goldNugget = Ingredient.of(Items.GOLD_NUGGET);
        Ingredient inputIngredient = Ingredient.of(item);

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY,
                Ingredient.EMPTY, goldNugget, Ingredient.EMPTY,
                goldNugget,    inputIngredient, goldNugget,
                Ingredient.EMPTY, goldNugget, Ingredient.EMPTY);

        ItemStack result = Util.make(new ItemStack(item), r -> {
            if (r.getItem() instanceof KeyUtils.IKeyHolder holder)
                holder.setPrivate(r, true);
        });

        return new ShapedRecipe(id, id.getNamespace() + "." + id.getPath().replace("/", "."), 3, 3, inputs, result);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration)
    {
        IModPlugin.super.registerItemSubtypes(registration);
        registerKeySubtypes(Enderthing.KEY, registration);
        registerKeySubtypes(Enderthing.KEY_CHEST_ITEM, registration);
        registerKeySubtypes(Enderthing.LOCK, registration);
        registerKeySubtypes(Enderthing.PACK, registration);
    }

    private <T extends Item & KeyUtils.IKeyHolder> void registerKeySubtypes(T item, ISubtypeRegistration registration)
    {
        registration.registerSubtypeInterpreter(item, (ingredient, context) -> {
            ItemStack stack = new ItemStack(ingredient.getItem());
            stack.setTag(ingredient.getTag());
            boolean isPrivate = item.isPrivate(stack);
            var key = "key_" + (isPrivate?"private_":"public");

            if(isPrivate && ingredient.getItem() instanceof KeyUtils.IBindable bindable)
            {
                if (bindable.isBound(stack))
                    key = key + "_bound";
            }

            return key;
        });
    }
}