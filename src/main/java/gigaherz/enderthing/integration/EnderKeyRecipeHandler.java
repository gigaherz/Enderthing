package gigaherz.enderthing.integration;

import gigaherz.enderthing.recipes.KeyRecipe;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

import javax.annotation.Nonnull;

public class EnderKeyRecipeHandler implements IRecipeHandler<KeyRecipe>
{
    @Nonnull
    @Override
    public Class<KeyRecipe> getRecipeClass()
    {
        return KeyRecipe.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid() {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull KeyRecipe recipe)
    {
        return new EnderKeyRecipeWrapper();
    }

    @Override
    public boolean isRecipeValid(@Nonnull KeyRecipe recipe)
    {
        return true;
    }
}