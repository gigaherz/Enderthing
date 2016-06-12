package gigaherz.enderthing.integration;

import gigaherz.enderthing.Enderthing;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin
{
    @Override
    public void register(@Nonnull IModRegistry registry)
    {
        registry.addRecipeHandlers(
                new EnderKeyRecipeHandler(),
                new EnderLockRecipeHandler(),
                new EnderPackRecipeHandler(),
                new MakePrivateRecipeHandler()
        );

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
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime)
    {
    }
}
