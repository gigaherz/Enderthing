package gigaherz.enderthing.integration;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

import javax.annotation.Nonnull;

public class EnderKeyRecipeHandler implements IRecipeHandler<EnderKeyRecipeWrapper>
{
    @Nonnull
    @Override
    public Class<EnderKeyRecipeWrapper> getRecipeClass()
    {
        return EnderKeyRecipeWrapper.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid()
    {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull EnderKeyRecipeWrapper recipe)
    {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull EnderKeyRecipeWrapper recipe)
    {
        return true;
    }
}