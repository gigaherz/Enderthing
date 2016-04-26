package gigaherz.enderthing.integration;

import com.google.common.collect.Lists;
import gigaherz.enderthing.items.ItemEnderLock;
import gigaherz.enderthing.recipes.LockRecipe;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EnderLockRecipeWrapper extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper
{
    public static List<EnderLockRecipeWrapper> getAllCombinations()
    {
        List<EnderLockRecipeWrapper> combinations = Lists.newArrayList();

        for (int c1 = 0; c1 < 16; c1++)
        {
            for (int c2 = 0; c2 < 16; c2++)
            {
                for (int c3 = 0; c3 < 16; c3++)
                {
                    combinations.add(new EnderLockRecipeWrapper(c1, c2, c3));
                }
            }
        }

        return combinations;
    }


    @Nonnull
    private final List<ItemStack> inputs;

    @Nonnull
    private final List<ItemStack> outputs;

    public EnderLockRecipeWrapper(int c1, int c2, int c3) {

        inputs = Lists.newArrayList();
        inputs.addAll(Arrays.asList(LockRecipe.PATTERN).subList(0, 6));

        inputs.add(new ItemStack(Blocks.WOOL, 1, c1));
        inputs.add(new ItemStack(Blocks.WOOL, 1, c2));
        inputs.add(new ItemStack(Blocks.WOOL, 1, c3));

        this.outputs = Collections.singletonList(
                ItemEnderLock.getItem(c1, c2, c3)
        );
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