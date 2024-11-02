package dev.gigaherz.enderthing;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlock;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockItem;
import dev.gigaherz.enderthing.blocks.EnderKeyChestRenderer;
import dev.gigaherz.enderthing.gui.KeyContainer;
import dev.gigaherz.enderthing.gui.KeyScreen;
import dev.gigaherz.enderthing.gui.PasscodeContainer;
import dev.gigaherz.enderthing.gui.PasscodeScreen;
import dev.gigaherz.enderthing.items.EnderCardItem;
import dev.gigaherz.enderthing.items.EnderKeyItem;
import dev.gigaherz.enderthing.items.EnderLockItem;
import dev.gigaherz.enderthing.items.EnderPackItem;
import dev.gigaherz.enderthing.network.SetItemKey;
import dev.gigaherz.enderthing.recipes.AddLockRecipe;
import dev.gigaherz.enderthing.recipes.MakeBoundRecipe;
import dev.gigaherz.enderthing.recipes.MakePrivateRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.data.recipes.*;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Mod(Enderthing.MODID)
public class Enderthing
{
    public static final String MODID = "enderthing";

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, MODID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<EnderKeyChestBlock>
            KEY_CHEST = BLOCKS.registerBlock("key_chest", EnderKeyChestBlock::new, Block.Properties.ofFullCopy(Blocks.ENDER_CHEST));

