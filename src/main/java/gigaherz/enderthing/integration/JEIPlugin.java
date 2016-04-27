package gigaherz.enderthing.integration;

import gigaherz.enderthing.Enderthing;
import mezz.jei.api.*;
import mezz.jei.api.INbtIgnoreList;
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
                new EnderPackRecipeHandler(),
                new MakePrivateRecipeHandler()
        );

        INbtIgnoreList nbtIgnoreList = registry.getJeiHelpers().getNbtIgnoreList();
        nbtIgnoreList.ignoreNbtTagNames(Enderthing.enderKey, "Color1");
        nbtIgnoreList.ignoreNbtTagNames(Enderthing.enderKey, "Color2");
        nbtIgnoreList.ignoreNbtTagNames(Enderthing.enderKey, "Color3");
        nbtIgnoreList.ignoreNbtTagNames(Enderthing.enderLock, "Color1");
        nbtIgnoreList.ignoreNbtTagNames(Enderthing.enderLock, "Color2");
        nbtIgnoreList.ignoreNbtTagNames(Enderthing.enderLock, "Color3");
        nbtIgnoreList.ignoreNbtTagNames(Enderthing.enderPack, "Color1");
        nbtIgnoreList.ignoreNbtTagNames(Enderthing.enderPack, "Color2");
        nbtIgnoreList.ignoreNbtTagNames(Enderthing.enderPack, "Color3");

        registry.addRecipes(
                Arrays.asList(
                        new EnderKeyRecipeWrapper(),
                        new EnderLockRecipeWrapper(),
                        new EnderPackRecipeWrapper(),
                        new MakePrivateRecipeWrapper(new ItemStack(Enderthing.enderKey), new ItemStack(Enderthing.enderKey, 1, 1)),
                        new MakePrivateRecipeWrapper(new ItemStack(Enderthing.enderLock), new ItemStack(Enderthing.enderLock, 1, 1)),
                        new MakePrivateRecipeWrapper(new ItemStack(Enderthing.enderPack), new ItemStack(Enderthing.enderPack, 1, 1))
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
