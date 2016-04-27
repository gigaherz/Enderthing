package gigaherz.enderthing.integration;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

import javax.annotation.Nonnull;

public class EnderLockRecipeHandler implements IRecipeHandler<EnderLockRecipeWrapper>
{
    @Nonnull
    @Override
    public Class<EnderLockRecipeWrapper> getRecipeClass()
    {
        return EnderLockRecipeWrapper.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid()
    {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull EnderLockRecipeWrapper recipe)
    {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull EnderLockRecipeWrapper recipe)
    {
        return true;
    }
}