package dev.gigaherz.enderthing.integration;

import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import dev.gigaherz.enderthing.recipes.AddLockRecipe;
import dev.gigaherz.enderthing.recipes.MakeBoundRecipe;
import dev.gigaherz.enderthing.recipes.MakePrivateRecipe;
import dev.gigaherz.enderthing.recipes.RemoveLockRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@JeiPlugin
public class JEIPlugin implements IModPlugin
{
    private static final Identifier ID = Enderthing.location("jei_plugin");
    public static final UUID DUMMY_UUID = UUID.nameUUIDFromBytes("dummy".getBytes(StandardCharsets.UTF_8));

    @Override
    public Identifier getPluginUid()
    {
        return ID;
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration)
    {
        registration.getCraftingCategory().addExtension(
                MakePrivateRecipe.class, new MakePrivateRecipeCategoryExtension()
        );
        registration.getCraftingCategory().addExtension(
                AddLockRecipe.class, new AddLockRecipeCategoryExtension()
        );
        registration.getCraftingCategory().addExtension(
                RemoveLockRecipe.class, new RemoveLockRecipeCategoryExtension()
        );
        registration.getCraftingCategory().addExtension(
                MakeBoundRecipe.class, new MakeBoundRecipeCategoryExtension()
        );
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration)
    {
        IModPlugin.super.registerItemSubtypes(registration);
        registerKeySubtypes("key", Enderthing.KEY.get(), registration, false);
        registerKeySubtypes("key_chest", Enderthing.KEY_CHEST_ITEM.get(), registration, true);
        registerKeySubtypes("lock", Enderthing.LOCK.get(), registration, true);
        registerKeySubtypes("pack", Enderthing.PACK.get(), registration, false);
    }

