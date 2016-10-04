package gigaherz.enderthing.integration;

import com.google.common.collect.Lists;
import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.recipes.PackRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EnderPackRecipeWrapper extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper
{
    @Nonnull
    private final List<ItemStack> inputs;

    @Nonnull
    private final ItemStack output;

    public EnderPackRecipeWrapper()
    {
        inputs = Lists.newArrayList();
        inputs.addAll(Arrays.asList(PackRecipe.PATTERN));

        inputs.set(3, new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE));
        inputs.set(4, new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE));
        inputs.set(5, new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE));

        output = new ItemStack(Enderthing.enderPack);
    }

    @Override
    public void getIngredients(IIngredients ingredients)
    {
        ingredients.setInputs(ItemStack.class, inputs);
        ingredients.setOutput(ItemStack.class, output);
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