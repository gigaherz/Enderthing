package dev.gigaherz.enderthing.integration;

import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;

import java.util.List;
import java.util.Optional;

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
                dummyPrivateRecipe(Enderthing.KEY.get(), Enderthing.location("dummy_makeprivate_key")),
                dummyPrivateRecipe(Enderthing.KEY_CHEST_ITEM.get(), Enderthing.location("dummy_makeprivate_key_chest")),
                dummyPrivateRecipe(Enderthing.LOCK.get(), Enderthing.location("dummy_makeprivate_lock")),
                dummyPrivateRecipe(Enderthing.PACK.get(), Enderthing.location("dummy_makeprivate_pack")),

                dummyBoundRecipe(Enderthing.KEY_CHEST_ITEM.get(), Enderthing.location("dummy_makebound_key_chest")),
                dummyBoundRecipe(Enderthing.LOCK.get(), Enderthing.location("dummy_makebound_lock")),

                dummyAddLockRecipe(false, Enderthing.location("dummy_addlock")),
                dummyAddLockRecipe(true, Enderthing.location("dummy_addlock_private")),
                dummySwapLockRecipe(Ingredient.of(
                        KeyUtils.setPrivate(new ItemStack(Enderthing.KEY_CHEST_ITEM.get()), false),
                        KeyUtils.setPrivate(new ItemStack(Enderthing.KEY_CHEST_ITEM.get()), true)
                ), false, Enderthing.location("dummy_swaplock")),
                dummySwapLockRecipe(Ingredient.of(
                        KeyUtils.setPrivate(new ItemStack(Enderthing.KEY_CHEST_ITEM.get()), false),
                        KeyUtils.setPrivate(new ItemStack(Enderthing.KEY_CHEST_ITEM.get()), true)
                ), true, Enderthing.location("dummy_swaplock_private")),
                dummyRemoveLockRecipe(Enderthing.location("dummy_removelock"))
        ));
    }

    private <T extends Item> RecipeHolder<CraftingRecipe> dummyRemoveLockRecipe(ResourceLocation id)
    {
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, Ingredient.of(
                KeyUtils.setPrivate(new ItemStack(Enderthing.KEY_CHEST_ITEM.get()), false),
                KeyUtils.setPrivate(new ItemStack(Enderthing.KEY_CHEST_ITEM.get()), true)
        ));

        ItemStack result = new ItemStack(Items.ENDER_CHEST);

        return new RecipeHolder<>(id, new ShapelessRecipe(id.getNamespace() + "." + id.getPath().replace("/", "."), CraftingBookCategory.MISC, result, inputs));
    }

    private <T extends Item> RecipeHolder<CraftingRecipe> dummyAddLockRecipe(boolean isPrivate, ResourceLocation id)
    {
        return dummySwapLockRecipe(Ingredient.of(Items.ENDER_CHEST), isPrivate, id);
    }

    private <T extends Item> RecipeHolder<CraftingRecipe> dummySwapLockRecipe(Ingredient inputIngredient, boolean isPrivate, ResourceLocation id)
    {
        Ingredient lock = Ingredient.of(KeyUtils.setPrivate(new ItemStack(Enderthing.LOCK.get()), isPrivate));

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, inputIngredient, lock);

        ItemStack result = KeyUtils.setPrivate(new ItemStack(Enderthing.KEY_CHEST_ITEM.get()), isPrivate);

        return new RecipeHolder<>(id, new ShapelessRecipe(id.getNamespace() + "." + id.getPath().replace("/", "."), CraftingBookCategory.MISC, result, inputs));
    }

    private <T extends Item> RecipeHolder<CraftingRecipe> dummyBoundRecipe(T item, ResourceLocation id)
    {
        Ingredient card = Ingredient.of(KeyUtils.setBound(new ItemStack(Enderthing.CARD.get()), Util.NIL_UUID));

        ItemStack result = KeyUtils.setPrivate(new ItemStack(item), true);

        Ingredient inputIngredient = Ingredient.of(result.copy());

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, inputIngredient, card);

        KeyUtils.setBound(result, Util.NIL_UUID);

        return new RecipeHolder<>(id, new ShapelessRecipe(id.getNamespace() + "." + id.getPath().replace("/", "."), CraftingBookCategory.MISC, result, inputs));
    }

    private <T extends Item> RecipeHolder<CraftingRecipe> dummyPrivateRecipe(T item, ResourceLocation id)
    {
        Ingredient goldNugget = Ingredient.of(Items.GOLD_NUGGET);
        Ingredient inputIngredient = Ingredient.of(item);

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY,
                Ingredient.EMPTY, goldNugget, Ingredient.EMPTY,
                goldNugget, inputIngredient, goldNugget,
                Ingredient.EMPTY, goldNugget, Ingredient.EMPTY);

        ItemStack result = KeyUtils.setPrivate(new ItemStack(item), true);

        return new RecipeHolder<>(id, new ShapedRecipe(id.getNamespace() + "." + id.getPath().replace("/", "."), CraftingBookCategory.MISC,
                new ShapedRecipePattern(3, 3, inputs, Optional.empty()), result));
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration)
    {
        IModPlugin.super.registerItemSubtypes(registration);
        registerKeySubtypes(Enderthing.KEY.get(), registration);
        registerKeySubtypes(Enderthing.KEY_CHEST_ITEM.get(), registration);
        registerKeySubtypes(Enderthing.LOCK.get(), registration);
        registerKeySubtypes(Enderthing.PACK.get(), registration);
    }

    private <T extends Item> void registerKeySubtypes(T item, ISubtypeRegistration registration)
    {
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, (ingredient, context) -> {

            boolean isPrivate = KeyUtils.isPrivate(ingredient);
            var key = "key_" + (isPrivate ? "private" : "public");

            if (isPrivate)
            {
                if (KeyUtils.isBound(ingredient))
                    key += "_bound";
            }

            return key;
        });
    }
}