    private <T extends Item> void registerKeySubtypes(String thing, T item, ISubtypeRegistration registration, boolean canBind)
    {
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, (ingredient, context) -> {

            boolean isPrivate = KeyUtils.isPrivate(ingredient);
            var key = thing + "_" + (isPrivate ? "private" : "public");

            if (isPrivate && canBind)
            {
                if (KeyUtils.isBound(ingredient))
                    key += "_bound";
            }

            return key;
        });
    }

    private static ItemStackTemplate toTemplate(ItemStack is)
    {
        return new ItemStackTemplate(is.getItem(), is.getCount(), is.getComponentsPatch());
    }

    private static class MakeBoundRecipeCategoryExtension implements ICraftingCategoryExtension<MakeBoundRecipe>
    {
        public static final SlotDisplay DUMMY_CARD = new SlotDisplay.ItemStackSlotDisplay(toTemplate(KeyUtils.setBound(Enderthing.CARD.toStack(), DUMMY_UUID)));
        public static final List<ItemStack> ALL_INPUTS = List.of(
                KeyUtils.setPrivate(Enderthing.KEY_CHEST_ITEM.toStack(), true),
                KeyUtils.setPrivate(Enderthing.LOCK.toStack(), true)
        );
        public static final List<ItemStack> ALL_OUTPUTS = List.of(
                KeyUtils.setBound(KeyUtils.setPrivate(Enderthing.KEY_CHEST_ITEM.toStack(), true), DUMMY_UUID),
                KeyUtils.setBound(KeyUtils.setPrivate(Enderthing.LOCK.toStack(), true), DUMMY_UUID)
        );

        @Override
        public List<SlotDisplay> getIngredients(RecipeHolder<MakeBoundRecipe> recipeHolder)
        {
            return getIngredients(ALL_INPUTS, DUMMY_UUID);
        }

        @Override
        public int getWidth(RecipeHolder<MakeBoundRecipe> recipeHolder)
        {
            return 2;
        }

        @Override
        public int getHeight(RecipeHolder<MakeBoundRecipe> recipeHolder)
        {
            return 1;
        }

        private List<SlotDisplay> getIngredients(List<ItemStack> chests, UUID bound)
        {
            var inputChest = getStackDisplays(chests);
            var inputCard = new SlotDisplay.ItemStackSlotDisplay(toTemplate(KeyUtils.setBound(Enderthing.CARD.toStack(), bound)));
            return List.of(inputChest, inputCard);
        }

        @Override
        public void setRecipe(RecipeHolder<MakeBoundRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses)
        {
            var focusedOutputs = focuses.getFocuses(VanillaTypes.ITEM_STACK, RecipeIngredientRole.OUTPUT).map(f -> f.getTypedValue().getIngredient()).toList();
            var focusedInputs = focuses.getFocuses(VanillaTypes.ITEM_STACK, RecipeIngredientRole.INPUT).map(f -> f.getTypedValue().getIngredient())
                    .filter(stack -> stack.is(Enderthing.KEY_CHEST_ITEM))
                    .toList();
            var focusedCards = focuses.getFocuses(VanillaTypes.ITEM_STACK, RecipeIngredientRole.INPUT).map(f -> f.getTypedValue().getIngredient())
                    .filter(stack -> stack.is(Enderthing.CARD))
                    .toList();

            UUID bound = DUMMY_UUID;
            if (!focusedCards.isEmpty()) {
                bound = Objects.requireNonNullElse(KeyUtils.getBound(focusedCards.getFirst()), DUMMY_UUID);
            }

            List<ItemStack> inputs, outputs;
            if (!focusedOutputs.isEmpty())
            {
                inputs = focusedOutputs.stream().map(stack -> KeyUtils.setBound(stack.copy(), null)).toList();
                outputs = focusedOutputs.stream().map(stack -> KeyUtils.setBound(stack.copy(), DUMMY_UUID)).toList();
            }
            else if (!focusedInputs.isEmpty())
            {
                inputs = focusedInputs.stream().map(stack -> KeyUtils.setBound(stack.copy(), null)).toList();
                outputs = focusedInputs.stream().map(stack -> KeyUtils.setBound(stack.copy(), DUMMY_UUID)).toList();
            }
            else
            {
                inputs = ALL_INPUTS;
                outputs = ALL_OUTPUTS;
            }

            craftingGridHelper.createAndSetOutputs(builder, getStackDisplays(outputs));
            craftingGridHelper.createAndSetIngredientsFromDisplays(builder, getIngredients(inputs, bound), 2, 1);
        }

        @Override
        public void onDisplayedIngredientsUpdate(RecipeHolder<MakeBoundRecipe> recipeHolder, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses)
        {
            if (recipeSlots.size() < 2)
                return;

            var output = recipeSlots.get(0);
            var input = recipeSlots.get(2);

            input.getDisplayedItemStack().ifPresent(inStack -> {
                output.getDisplayedItemStack().ifPresent(outStack -> {
                    var in1 = inStack.copy();
                    if (KeyUtils.isPrivate(outStack))
                    {
                        KeyUtils.setPrivate(in1, true);
                        KeyUtils.setBound(in1, KeyUtils.getBound(outStack));
                    }
                    KeyUtils.setKey(in1, KeyUtils.getKey(outStack));
                    input.createDisplayOverrides().add(in1);
                });
            });
        }
    }

    private static class RemoveLockRecipeCategoryExtension implements ICraftingCategoryExtension<RemoveLockRecipe>
    {
        public static final SlotDisplay OUTPUT = new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(Items.ENDER_CHEST));
        public static final List<ItemStack> ALL_KEY_CHESTS = List.of(
                KeyUtils.setPrivate(Enderthing.KEY_CHEST_ITEM.toStack(), false),
                KeyUtils.setPrivate(Enderthing.KEY_CHEST_ITEM.toStack(), true),
                KeyUtils.setBound(KeyUtils.setPrivate(Enderthing.KEY_CHEST_ITEM.toStack(), true), DUMMY_UUID)
        );
        public static final SlotDisplay ALL_INPUTS = getStackDisplays(ALL_KEY_CHESTS);

        @Override
        public List<SlotDisplay> getIngredients(RecipeHolder<RemoveLockRecipe> recipeHolder)
        {
            return List.of(ALL_INPUTS);
        }

        @Override
        public int getWidth(RecipeHolder<RemoveLockRecipe> recipeHolder)
        {
            return 1;
        }

        @Override
        public int getHeight(RecipeHolder<RemoveLockRecipe> recipeHolder)
        {
            return 1;
        }

        @Override
        public void setRecipe(RecipeHolder<RemoveLockRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses)
        {
            var focusedInputs = focuses.getFocuses(VanillaTypes.ITEM_STACK, RecipeIngredientRole.INPUT).map(f -> f.getTypedValue().getIngredient())
                    .filter(stack -> stack.is(Enderthing.KEY_CHEST_ITEM))
                    .toList();
            if (focusedInputs.isEmpty())
                focusedInputs = ALL_KEY_CHESTS;

            craftingGridHelper.createAndSetOutputs(builder, OUTPUT);
            craftingGridHelper.createAndSetIngredientsFromDisplays(builder, List.of(getStackDisplays(focusedInputs)), 1, 1);
        }
    }

    private static class AddLockRecipeCategoryExtension implements ICraftingCategoryExtension<AddLockRecipe>
    {
        public static final List<ItemStack> ALL_OUTPUTS = List.of(
                KeyUtils.setPrivate(Enderthing.KEY_CHEST_ITEM.toStack(), false),
                KeyUtils.setPrivate(Enderthing.KEY_CHEST_ITEM.toStack(), true),
                KeyUtils.setBound(KeyUtils.setPrivate(Enderthing.KEY_CHEST_ITEM.toStack(), true), DUMMY_UUID)
        );
        public static final List<ItemStack> ALL_CHESTS = List.of(
                Items.ENDER_CHEST.getDefaultInstance(),
                KeyUtils.setPrivate(Enderthing.KEY_CHEST_ITEM.toStack(), false),
                KeyUtils.setPrivate(Enderthing.KEY_CHEST_ITEM.toStack(), true),
                KeyUtils.setBound(KeyUtils.setPrivate(Enderthing.KEY_CHEST_ITEM.toStack(), true), DUMMY_UUID)
        );
        public static final List<ItemStack> ALL_LOCKS = List.of(
                KeyUtils.setPrivate(Enderthing.LOCK.toStack(), false),
                KeyUtils.setPrivate(Enderthing.LOCK.toStack(), true),
                KeyUtils.setBound(KeyUtils.setPrivate(Enderthing.LOCK.toStack(), true), DUMMY_UUID)
        );

        @Override
        public List<SlotDisplay> getIngredients(RecipeHolder<AddLockRecipe> recipeHolder)
        {
            return getIngredients(ALL_OUTPUTS);
        }

        @Override
        public int getWidth(RecipeHolder<AddLockRecipe> recipeHolder)
        {
            return 2;
        }

        @Override
        public int getHeight(RecipeHolder<AddLockRecipe> recipeHolder)
        {
            return 1;
        }

        private List<SlotDisplay> getIngredients(List<ItemStack> focusedOutputs)
        {
            var inputChest = new SlotDisplay.Composite(
                    ALL_CHESTS.stream().map(stack ->
                            (SlotDisplay) new SlotDisplay.ItemStackSlotDisplay(toTemplate(stack))).toList()
            );
            var inputLock = new SlotDisplay.Composite(
                    focusedOutputs.stream().map(o -> {
                        var stack = Enderthing.LOCK.toStack();
                        if (KeyUtils.isPrivate(o)) {
                            KeyUtils.setPrivate(stack, true);
                            if (KeyUtils.isBound(o)) {
                                KeyUtils.setBound(stack, KeyUtils.getBound(o));
                            }
                        }
                        return (SlotDisplay) new SlotDisplay.ItemStackSlotDisplay(toTemplate(stack));
                    }).toList()
            );

            return List.of(inputChest, inputLock);
        }

        @Override
        public void setRecipe(RecipeHolder<AddLockRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses)
        {
            var focusedOutputs = focuses.getFocuses(VanillaTypes.ITEM_STACK, RecipeIngredientRole.OUTPUT).map(f -> f.getTypedValue().getIngredient()).toList();
            var focusedInputs = focuses.getFocuses(VanillaTypes.ITEM_STACK, RecipeIngredientRole.INPUT).map(f -> f.getTypedValue().getIngredient())
                    .filter(stack -> stack.is(Enderthing.LOCK))
                    .toList();

            List<ItemStack> outputs;
            if (!focusedOutputs.isEmpty())
                outputs = focusedOutputs;
            else if (!focusedInputs.isEmpty())
                outputs = focusedInputs.stream().map(stack -> {
                    var out = Enderthing.KEY_CHEST_ITEM.toStack();
                    if (KeyUtils.isPrivate(stack))
                    {
                        KeyUtils.setPrivate(out, true);
                        KeyUtils.setBound(out, KeyUtils.getBound(stack));
                    }
                    return out;
                }).toList();
            else
                outputs = ALL_OUTPUTS;

            craftingGridHelper.createAndSetOutputs(builder, getStackDisplays(outputs));
            craftingGridHelper.createAndSetIngredientsFromDisplays(builder, getIngredients(outputs), 2, 1);
        }

        @Override
        public void onDisplayedIngredientsUpdate(RecipeHolder<AddLockRecipe> recipeHolder, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses)
        {
            if (recipeSlots.size() < 2)
                return;

            var output = recipeSlots.get(0);
            var input = recipeSlots.get(2);

            input.getDisplayedItemStack().ifPresent(inStack -> {
                output.getDisplayedItemStack().ifPresent(outStack -> {
                    var in1 = inStack.copy();
                    if (KeyUtils.isPrivate(outStack))
                    {
                        KeyUtils.setPrivate(in1, true);
                        KeyUtils.setBound(in1, KeyUtils.getBound(outStack));
                    }
                    KeyUtils.setKey(in1, KeyUtils.getKey(outStack));
                    input.createDisplayOverrides().add(in1);
                });
            });
        }
    }

    private static class MakePrivateRecipeCategoryExtension implements ICraftingCategoryExtension<MakePrivateRecipe>
    {
        public static final List<ItemStack> ALL_INPUTS = List.of(
                Enderthing.KEY.toStack(),
                Enderthing.KEY_CHEST_ITEM.toStack(),
                Enderthing.LOCK.toStack(),
                Enderthing.PACK.toStack()
        );
        public static final List<ItemStack> ALL_OUTPUTS = List.of(
                KeyUtils.setPrivate(Enderthing.KEY.toStack(), true),
                KeyUtils.setPrivate(Enderthing.KEY_CHEST_ITEM.toStack(), true),
                KeyUtils.setPrivate(Enderthing.LOCK.toStack(), true),
                KeyUtils.setPrivate(Enderthing.PACK.toStack(), true)
        );

        @Override
        public List<SlotDisplay> getIngredients(RecipeHolder<MakePrivateRecipe> recipeHolder)
        {
            return getIngredients(ALL_INPUTS);
        }

        @Override
        public int getWidth(RecipeHolder<MakePrivateRecipe> recipeHolder)
        {
            return 3;
        }

        @Override
        public int getHeight(RecipeHolder<MakePrivateRecipe> recipeHolder)
        {
            return 3;
        }

        private List<SlotDisplay> getIngredients(List<ItemStack> inputs)
        {
            var ___empty__ = SlotDisplay.Empty.INSTANCE;
            var goldNugget = new SlotDisplay.ItemSlotDisplay(Items.GOLD_NUGGET);
            var input_item = getStackDisplays(inputs);

            return List.of(
                    ___empty__, goldNugget, ___empty__,
                    goldNugget, input_item, goldNugget,
                    ___empty__, goldNugget, ___empty__);
        }

        @Override
        public void setRecipe(RecipeHolder<MakePrivateRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses)
        {
            var focusedOutputs = focuses.getFocuses(VanillaTypes.ITEM_STACK, RecipeIngredientRole.OUTPUT).map(f -> f.getTypedValue().getIngredient())
                    .filter(stack -> ALL_OUTPUTS.stream().anyMatch(s -> s.is(stack.getItem())))
                    .toList();
            var focusedInputs = focuses.getFocuses(VanillaTypes.ITEM_STACK, RecipeIngredientRole.INPUT).map(f -> f.getTypedValue().getIngredient())
                    .filter(stack -> ALL_INPUTS.stream().anyMatch(s -> s.is(stack.getItem())))
                    .toList();

            List<ItemStack> inputs, outputs;
            if (!focusedOutputs.isEmpty())
            {
                inputs = focusedOutputs.stream().map(stack -> KeyUtils.setPrivate(stack.copy(), false)).toList();
                outputs = focusedOutputs.stream().map(stack -> KeyUtils.setPrivate(stack.copy(), true)).toList();
            }
            else if (!focusedInputs.isEmpty())
            {
                inputs = focusedInputs.stream().map(stack -> KeyUtils.setPrivate(stack.copy(), false)).toList();
                outputs = focusedInputs.stream().map(stack -> KeyUtils.setPrivate(stack.copy(), true)).toList();
            }
            else
            {
                inputs = ALL_INPUTS;
                outputs = ALL_OUTPUTS;
            }

            craftingGridHelper.createAndSetOutputs(builder, getStackDisplays(outputs));
            craftingGridHelper.createAndSetIngredientsFromDisplays(builder, getIngredients(inputs), 3, 3);
        }

        @Override
        public void onDisplayedIngredientsUpdate(RecipeHolder<MakePrivateRecipe> recipeHolder, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses)
        {
            if (recipeSlots.size() < 9)
                return;

            var focusedInputs = focuses.getFocuses(VanillaTypes.ITEM_STACK, RecipeIngredientRole.INPUT).map(f -> f.getTypedValue().getIngredient())
                    .filter(stack -> ALL_OUTPUTS.stream().anyMatch(s -> s.getItem() == stack.getItem()))
                    .toList();

            var output = recipeSlots.get(0);
            var input = recipeSlots.get(5);

            if (!focusedInputs.isEmpty()) {
                input.getDisplayedItemStack().ifPresent(inStack -> {
                    input.createDisplayOverrides().add(KeyUtils.setPrivate(inStack.copy(), false));
                    output.createDisplayOverrides().add(KeyUtils.setKey(KeyUtils.setPrivate(inStack.copy(), true), KeyUtils.getKey(inStack)));
                });
            }
            else
            {
                output.getDisplayedItemStack().ifPresent(outStack -> {
                    input.createDisplayOverrides().add(KeyUtils.setKey(KeyUtils.setPrivate(outStack.copy(), false), KeyUtils.getKey(outStack)));
                    output.createDisplayOverrides().add(KeyUtils.setPrivate(outStack.copy(), true));
                });
            }
        }
    }

    private static SlotDisplay getStackDisplays(List<ItemStack> outputs, boolean priv)
    {
        return new SlotDisplay.Composite(outputs.stream().map(stack -> {
            stack = KeyUtils.setPrivate(stack.copy(), priv);
            return (SlotDisplay) new SlotDisplay.ItemStackSlotDisplay(toTemplate(stack));
        }).toList());
    }

    private static SlotDisplay getStackDisplays(List<ItemStack> outputs)
    {
        return new SlotDisplay.Composite(outputs.stream().map(stack -> (SlotDisplay) new SlotDisplay.ItemStackSlotDisplay(toTemplate(stack))).toList());
    }
}