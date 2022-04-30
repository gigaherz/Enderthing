package dev.gigaherz.enderthing;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlock;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockItem;
import dev.gigaherz.enderthing.gui.KeyContainer;
import dev.gigaherz.enderthing.gui.KeyScreen;
import dev.gigaherz.enderthing.gui.PasscodeContainer;
import dev.gigaherz.enderthing.gui.PasscodeScreen;
import dev.gigaherz.enderthing.items.EnderKeyItem;
import dev.gigaherz.enderthing.network.SetItemKey;
import dev.gigaherz.enderthing.recipes.AddLockRecipe;
import dev.gigaherz.enderthing.recipes.MakeBoundRecipe;
import dev.gigaherz.enderthing.recipes.MakePrivateRecipe;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import dev.gigaherz.enderthing.items.EnderCardItem;
import dev.gigaherz.enderthing.items.EnderLockItem;
import dev.gigaherz.enderthing.items.EnderPackItem;
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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
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
    public static final String MODID = "enderthing";

    @ObjectHolder("enderthing:key_chest")
    public static EnderKeyChestBlock KEY_CHEST;

    @ObjectHolder("enderthing:key_chest")
    public static EnderKeyChestBlockItem KEY_CHEST_ITEM;

    @ObjectHolder("enderthing:key")
    public static EnderKeyItem KEY;

    @ObjectHolder("enderthing:lock")
    public static EnderLockItem LOCK;

    @ObjectHolder("enderthing:pack")
    public static EnderPackItem PACK;

    @ObjectHolder("enderthing:card")
    public static EnderCardItem CARD;

    public static Enderthing INSTANCE;

    public static CreativeModeTab ENDERTHING_GROUP = new CreativeModeTab("enderthing.things")
    {
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(KEY);
        }
    };

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MODID, "main"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public Enderthing()
    {
        INSTANCE = this;

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addGenericListener(Block.class, this::registerBlocks);
        modEventBus.addGenericListener(Item.class, this::registerItems);
        modEventBus.addGenericListener(BlockEntityType.class, this::registerTileEntities);
        modEventBus.addGenericListener(MenuType.class, this::registerContainers);
        modEventBus.addGenericListener(RecipeSerializer.class, this::registerRecipeSerializers);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::gatherData);
    }

    public void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(
                new EnderKeyChestBlock(Block.Properties.copy(Blocks.ENDER_CHEST)).setRegistryName("key_chest")
        );
    }

    public void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                new EnderKeyChestBlockItem(KEY_CHEST, new Item.Properties().tab(ENDERTHING_GROUP))
                        .setRegistryName(KEY_CHEST.getRegistryName()),
                new EnderKeyItem(new Item.Properties().tab(ENDERTHING_GROUP)).setRegistryName("key"),
                new EnderLockItem(new Item.Properties().tab(ENDERTHING_GROUP)).setRegistryName("lock"),
                new EnderPackItem(new Item.Properties().stacksTo(1).tab(ENDERTHING_GROUP)).setRegistryName("pack"),
                new EnderCardItem(new Item.Properties().stacksTo(1).tab(ENDERTHING_GROUP)).setRegistryName("card")
        );
    }

    private void registerTileEntities(RegistryEvent.Register<BlockEntityType<?>> event)
    {
        event.getRegistry().registerAll(
                BlockEntityType.Builder.of(EnderKeyChestBlockEntity::new, KEY_CHEST).build(null).setRegistryName("key_chest")
        );
    }

    private void registerContainers(RegistryEvent.Register<MenuType<?>> event)
    {
        event.getRegistry().registerAll(
                IForgeMenuType.create(KeyContainer::new).setRegistryName("key"),
                IForgeMenuType.create(PasscodeContainer::new).setRegistryName("passcode")
        );
    }

    private void registerRecipeSerializers(RegistryEvent.Register<RecipeSerializer<?>> event)
    {
        event.getRegistry().registerAll(
                new SimpleRecipeSerializer<>(MakePrivateRecipe::new).setRegistryName("make_private"),
                new SimpleRecipeSerializer<>(AddLockRecipe::new).setRegistryName("add_lock"),
                new SimpleRecipeSerializer<>(MakeBoundRecipe::new).setRegistryName("make_bound")
        );
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        int messageNumber = 0;
        CHANNEL.messageBuilder(SetItemKey.class, messageNumber++, NetworkDirection.PLAY_TO_SERVER).encoder(SetItemKey::encode).decoder(SetItemKey::new).consumer(SetItemKey::handle).add();
        LOGGER.debug("Final message number: " + messageNumber);
    }

    private void clientSetup(FMLClientSetupEvent event)
    {
        MenuScreens.register(KeyContainer.TYPE, KeyScreen::new);
        MenuScreens.register(PasscodeContainer.TYPE, PasscodeScreen::new);

        ItemProperties.register(KEY, new ResourceLocation("private"), (stack, world, entity, i) -> KeyUtils.isPrivate(stack) ? 1.0f : 0.0f);
        ItemProperties.register(LOCK, new ResourceLocation("private"), (stack, world, entity, i) -> KeyUtils.isPrivate(stack) ? 1.0f : 0.0f);
        ItemProperties.register(PACK, new ResourceLocation("private"), (stack, world, entity, i) -> KeyUtils.isPrivate(stack) ? 1.0f : 0.0f);
        ItemProperties.register(KEY_CHEST_ITEM, new ResourceLocation("private"), (stack, world, entity, i) -> KeyUtils.isPrivate(stack) ? 1.0f : 0.0f);

        ItemProperties.register(LOCK, new ResourceLocation("bound"), (stack, world, entity, i) -> KeyUtils.isPrivate(stack) && KeyUtils.isBound(stack) ? 1.0f : 0.0f);
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
                tooltip.add(new TranslatableComponent("tooltip.enderthing.private").withStyle(ChatFormatting.ITALIC, ChatFormatting.BOLD));
            }

            long key = KeyUtils.getKey(stack);
            if (key >= 0)
            {
                tooltip.add(new TranslatableComponent("tooltip.enderthing.key", key).withStyle(ChatFormatting.ITALIC));
            }
            else
            {
                tooltip.add(new TranslatableComponent("tooltip.enderthing.key_missing").withStyle(ChatFormatting.ITALIC));
            }
        }
    }

    public void gatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();

        if (event.includeServer())
        {
            gen.addProvider(new Recipes(gen));
            gen.addProvider(new Loot(gen));

            var existingFileHelper = event.getExistingFileHelper();
            var blockTags = new BlockTagGens(gen, existingFileHelper);
            var itemTags = new ItemTagGens(gen, blockTags, existingFileHelper);
            gen.addProvider(blockTags);
            gen.addProvider(itemTags);

        }
        if (event.includeClient())
        {
            //gen.addProvider(new BlockStates(gen, event));
        }
    }

    private static class ItemTagGens extends ItemTagsProvider implements DataProvider
    {
        public ItemTagGens(DataGenerator gen, BlockTagsProvider blockTags, ExistingFileHelper existingFileHelper)
        {
            super(gen, blockTags, MODID, existingFileHelper);
        }

        @Nullable
        public Tag.Builder getTagByName(ResourceLocation tag)
        {
            return this.builders.get(tag);
        }

        @Override
        protected void addTags()
        {
            tag(Tags.Items.CHESTS_ENDER)
                    .add(Enderthing.KEY_CHEST_ITEM);
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
                    .add(Enderthing.KEY_CHEST);
            tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .add(Enderthing.KEY_CHEST);
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
                this.add(KEY_CHEST, BlockTables::droppingWithContents);
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
                                                .otherwise(LootItem.lootTableItem(LOCK)
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
                return ForgeRegistries.BLOCKS.getValues().stream()
                        .filter(b -> b.getRegistryName().getNamespace().equals(MODID))
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
            ShapedRecipeBuilder.shaped(Enderthing.KEY)
                    .pattern("o  ")
                    .pattern("eog")
                    .pattern("nnn")
                    .define('o', Blocks.OBSIDIAN)
                    .define('g', Items.GOLD_INGOT)
                    .define('e', Items.ENDER_EYE)
                    .define('n', Items.IRON_NUGGET)
                    .unlockedBy("has_gold", has(Items.ENDER_EYE))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(Enderthing.LOCK)
                    .pattern(" g ")
                    .pattern("geg")
                    .pattern("nnn")
                    .define('g', Items.GOLD_INGOT)
                    .define('e', Items.ENDER_EYE)
                    .define('n', Items.IRON_NUGGET)
                    .unlockedBy("has_gold", has(Items.ENDER_EYE))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(Enderthing.PACK)
                    .pattern("lel")
                    .pattern("nnn")
                    .pattern("lcl")
                    .define('l', Items.LEATHER)
                    .define('c', Blocks.ENDER_CHEST)
                    .define('e', Items.ENDER_EYE)
                    .define('n', Items.IRON_NUGGET)
                    .unlockedBy("has_gold", has(Items.ENDER_EYE))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(Enderthing.CARD)
                    .pattern("nnn")
                    .pattern("ppp")
                    .pattern("nnn")
                    .define('n', Items.GOLD_NUGGET)
                    .define('p', Items.PAPER)
                    .unlockedBy("has_gold", has(Items.ENDER_EYE))
                    .save(consumer);

            SpecialRecipeBuilder.special(MakePrivateRecipe.SERIALIZER).save(consumer, "enderthing:make_private");
            SpecialRecipeBuilder.special(AddLockRecipe.SERIALIZER).save(consumer, "enderthing:add_lock");
            SpecialRecipeBuilder.special(MakeBoundRecipe.SERIALIZER).save(consumer, "enderthing:make_bound");
        }
    }
}

