package gigaherz.enderthing;

import com.google.common.primitives.Longs;
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
import joptsimple.internal.Strings;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

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

    @ObjectHolder("enderthing:key_chest")
    public static TileEntityType<EnderKeyChestTileEntity> tileKeyChest;
    @ObjectHolder("enderthing:key_chest_private")
    public static TileEntityType<EnderKeyChestTileEntity.Private> tileKeyChestPrivate;

    public static Enderthing instance;

    //public static GuiHandler guiHandler = new GuiHandler();

    public static ItemGroup tabEnderthing = new ItemGroup("enderthing.things")
    {
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(enderKey);
        }
    };

    public static boolean breakChestOnHarvest = true;

    public static final Logger logger = LogManager.getLogger(MODID);

    public Enderthing()
    {
        instance = this;

        // TODO: Rune dust, rune, rune pattern

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addGenericListener(Block.class, this::registerBlocks);
        modEventBus.addGenericListener(Item.class, this::registerItems);
        modEventBus.addGenericListener(TileEntityType.class, this::registerTileEntities);
        modEventBus.addGenericListener(ContainerType.class, this::registerContainerss);
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

            if (key < 0)
            {
                tooltip.add(new TranslationTextComponent("tooltip." + Enderthing.MODID + ".color_missing").applyTextStyle(TextFormatting.ITALIC));
                return;
            }

            //tooltip.add(new TextComponentTranslation("tooltip." + Enderthing.MODID + ".password", words));
        }

    }
}

