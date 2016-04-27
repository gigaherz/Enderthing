package gigaherz.enderthing.integration;

import com.google.common.collect.Lists;
import gigaherz.enderthing.recipes.MakePrivateRecipe;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MakePrivateRecipeWrapper extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper
{
    @Nonnull
    private final List<ItemStack> inputs;

    @Nonnull
    private final List<ItemStack> outputs;

    public MakePrivateRecipeWrapper(ItemStack centerInput, ItemStack output)
    {
        inputs = Lists.newArrayList();
        inputs.addAll(Arrays.asList(MakePrivateRecipe.PATTERN));

        inputs.set(4, centerInput);

        this.outputs = Collections.singletonList(output);
    }

    @Override
    @Nonnull
    public List<ItemStack> getInputs()
    {
        return inputs;
    }

    @Override
    @Nonnull
    public List<ItemStack> getOutputs()
    {
        return outputs;
    }

    @Override
    public int getWidth()
    {
        return 3;
    }

    @Override
    public int getHeight()
    {
        return 3;
    }
}