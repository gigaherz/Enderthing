package gigaherz.enderthing;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import gigaherz.enderthing.blocks.EnderKeyChestBlock;
import gigaherz.enderthing.blocks.EnderKeyChestBlockItem;
import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import gigaherz.enderthing.client.ClientEvents;
import gigaherz.enderthing.gui.KeyContainer;
import gigaherz.enderthing.gui.KeyScreen;
import gigaherz.enderthing.gui.PasscodeContainer;
import gigaherz.enderthing.gui.PasscodeScreen;
import gigaherz.enderthing.items.EnderCardItem;
import gigaherz.enderthing.items.EnderKeyItem;
import gigaherz.enderthing.items.EnderLockItem;
import gigaherz.enderthing.items.EnderPackItem;
import gigaherz.enderthing.network.SetItemKey;
import gigaherz.enderthing.recipes.AddLockRecipe;
import gigaherz.enderthing.recipes.MakeBoundRecipe;
import gigaherz.enderthing.recipes.MakePrivateRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.data.*;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.CopyName;
import net.minecraft.loot.functions.CopyNbt;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public static ItemGroup ENDERTHING_GROUP = new ItemGroup("enderthing.things")
    {
        @Override
        public ItemStack createIcon()
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
        modEventBus.addGenericListener(TileEntityType.class, this::registerTileEntities);
        modEventBus.addGenericListener(ContainerType.class, this::registerContainers);
        modEventBus.addGenericListener(IRecipeSerializer.class, this::registerRecipeSerializers);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::gatherData);
    }

    public void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(
                new EnderKeyChestBlock(Block.Properties.from(Blocks.ENDER_CHEST)).setRegistryName("key_chest")
        );
    }

    public void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                new EnderKeyChestBlockItem(KEY_CHEST, new Item.Properties())
                        .setRegistryName(KEY_CHEST.getRegistryName()),
                new EnderKeyItem(new Item.Properties().group(ENDERTHING_GROUP)).setRegistryName("key"),
                new EnderLockItem(new Item.Properties().group(ENDERTHING_GROUP)).setRegistryName("lock"),
                new EnderPackItem(new Item.Properties().maxStackSize(1).group(ENDERTHING_GROUP)).setRegistryName("pack"),
                new EnderCardItem(new Item.Properties().maxStackSize(1).group(ENDERTHING_GROUP)).setRegistryName("card")
        );
    }

    private void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event)
    {
        event.getRegistry().registerAll(
                TileEntityType.Builder.create(EnderKeyChestTileEntity::new, KEY_CHEST).build(null).setRegistryName("key_chest")
        );
    }

    private void registerContainers(RegistryEvent.Register<ContainerType<?>> event)
    {
        event.getRegistry().registerAll(
                IForgeContainerType.create(KeyContainer::new).setRegistryName("key"),
                IForgeContainerType.create(PasscodeContainer::new).setRegistryName("passcode")
        );
    }

    private void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        event.getRegistry().registerAll(
                new SpecialRecipeSerializer<>(MakePrivateRecipe::new).setRegistryName("make_private"),
                new SpecialRecipeSerializer<>(AddLockRecipe::new).setRegistryName("add_lock"),
                new SpecialRecipeSerializer<>(MakeBoundRecipe::new).setRegistryName("make_bound")
        );
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        int messageNumber = 0;
        CHANNEL.messageBuilder(SetItemKey.class, messageNumber++).encoder(SetItemKey::encode).decoder(SetItemKey::new).consumer(SetItemKey::handle).add();
        LOGGER.debug("Final message number: " + messageNumber);
    }

    private void clientSetup(FMLClientSetupEvent event)
    {
        ScreenManager.registerFactory(KeyContainer.TYPE, KeyScreen::new);
        ScreenManager.registerFactory(PasscodeContainer.TYPE, PasscodeScreen::new);

        ItemModelsProperties.func_239418_a_(KEY, new ResourceLocation("private"), (stack, world, entity) -> KeyUtils.isPrivate(stack) ? 1.0f : 0.0f);
        ItemModelsProperties.func_239418_a_(LOCK, new ResourceLocation("private"), (stack, world, entity) -> KeyUtils.isPrivate(stack) ? 1.0f : 0.0f);
        ItemModelsProperties.func_239418_a_(PACK, new ResourceLocation("private"), (stack, world, entity) -> KeyUtils.isPrivate(stack) ? 1.0f : 0.0f);
        ItemModelsProperties.func_239418_a_(KEY_CHEST_ITEM, new ResourceLocation("private"), (stack, world, entity) -> KeyUtils.isPrivate(stack) ? 1.0f : 0.0f);

        ItemModelsProperties.func_239418_a_(LOCK, new ResourceLocation("bound"), (stack, world, entity) -> KeyUtils.isPrivate(stack) && KeyUtils.isBound(stack) ? 1.0f : 0.0f);
    }

    public static ResourceLocation location(String path)
    {
        return new ResourceLocation(MODID, path);
    }

    public static class Client
    {
        public static void addStandardInformation(ItemStack stack, List<ITextComponent> tooltip)
        {
            if (KeyUtils.isPrivate(stack))
            {
                tooltip.add(new TranslationTextComponent("tooltip.enderthing.private").mergeStyle(TextFormatting.ITALIC, TextFormatting.BOLD));
            }

            long key = KeyUtils.getKey(stack);
            if (key >= 0)
            {
                tooltip.add(new TranslationTextComponent("tooltip.enderthing.key", key).mergeStyle(TextFormatting.ITALIC));
            }
            else
            {
                tooltip.add(new TranslationTextComponent("tooltip.enderthing.key_missing").mergeStyle(TextFormatting.ITALIC));
            }
        }

    }

    public void gatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();

        if (event.includeServer())
        {
            gen.addProvider(new Recipes(gen));
            gen.addProvider(new LootTables(gen));
            //gen.addProvider(new ItemTagGens(gen));
            //gen.addProvider(new BlockTagGens(gen));
        }
        if (event.includeClient())
        {
            //gen.addProvider(new BlockStates(gen, event));
        }
    }

    private static class LootTables extends LootTableProvider implements IDataProvider
    {
        public LootTables(DataGenerator gen)
        {
            super(gen);
        }

        private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> tables = ImmutableList.of(
                Pair.of(BlockTables::new, LootParameterSets.BLOCK)
                //Pair.of(FishingLootTables::new, LootParameterSets.FISHING),
                //Pair.of(ChestLootTables::new, LootParameterSets.CHEST),
                //Pair.of(EntityLootTables::new, LootParameterSets.ENTITY),
                //Pair.of(GiftLootTables::new, LootParameterSets.GIFT)
        );

        @Override
        protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables()
        {
            return tables;
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker)
        {
            map.forEach((p_218436_2_, p_218436_3_) -> {
                LootTableManager.func_227508_a_(validationtracker, p_218436_2_, p_218436_3_);
            });
        }

        public static class BlockTables extends BlockLootTables
        {
            @SuppressWarnings("ConstantConditions")
            @Override
            protected void addTables()
            {
                this.registerLootTable(KEY_CHEST, BlockTables::droppingWithContents);
            }

            protected static LootTable.Builder dropping(Block block, ILootCondition.IBuilder condition, LootEntry.Builder<?> alternativeEntry) {
                return LootTable.builder()
                        .addLootPool(LootPool.builder()
                                .rolls(ConstantRange.of(1))
                                .addEntry(ItemLootEntry
                                        .builder(block).acceptCondition(condition)
                                        .alternatively(alternativeEntry)));
            }

            protected static LootTable.Builder droppingWithContents(Block block) {
                return LootTable.builder()
                        .addLootPool(withSurvivesExplosion(block,
                                LootPool.builder()
                                        .rolls(ConstantRange.of(1))
                                        .addEntry(ItemLootEntry.builder(block)
                                                .acceptCondition(SILK_TOUCH)
                                                .acceptFunction(CopyName.builder(CopyName.Source.BLOCK_ENTITY))
                                                .acceptFunction(CopyNbt.builder(CopyNbt.Source.BLOCK_ENTITY)
                                                        .replaceOperation("Key", "BlockEntityTag.Key")
                                                        .replaceOperation("IsPrivate", "BlockEntityTag.IsPrivate")
                                                        .replaceOperation("Bound", "BlockEntityTag.Bound")
                                                )
                                                .alternatively(ItemLootEntry.builder(LOCK)
                                                        .acceptFunction(CopyNbt.builder(CopyNbt.Source.BLOCK_ENTITY)
                                                                .replaceOperation("Key", "Key")
                                                                .replaceOperation("IsPrivate", "IsPrivate")
                                                                .replaceOperation("Bound", "Bound")
                                                        ))
                                        )
                                )
                        ).addLootPool(LootPool.builder().rolls(ConstantRange.of(1))
                                .addEntry(withNoSilkTouchRandomly(block, Items.OBSIDIAN, ConstantRange.of(8)))
                        );
            }

            protected static LootEntry.Builder<?> withNoSilkTouchRandomly(Block block, IItemProvider item, IRandomRange range) {
                return withExplosionDecay(block, ItemLootEntry.builder(item).acceptFunction(SetCount.builder(range))).acceptCondition(NO_SILK_TOUCH);
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
        protected void registerRecipes(Consumer<IFinishedRecipe> consumer)
        {
            ShapedRecipeBuilder.shapedRecipe(Enderthing.KEY)
                    .patternLine("o  ")
                    .patternLine("eog")
                    .patternLine("nnn")
                    .key('o', Blocks.OBSIDIAN)
                    .key('g', Items.GOLD_INGOT)
                    .key('e', Items.ENDER_EYE)
                    .key('n', Items.IRON_NUGGET)
                    .addCriterion("has_gold", hasItem(Items.ENDER_EYE))
                    .build(consumer);

            ShapedRecipeBuilder.shapedRecipe(Enderthing.LOCK)
                    .patternLine(" g ")
                    .patternLine("geg")
                    .patternLine("nnn")
                    .key('g', Items.GOLD_INGOT)
                    .key('e', Items.ENDER_EYE)
                    .key('n', Items.IRON_NUGGET)
                    .addCriterion("has_gold", hasItem(Items.ENDER_EYE))
                    .build(consumer);

            ShapedRecipeBuilder.shapedRecipe(Enderthing.PACK)
                    .patternLine("lel")
                    .patternLine("nnn")
                    .patternLine("lcl")
                    .key('l', Items.LEATHER)
                    .key('c', Blocks.ENDER_CHEST)
                    .key('e', Items.ENDER_EYE)
                    .key('n', Items.IRON_NUGGET)
                    .addCriterion("has_gold", hasItem(Items.ENDER_EYE))
                    .build(consumer);

            ShapedRecipeBuilder.shapedRecipe(Enderthing.CARD)
                    .patternLine("nnn")
                    .patternLine("ppp")
                    .patternLine("nnn")
                    .key('n', Items.GOLD_NUGGET)
                    .key('p', Items.PAPER)
                    .addCriterion("has_gold", hasItem(Items.ENDER_EYE))
                    .build(consumer);

            CustomRecipeBuilder.customRecipe(MakePrivateRecipe.SERIALIZER).build(consumer, "enderthing:make_private");
            CustomRecipeBuilder.customRecipe(AddLockRecipe.SERIALIZER).build(consumer, "enderthing:add_lock");
            CustomRecipeBuilder.customRecipe(MakeBoundRecipe.SERIALIZER).build(consumer, "enderthing:make_bound");
        }
    }
}

