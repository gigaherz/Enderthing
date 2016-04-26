package gigaherz.enderthing.integration;

import gigaherz.enderthing.recipes.KeyRecipe;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

import javax.annotation.Nonnull;
import java.util.Random;

public class EnderKeyRecipeHandler implements IRecipeHandler<KeyRecipe>
{
    Random rand = new Random();

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
        int c1 = rand.nextInt(16);
        int c2 = rand.nextInt(16);
        int c3 = rand.nextInt(16);

        return new EnderKeyRecipeWrapper(c1, c2, c3);
    }

    @Override
    public boolean isRecipeValid(@Nonnull KeyRecipe recipe)
    {
        return true;
    }
}