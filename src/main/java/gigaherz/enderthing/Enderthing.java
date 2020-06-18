package gigaherz.enderthing;

import gigaherz.enderthing.blocks.EnderKeyChestBlock;
import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import gigaherz.enderthing.gui.KeyContainer;
import gigaherz.enderthing.gui.KeyScreen;
import gigaherz.enderthing.gui.PasscodeContainer;
import gigaherz.enderthing.gui.PasscodeScreen;
import gigaherz.enderthing.items.EnderCardItem;
import gigaherz.enderthing.items.EnderKeyItem;
import gigaherz.enderthing.items.EnderLockItem;
import gigaherz.enderthing.items.EnderPackItem;
import gigaherz.enderthing.network.SetItemKey;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod.EventBusSubscriber
@Mod(Enderthing.MODID)
public class Enderthing
{
    public static final String MODID = "enderthing";

    @ObjectHolder("enderthing:key_chest")
    public static EnderKeyChestBlock enderKeyChest;

    @ObjectHolder("enderthing:key_chest_private")
    public static EnderKeyChestBlock enderKeyChestPrivate;

    @ObjectHolder("enderthing:key")
    public static EnderKeyItem enderKey;

    @ObjectHolder("enderthing:key_private")
    public static EnderKeyItem enderKeyPrivate;

    @ObjectHolder("enderthing:lock")
    public static EnderLockItem enderLock;

    @ObjectHolder("enderthing:lock_private")
    public static EnderLockItem enderLockPrivate;

    @ObjectHolder("enderthing:pack")
    public static EnderPackItem enderPack;

    @ObjectHolder("enderthing:pack_private")
    public static EnderPackItem enderPackPrivate;

    @ObjectHolder("enderthing:card")
    public static EnderCardItem enderCard;

    public static Enderthing instance;

    public static ItemGroup tabEnderthing = new ItemGroup("enderthing.things")
    {
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(enderKey);
        }
    };

    public static boolean breakChestOnHarvest = true;

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
        instance = this;

        // TODO: Rune dust, rune, rune pattern

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addGenericListener(Block.class, this::registerBlocks);
        modEventBus.addGenericListener(Item.class, this::registerItems);
        modEventBus.addGenericListener(TileEntityType.class, this::registerTileEntities);
        modEventBus.addGenericListener(ContainerType.class, this::registerContainerss);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        //ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.GUIFACTORY, () -> message -> Containers.Client.getClientGuiElement(message));
    }

    public void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(
                new EnderKeyChestBlock(Block.Properties.create(Material.IRON)
                        .hardnessAndResistance(22.5F, 1000F)
                        .sound(SoundType.STONE)
                        .lightValue(5)).setRegistryName("key_chest"),
                new EnderKeyChestBlock.Private(Block.Properties.create(Material.IRON)
                        .hardnessAndResistance(22.5F, 1000F)
                        .sound(SoundType.STONE)
                        .lightValue(5)).setRegistryName("key_chest_private"),
                new EnderKeyChestBlock.Private(Block.Properties.create(Material.IRON)
                        .hardnessAndResistance(22.5F, 1000F)
                        .sound(SoundType.STONE)
                        .lightValue(5)).setRegistryName("key_chest_bound")

        );
    }

    public void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                new EnderKeyChestBlock.AsItem(enderKeyChest, false, new Item.Properties().group(tabEnderthing))
                        .setRegistryName(enderKeyChest.getRegistryName()),
                new EnderKeyChestBlock.AsItem(enderKeyChestPrivate, true, new Item.Properties().group(tabEnderthing))
                        .setRegistryName(enderKeyChestPrivate.getRegistryName()),

                new EnderKeyItem(false, new Item.Properties().group(tabEnderthing)).setRegistryName("key"),
                new EnderKeyItem(true, new Item.Properties().group(tabEnderthing)).setRegistryName("key_private"),
                new EnderLockItem(false, new Item.Properties().group(tabEnderthing)).setRegistryName("lock"),
                new EnderLockItem(true, new Item.Properties().group(tabEnderthing)).setRegistryName("lock_private"),
                new EnderPackItem(false, new Item.Properties().group(tabEnderthing)).setRegistryName("pack"),
                new EnderPackItem(true, new Item.Properties().group(tabEnderthing)).setRegistryName("pack_private"),

                new EnderCardItem(new Item.Properties().maxStackSize(1).group(tabEnderthing)).setRegistryName("card")
        );
    }

    private void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event)
    {
        event.getRegistry().registerAll(
                TileEntityType.Builder.create(EnderKeyChestTileEntity::new, enderKeyChest).build(null).setRegistryName("key_chest"),
                TileEntityType.Builder.create(EnderKeyChestTileEntity.Private::new, enderKeyChestPrivate).build(null).setRegistryName("key_chest_private")
        );
    }

    private void registerContainerss(RegistryEvent.Register<ContainerType<?>> event)
    {
        event.getRegistry().registerAll(
                IForgeContainerType.create(KeyContainer::new).setRegistryName("key"),
                IForgeContainerType.create(PasscodeContainer::new).setRegistryName("passcode")
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
    }

        /*
    public void initRecipes()
    {
        GameRegistry.addRecipe(new ItemStack(enderCard),
                "nnn",
                "ppp",
                "nnn",
                'n', Items.GOLD_NUGGET,
                'p', Items.PAPER);

        GameRegistry.addRecipe(new KeyRecipe());
        GameRegistry.addRecipe(new LockRecipe());
        GameRegistry.addRecipe(new PackRecipe());
        GameRegistry.addRecipe(new MakePrivateRecipe(enderKey));
        GameRegistry.addRecipe(new MakePrivateRecipe(enderLock));
        GameRegistry.addRecipe(new MakePrivateRecipe(enderPack));
        GameRegistry.addRecipe(new MakePrivateRecipe(enderKeyChest));
        GameRegistry.addRecipe(new ChangeColorsRecipe(enderKey, false));
        GameRegistry.addRecipe(new ChangeColorsRecipe(enderLock, false));
        GameRegistry.addRecipe(new ChangeColorsRecipe(enderPack, false));
        GameRegistry.addRecipe(new ChangeColorsRecipe(enderKeyChest, false));
        GameRegistry.addRecipe(new ChangeColorsRecipe(enderKey, true));
        GameRegistry.addRecipe(new ChangeColorsRecipe(enderLock, true));
        GameRegistry.addRecipe(new ChangeColorsRecipe(enderPack, true));
        GameRegistry.addRecipe(new ChangeColorsRecipe(enderKeyChest, true));

        RecipeSorter.register(MODID + ":ender_key", KeyRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register(MODID + ":ender_lock", LockRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register(MODID + ":ender_pack", PackRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register(MODID + ":make_private", MakePrivateRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register(MODID + ":change_color", ChangeColorsRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");

    }
    */

    public static ResourceLocation location(String path)
    {
        return new ResourceLocation(MODID, path);
    }

    public static class Client
    {
        public static void addStandardInformation(ItemStack stack, List<ITextComponent> tooltip)
        {
            long key = KeyUtils.getKey(stack);

            if (key >= 0)
            {
                tooltip.add(new TranslationTextComponent("tooltip.enderthing.key", key).applyTextStyle(TextFormatting.ITALIC));
            }
            else
            {
                tooltip.add(new TranslationTextComponent("tooltip.enderthing.key_missing").applyTextStyle(TextFormatting.ITALIC));
            }

            //tooltip.add(new TextComponentTranslation("tooltip.enderthing.password", words));
        }

    }
}

