package gigaherz.enderthing.integration;

import gigaherz.enderthing.Enderthing;
import mezz.jei.api.*;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin
{
    @Override
    public void onJeiHelpersAvailable(IJeiHelpers jeiHelpers)
    {
    }

    @Override
    public void onItemRegistryAvailable(IItemRegistry itemRegistry)
    {
    }

    @Override
    public void register(@Nonnull IModRegistry registry)
    {
        registry.addRecipeHandlers(
                new EnderKeyRecipeHandler(),
                new EnderLockRecipeHandler(),
                new MakePrivateRecipeHandler()
        );

        registry.addRecipes(
                Arrays.asList(
                        new MakePrivateRecipeWrapper(new ItemStack(Enderthing.enderKey), new ItemStack(Enderthing.enderKey, 1, 1)),
                        new MakePrivateRecipeWrapper(new ItemStack(Enderthing.enderLock), new ItemStack(Enderthing.enderLock, 1, 1))
                ));
    }

    @Override
    public void onRecipeRegistryAvailable(@Nonnull IRecipeRegistry recipeRegistry)
    {
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime)
    {
    }
}
