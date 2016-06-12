package gigaherz.enderthing.integration;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

import javax.annotation.Nonnull;

public class MakePrivateRecipeHandler implements IRecipeHandler<MakePrivateRecipeWrapper>
{
    @Nonnull
    @Override
    public Class<MakePrivateRecipeWrapper> getRecipeClass()
    {
        return MakePrivateRecipeWrapper.class;
    }

    @Deprecated
    @Nonnull
    @Override
    public String getRecipeCategoryUid()
    {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid(@Nonnull MakePrivateRecipeWrapper recipe)
    {
        return getRecipeCategoryUid();
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull MakePrivateRecipeWrapper recipe)
    {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull MakePrivateRecipeWrapper recipe)
    {
        return true;
    }
}