    public static final DeferredItem<EnderKeyChestBlockItem>
            KEY_CHEST_ITEM = ITEMS.registerItem("key_chest", props -> new EnderKeyChestBlockItem(KEY_CHEST.get(), props.useBlockDescriptionPrefix()));
    public static final DeferredItem<EnderKeyItem>
            KEY   = ITEMS.registerItem("key", EnderKeyItem::new);
    public static final DeferredItem<EnderLockItem>
            LOCK = ITEMS.registerItem("lock", EnderLockItem::new);
    public static final DeferredItem<EnderPackItem>
            PACK = ITEMS.registerItem("pack", props -> new EnderPackItem(props.stacksTo(1)));
    public static final DeferredItem<EnderCardItem>
            CARD = ITEMS.registerItem("card", props -> new EnderCardItem(props.stacksTo(1)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnderKeyChestBlockEntity>>
            KEY_CHEST_BLOCK_ENTITY = BLOCK_ENTITIES.register("key_chest", () -> new BlockEntityType<>(EnderKeyChestBlockEntity::new, KEY_CHEST.get()));

    public static final DeferredHolder<MenuType<?>, MenuType<KeyContainer>>
            KEY_CONTAINER = MENU_TYPES.register("key", () -> IMenuTypeExtension.create(KeyContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<PasscodeContainer>>
            PASSCODE_CONTAINER = MENU_TYPES.register("passcode", () -> IMenuTypeExtension.create(PasscodeContainer::new));

    public static final DeferredHolder<RecipeSerializer<?>, CustomRecipe.Serializer<MakePrivateRecipe>>
            MAKE_PRIVATE = RECIPE_SERIALIZERS.register("make_private", () -> new CustomRecipe.Serializer<>(MakePrivateRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, CustomRecipe.Serializer<AddLockRecipe>>
            ADD_LOCK = RECIPE_SERIALIZERS.register("add_lock", () -> new CustomRecipe.Serializer<>(AddLockRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, CustomRecipe.Serializer<MakeBoundRecipe>>
            MAKE_BOUND = RECIPE_SERIALIZERS.register("make_bound", () -> new CustomRecipe.Serializer<>(MakeBoundRecipe::new));

    public static DeferredHolder<CreativeModeTab, CreativeModeTab> ENDERTHING_GROUP =
            CREATIVE_TABS.register("enderthing_things", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                    .icon(() -> new ItemStack(KEY.get()))
                    .title(Component.translatable("tab.enderthing.things"))
                    .displayItems((featureFlags, output) -> {
                        KEY_CHEST_ITEM.get().fillItemCategory(output);
                        KEY.get().fillItemCategory(output);
                        LOCK.get().fillItemCategory(output);
                        PACK.get().fillItemCategory(output);
                        output.accept(CARD.get());
                    }).build());

    public Enderthing(IEventBus modEventBus)
    {
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        KeyUtils.init(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::gatherData);
    }

    private void commonSetup(RegisterPayloadHandlersEvent event)
    {
        final PayloadRegistrar registrar = event.registrar(MODID).versioned("1.0");
        registrar.playToServer(SetItemKey.TYPE, SetItemKey.STREAM_CODEC, SetItemKey::handle);
    }

    public static ResourceLocation location(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public void gatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();

        gen.addProvider(event.includeServer(), new Recipes(gen.getPackOutput(), event.getLookupProvider()));
        gen.addProvider(event.includeServer(), Loot.create(gen.getPackOutput(), event.getLookupProvider()));

        var existingFileHelper = event.getExistingFileHelper();
        var blockTags = new BlockTagGens(gen, existingFileHelper);
        var itemTags = new ItemTagGens(gen, blockTags.contentsGetter(), existingFileHelper);
        gen.addProvider(event.includeServer(), blockTags);
        gen.addProvider(event.includeServer(), itemTags);
    }

    private static class ItemTagGens extends ItemTagsProvider implements DataProvider
    {
        public ItemTagGens(DataGenerator gen, CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper existingFileHelper)
        {
            super(gen.getPackOutput(), CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor()),
                    blockTags, MODID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider p_256380_)
        {
            tag(Tags.Items.CHESTS_ENDER)
                    .add(Enderthing.KEY_CHEST_ITEM.get());
            tag(KeyUtils.CAN_MAKE_BOUND)
                    .add(Enderthing.KEY_CHEST_ITEM.get())
                    .add(Enderthing.LOCK.get());
            tag(KeyUtils.CAN_MAKE_PRIVATE)
                    .add(Enderthing.KEY_CHEST_ITEM.get())
                    .add(Enderthing.LOCK.get())
                    .add(Enderthing.PACK.get())
                    .add(Enderthing.KEY.get());
        }
    }

    private static class BlockTagGens extends BlockTagsProvider implements DataProvider
    {
        public BlockTagGens(DataGenerator gen, ExistingFileHelper existingFileHelper)
        {
            super(gen.getPackOutput(), CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor()),
                    MODID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider p_256380_)
        {
            tag(Tags.Blocks.CHESTS_ENDER)
                    .add(Enderthing.KEY_CHEST.get());
            tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .add(Enderthing.KEY_CHEST.get());
        }
    }

    private static class Loot
    {
        public static LootTableProvider create(PackOutput gen, CompletableFuture<HolderLookup.Provider> lookup)
        {
            return new LootTableProvider(gen, Set.of(), List.of(
                    new LootTableProvider.SubProviderEntry(Loot.BlockTables::new, LootContextParamSets.BLOCK)
            ), lookup);
        }

        public static class BlockTables extends VanillaBlockLoot
        {
            public BlockTables(HolderLookup.Provider provider)
            {
                super(provider);
            }

            @Override
            protected void generate()
            {
                this.add(KEY_CHEST.get(), this::droppingWithContents);
            }

            protected LootTable.Builder dropping(Block block, LootItemCondition.Builder condition, LootPoolEntryContainer.Builder<?> alternativeEntry)
            {
                return LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem
                                        .lootTableItem(block).when(condition)
                                        .otherwise(alternativeEntry)));
            }

            protected LootTable.Builder droppingWithContents(Block block)
            {
                return LootTable.lootTable()
                        .withPool(applyExplosionCondition(block,
                                        LootPool.lootPool()
                                                .setRolls(ConstantValue.exactly(1))
                                                .add(LootItem.lootTableItem(block)
                                                        .when(hasSilkTouch())
                                                        .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                                                        .apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                                                                .include(KeyUtils.KEY.get())
                                                                .include(KeyUtils.IS_PRIVATE.get())
                                                                .include(KeyUtils.BINDING.get())
                                                        )
                                                        .otherwise(LootItem.lootTableItem(LOCK.get())
                                                                .apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                                                                        .include(KeyUtils.KEY.get())
                                                                        .include(KeyUtils.IS_PRIVATE.get())
                                                                        .include(KeyUtils.BINDING.get())
                                                                )
                                                        )
                                                )
                                )
                        ).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                                .add(withNoSilkTouchRandomly(block, Items.OBSIDIAN, ConstantValue.exactly(8)))
                        );
            }

            protected LootPoolEntryContainer.Builder<?> withNoSilkTouchRandomly(Block block, ItemLike item, NumberProvider range)
            {
                return applyExplosionDecay(block, LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(range))).when(hasSilkTouch().invert());
            }


            @Override
            protected Iterable<Block> getKnownBlocks()
            {
                return BuiltInRegistries.BLOCK.entrySet().stream()
                        .filter(e -> e.getKey().location().getNamespace().equals(MODID))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());
            }
        }
    }

    private static class Recipes extends RecipeProvider.Runner
    {
        public Recipes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider)
        {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider lookup, RecipeOutput output)
        {
            return new VanillaRecipeProvider(lookup, output)
            {
                @Override
                protected void buildRecipes()
                {
                    shaped(RecipeCategory.MISC, Enderthing.KEY.get())
                            .pattern("o  ")
                            .pattern("eog")
                            .pattern("nnn")
                            .define('o', Tags.Items.OBSIDIANS_NORMAL)
                            .define('g', Tags.Items.INGOTS_GOLD)
                            .define('e', Items.ENDER_EYE)
                            .define('n', Tags.Items.NUGGETS_IRON)
                            .unlockedBy("has_gold", has(Items.ENDER_EYE))
                            .save(output);

                    shaped(RecipeCategory.MISC, Enderthing.LOCK.get())
                            .pattern(" g ")
                            .pattern("geg")
                            .pattern("nnn")
                            .define('g', Tags.Items.INGOTS_GOLD)
                            .define('e', Items.ENDER_EYE)
                            .define('n', Tags.Items.NUGGETS_IRON)
                            .unlockedBy("has_gold", has(Items.ENDER_EYE))
                            .save(output);

                    shaped(RecipeCategory.MISC, Enderthing.PACK.get())
                            .pattern("lel")
                            .pattern("nnn")
                            .pattern("lcl")
                            .define('l', Tags.Items.LEATHERS)
                            .define('c', Blocks.ENDER_CHEST)
                            .define('e', Items.ENDER_EYE)
                            .define('n', Tags.Items.NUGGETS_IRON)
                            .unlockedBy("has_gold", has(Items.ENDER_EYE))
                            .save(output);

                    shaped(RecipeCategory.MISC, Enderthing.CARD.get())
                            .pattern("nnn")
                            .pattern("ppp")
                            .pattern("nnn")
                            .define('n', Tags.Items.NUGGETS_GOLD)
                            .define('p', Items.PAPER)
                            .unlockedBy("has_gold", has(Items.ENDER_EYE))
                            .save(output);

                    SpecialRecipeBuilder.special(MakePrivateRecipe::new).save(output, "enderthing:make_private");
                    SpecialRecipeBuilder.special(AddLockRecipe::new).save(output, "enderthing:add_lock");
                    SpecialRecipeBuilder.special(MakeBoundRecipe::new).save(output, "enderthing:make_bound");
                }
            };
        }

        @Override
        public String getName()
        {
            return "Enderthing Recipes";
        }
    }
}

