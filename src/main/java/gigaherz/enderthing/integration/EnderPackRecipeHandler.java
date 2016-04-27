package gigaherz.enderthing.integration;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

import javax.annotation.Nonnull;

public class EnderPackRecipeHandler implements IRecipeHandler<EnderPackRecipeWrapper>
{
    @Nonnull
    @Override
    public Class<EnderPackRecipeWrapper> getRecipeClass()
    {
        return EnderPackRecipeWrapper.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid()
    {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull EnderPackRecipeWrapper recipe)
    {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull EnderPackRecipeWrapper recipe)
    {
        return true;
    }
}