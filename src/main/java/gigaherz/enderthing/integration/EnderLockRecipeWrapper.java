package gigaherz.enderthing.integration;

import com.google.common.collect.Lists;
import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.recipes.LockRecipe;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EnderLockRecipeWrapper extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper
{
    @Nonnull
    private final List<ItemStack> inputs;

    @Nonnull
    private final List<ItemStack> outputs;

    public EnderLockRecipeWrapper()
    {

        inputs = Lists.newArrayList();
        inputs.addAll(Arrays.asList(LockRecipe.PATTERN).subList(0, 6));

        inputs.add(new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE));
        inputs.add(new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE));
        inputs.add(new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE));

        this.outputs = Collections.singletonList(new ItemStack(Enderthing.enderLock));
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