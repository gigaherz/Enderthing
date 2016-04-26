package gigaherz.enderthing.integration;

import mezz.jei.api.*;

import javax.annotation.Nonnull;
import java.util.Arrays;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin
{
    @Override
    public void register(@Nonnull IModRegistry registry)
    {
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();

        registry.addRecipeHandlers(
                new EnderKeyRecipeHandler(),
                new EnderLockRecipeHandler()
        );
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime)
    {
    }
}
