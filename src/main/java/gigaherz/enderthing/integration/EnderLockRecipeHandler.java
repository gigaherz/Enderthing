package gigaherz.enderthing.integration;

import gigaherz.enderthing.recipes.LockRecipe;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

import javax.annotation.Nonnull;

public class EnderLockRecipeHandler implements IRecipeHandler<LockRecipe>
{
    @Nonnull
    @Override
    public Class<LockRecipe> getRecipeClass()
    {
        return LockRecipe.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid()
    {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull LockRecipe recipe)
    {
        return new EnderLockRecipeWrapper();
    }

    @Override
    public boolean isRecipeValid(@Nonnull LockRecipe recipe)
    {
        return true;
    }
}