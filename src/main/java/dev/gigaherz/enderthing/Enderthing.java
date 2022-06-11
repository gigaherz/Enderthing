package dev.gigaherz.enderthing;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlock;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockItem;
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
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
@Mod(Enderthing.MODID)
public class Enderthing
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String MODID = "enderthing";

    public static CreativeModeTab ENDERTHING_GROUP = new CreativeModeTab("enderthing.things")
    {
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(Enderthing.KEY.get());
        }
    };

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);

    public static final RegistryObject<EnderKeyChestBlock> KEY_CHEST = BLOCKS.register("key_chest", () -> new EnderKeyChestBlock(Block.Properties.copy(Blocks.ENDER_CHEST)));
    public static final RegistryObject<EnderKeyChestBlockItem> KEY_CHEST_ITEM = ITEMS.register("key_chest", () -> new EnderKeyChestBlockItem(KEY_CHEST.get(), new Item.Properties().tab(ENDERTHING_GROUP)));
    public static final RegistryObject<EnderKeyItem> KEY = ITEMS.register("key", () -> new EnderKeyItem(new Item.Properties().tab(ENDERTHING_GROUP)));
    public static final RegistryObject<EnderLockItem> LOCK = ITEMS.register("lock", () -> new EnderLockItem(new Item.Properties().tab(ENDERTHING_GROUP)));
    public static final RegistryObject<EnderPackItem> PACK = ITEMS.register("pad", () -> new EnderPackItem(new Item.Properties().stacksTo(1).tab(ENDERTHING_GROUP)));
    public static final RegistryObject<EnderCardItem> CARD = ITEMS.register("card", () -> new EnderCardItem(new Item.Properties().stacksTo(1).tab(ENDERTHING_GROUP)));

    public static final RegistryObject<BlockEntityType<EnderKeyChestBlockEntity>>
            KEY_CHEST_BLOCK_ENTITY = BLOCK_ENTITIES.register("key_chest", () -> BlockEntityType.Builder.of(EnderKeyChestBlockEntity::new, KEY_CHEST.get()).build(null));

    public static final RegistryObject<MenuType<KeyContainer>> KEY_CONTAINER = MENU_TYPES.register("key", () -> IForgeMenuType.create(KeyContainer::new));
    public static final RegistryObject<MenuType<PasscodeContainer>> PASSCODE_CONTAINER = MENU_TYPES.register("passcode", () -> IForgeMenuType.create(PasscodeContainer::new));

    public static final RegistryObject<SimpleRecipeSerializer<?>> MAKE_PRIVATE = RECIPE_SERIALIZERS.register("make_private", () ->   new SimpleRecipeSerializer<>(MakePrivateRecipe::new));
    public static final RegistryObject<SimpleRecipeSerializer<?>> ADD_LOCK = RECIPE_SERIALIZERS.register("add_lock", () ->   new SimpleRecipeSerializer<>(AddLockRecipe::new));
    public static final RegistryObject<SimpleRecipeSerializer<?>> MAKE_BOUND = RECIPE_SERIALIZERS.register("make_bound", () ->  new SimpleRecipeSerializer<>(MakeBoundRecipe::new));

    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MODID, "main"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public Enderthing()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        MENU_TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::gatherData);
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        int messageNumber = 0;
        CHANNEL.messageBuilder(SetItemKey.class, messageNumber++, NetworkDirection.PLAY_TO_SERVER).encoder(SetItemKey::encode).decoder(SetItemKey::new).consumer(SetItemKey::handle).add();
        LOGGER.debug("Final message number: " + messageNumber);
    }

    private void clientSetup(FMLClientSetupEvent event)
    {
        MenuScreens.register(Enderthing.KEY_CONTAINER.get(), KeyScreen::new);
        MenuScreens.register(Enderthing.PASSCODE_CONTAINER.get(), PasscodeScreen::new);

        ItemProperties.register(KEY.get(), new ResourceLocation("private"), (stack, world, entity, i) -> KeyUtils.isPrivate(stack) ? 1.0f : 0.0f);
        ItemProperties.register(LOCK.get(), new ResourceLocation("private"), (stack, world, entity, i) -> KeyUtils.isPrivate(stack) ? 1.0f : 0.0f);
        ItemProperties.register(PACK.get(), new ResourceLocation("private"), (stack, world, entity, i) -> KeyUtils.isPrivate(stack) ? 1.0f : 0.0f);
        ItemProperties.register(KEY_CHEST_ITEM.get(), new ResourceLocation("private"), (stack, world, entity, i) -> KeyUtils.isPrivate(stack) ? 1.0f : 0.0f);

        ItemProperties.register(LOCK.get(), new ResourceLocation("bound"), (stack, world, entity, i) -> KeyUtils.isPrivate(stack) && KeyUtils.isBound(stack) ? 1.0f : 0.0f);
    }

    public static ResourceLocation location(String path)
    {
        return new ResourceLocation(MODID, path);
    }

    public static class Client
    {
        public static void addStandardInformation(ItemStack stack, List<Component> tooltip)
        {
            if (KeyUtils.isPrivate(stack))
            {
                tooltip.add(Component.translatable("tooltip.enderthing.private").withStyle(ChatFormatting.ITALIC, ChatFormatting.BOLD));
            }

            long key = KeyUtils.getKey(stack);
            if (key >= 0)
            {
                tooltip.add(Component.translatable("tooltip.enderthing.key", key).withStyle(ChatFormatting.ITALIC));
            }
            else
            {
                tooltip.add(Component.translatable("tooltip.enderthing.key_missing").withStyle(ChatFormatting.ITALIC));
            }
        }
    }

    public void gatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();

        gen.addProvider(event.includeServer(), new Recipes(gen));
        gen.addProvider(event.includeServer(), new Loot(gen));

        var existingFileHelper = event.getExistingFileHelper();
        var blockTags = new BlockTagGens(gen, existingFileHelper);
        var itemTags = new ItemTagGens(gen, blockTags, existingFileHelper);
        gen.addProvider(event.includeServer(), blockTags);
        gen.addProvider(event.includeServer(), itemTags);

    }

    private static class ItemTagGens extends ItemTagsProvider implements DataProvider
    {
        public ItemTagGens(DataGenerator gen, BlockTagsProvider blockTags, ExistingFileHelper existingFileHelper)
        {
            super(gen, blockTags, MODID, existingFileHelper);
        }

        @Override
        protected void addTags()
        {
            tag(Tags.Items.CHESTS_ENDER)
                    .add(Enderthing.KEY_CHEST_ITEM.get());
        }
    }

    private static class BlockTagGens extends BlockTagsProvider implements DataProvider
    {
        public BlockTagGens(DataGenerator gen, ExistingFileHelper existingFileHelper)
        {
            super(gen, MODID, existingFileHelper);
        }

        @Override
        protected void addTags()
        {
            tag(Tags.Blocks.CHESTS_ENDER)
                    .add(Enderthing.KEY_CHEST.get());
            tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .add(Enderthing.KEY_CHEST.get());
        }
    }

    private static class Loot extends LootTableProvider implements DataProvider
    {
        public Loot(DataGenerator gen)
        {
            super(gen);
        }

        private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> tables = ImmutableList.of(
                Pair.of(BlockTables::new, LootContextParamSets.BLOCK)
                //Pair.of(FishingLootTables::new, LootParameterSets.FISHING),
                //Pair.of(ChestLootTables::new, LootParameterSets.CHEST),
                //Pair.of(EntityLootTables::new, LootParameterSets.ENTITY),
                //Pair.of(GiftLootTables::new, LootParameterSets.GIFT)
        );

        @Override
        protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables()
        {
            return tables;
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker)
        {
            map.forEach((p_218436_2_, p_218436_3_) -> {
                LootTables.validate(validationtracker, p_218436_2_, p_218436_3_);
            });
        }

        public static class BlockTables extends BlockLoot
        {
            @SuppressWarnings("ConstantConditions")
            @Override
            protected void addTables()
            {
                this.add(KEY_CHEST.get(), BlockTables::droppingWithContents);
            }

            protected static LootTable.Builder dropping(Block block, LootItemCondition.Builder condition, LootPoolEntryContainer.Builder<?> alternativeEntry)
            {
                return LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem
                                        .lootTableItem(block).when(condition)
                                        .otherwise(alternativeEntry)));
            }

            protected static LootTable.Builder droppingWithContents(Block block)
            {
                return LootTable.lootTable()
                        .withPool(applyExplosionCondition(block,
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1))
                                        .add(LootItem.lootTableItem(block)
                                                .when(HAS_SILK_TOUCH)
                                                .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                                                .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                                        .copy("Key", "BlockEntityTag.Key")
                                                        .copy("IsPrivate", "BlockEntityTag.IsPrivate")
                                                        .copy("Bound", "BlockEntityTag.Bound")
                                                )
                                                .otherwise(LootItem.lootTableItem(LOCK.get())
                                                        .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                                                .copy("Key", "Key")
                                                                .copy("IsPrivate", "IsPrivate")
                                                                .copy("Bound", "Bound")
                                                        ))
                                        )
                                )
                        ).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                                .add(withNoSilkTouchRandomly(block, Items.OBSIDIAN, ConstantValue.exactly(8)))
                        );
            }

            protected static LootPoolEntryContainer.Builder<?> withNoSilkTouchRandomly(Block block, ItemLike item, NumberProvider range)
            {
                return applyExplosionDecay(block, LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(range))).when(HAS_NO_SILK_TOUCH);
            }


            @Override
            protected Iterable<Block> getKnownBlocks()
            {
                return ForgeRegistries.BLOCKS.getEntries().stream()
                        .filter(e -> e.getKey().location().getNamespace().equals(MODID))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());
            }
        }
    }

    private static class Recipes extends RecipeProvider
    {
        public Recipes(DataGenerator gen)
        {
            super(gen);
        }

        @Override
        protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer)
        {
            ShapedRecipeBuilder.shaped(Enderthing.KEY.get())
                    .pattern("o  ")
                    .pattern("eog")
                    .pattern("nnn")
                    .define('o', Blocks.OBSIDIAN)
                    .define('g', Items.GOLD_INGOT)
                    .define('e', Items.ENDER_EYE)
                    .define('n', Items.IRON_NUGGET)
                    .unlockedBy("has_gold", has(Items.ENDER_EYE))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(Enderthing.LOCK.get())
                    .pattern(" g ")
                    .pattern("geg")
                    .pattern("nnn")
                    .define('g', Items.GOLD_INGOT)
                    .define('e', Items.ENDER_EYE)
                    .define('n', Items.IRON_NUGGET)
                    .unlockedBy("has_gold", has(Items.ENDER_EYE))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(Enderthing.PACK.get())
                    .pattern("lel")
                    .pattern("nnn")
                    .pattern("lcl")
                    .define('l', Items.LEATHER)
                    .define('c', Blocks.ENDER_CHEST)
                    .define('e', Items.ENDER_EYE)
                    .define('n', Items.IRON_NUGGET)
                    .unlockedBy("has_gold", has(Items.ENDER_EYE))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(Enderthing.CARD.get())
                    .pattern("nnn")
                    .pattern("ppp")
                    .pattern("nnn")
                    .define('n', Items.GOLD_NUGGET)
                    .define('p', Items.PAPER)
                    .unlockedBy("has_gold", has(Items.ENDER_EYE))
                    .save(consumer);

            SpecialRecipeBuilder.special(MAKE_PRIVATE.get()).save(consumer, "enderthing:make_private");
            SpecialRecipeBuilder.special(ADD_LOCK.get()).save(consumer, "enderthing:add_lock");
            SpecialRecipeBuilder.special(MAKE_BOUND.get()).save(consumer, "enderthing:make_bound");
        }
    }
}

