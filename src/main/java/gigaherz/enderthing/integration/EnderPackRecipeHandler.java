package gigaherz.enderthing.integration;

import gigaherz.enderthing.recipes.PackRecipe;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

import javax.annotation.Nonnull;

public class EnderPackRecipeHandler implements IRecipeHandler<PackRecipe>
{
    @Nonnull
    @Override
    public Class<PackRecipe> getRecipeClass()
    {
        return PackRecipe.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid()
    {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull PackRecipe recipe)
    {
        return new EnderPackRecipeWrapper();
    }

    @Override
    public boolean isRecipeValid(@Nonnull PackRecipe recipe)
    {
        return true;
    }
}