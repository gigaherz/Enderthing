package gigaherz.enderthing;

import gigaherz.enderthing.blocks.BlockEnderKeyChest;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import gigaherz.enderthing.gui.GuiHandler;
import gigaherz.enderthing.items.ItemEnderCard;
import gigaherz.enderthing.items.ItemEnderKey;
import gigaherz.enderthing.items.ItemEnderLock;
import gigaherz.enderthing.items.ItemEnderPack;
import gigaherz.enderthing.network.UpdatePlayersUsing;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber
@Mod(Enderthing.MODID)
public class Enderthing
{
    public static final String MODID = "enderthing";

    public static final String INVENTORY_ID_KEY = "InventoryId";

    @ObjectHolder("enderthing:key_chest")
    public static BlockEnderKeyChest enderKeyChest;

    @ObjectHolder("enderthing:key_chest_private")
    public static BlockEnderKeyChest enderKeyChestPrivate;

    @ObjectHolder("enderthing:key_chest_bound")
    public static BlockEnderKeyChest enderKeyChestBound;

    @ObjectHolder("enderthing:key")
    public static ItemEnderKey enderKey;

    @ObjectHolder("enderthing:key_private")
    public static ItemEnderKey enderKeyPrivate;

    @ObjectHolder("enderthing:lock")
    public static ItemEnderLock enderLock;

    @ObjectHolder("enderthing:lock_private")
    public static ItemEnderLock enderLockPrivate;

    @ObjectHolder("enderthing:pack")
    public static ItemEnderPack enderPack;

    @ObjectHolder("enderthing:pack_private")
    public static ItemEnderPack enderPackPrivate;

    @ObjectHolder("enderthing:card")
    public static ItemEnderCard enderCard;

    @ObjectHolder("enderthing:key_chest")
    public static TileEntityType<TileEnderKeyChest> tileKeyChest;
    @ObjectHolder("enderthing:key_chest_private")
    public static TileEntityType<TileEnderKeyChest.Private> tileKeyChestPrivate;

    public static Enderthing instance;

    //public static GuiHandler guiHandler = new GuiHandler();

    public static ItemGroup tabEnderthing = new ItemGroup("tabEnderthing")
    {
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(enderKey);
        }
    };

    public static final String CHANNEL = MODID;
    private static final String PROTOCOL_VERSION = "1.0";
    public static SimpleChannel channel = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MODID, CHANNEL))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static boolean breakChestOnHarvest = true;

    public static final Logger logger = LogManager.getLogger(MODID);

    public Enderthing()
    {
        instance = this;

        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Block.class, this::registerBlocks);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, this::registerItems);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(TileEntityType.class, this::registerTileEntities);


        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.GUIFACTORY, () -> message -> GuiHandler.Client.getClientGuiElement(message));
    }

    public void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(
                new BlockEnderKeyChest(Block.Properties.create(Material.IRON)
                        .hardnessAndResistance(22.5F, 1000F)
                        .sound(SoundType.STONE)
                        .lightValue(5)).setRegistryName("key_chest"),
                new BlockEnderKeyChest.Private(Block.Properties.create(Material.IRON)
                        .hardnessAndResistance(22.5F, 1000F)
                        .sound(SoundType.STONE)
                        .lightValue(5)).setRegistryName("key_chest_private"),
                new BlockEnderKeyChest.Private(Block.Properties.create(Material.IRON)
                        .hardnessAndResistance(22.5F, 1000F)
                        .sound(SoundType.STONE)
                        .lightValue(5)).setRegistryName("key_chest_bound")

        );
    }

    public void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                new BlockEnderKeyChest.AsItem(enderKeyChest, false, false, new Item.Properties().group(tabEnderthing))
                        .setRegistryName(enderKeyChest.getRegistryName()),
                new BlockEnderKeyChest.AsItem(enderKeyChestPrivate, true, false, new Item.Properties().group(tabEnderthing))
                        .setRegistryName(enderKeyChestPrivate.getRegistryName()),

                new ItemEnderKey(false, new Item.Properties().group(tabEnderthing)).setRegistryName("key"),
                new ItemEnderKey(true, new Item.Properties().group(tabEnderthing)).setRegistryName("key_private"),
                new ItemEnderLock(false, new Item.Properties().group(tabEnderthing)).setRegistryName("lock"),
                new ItemEnderLock(true, new Item.Properties().group(tabEnderthing)).setRegistryName("lock_private"),
                new ItemEnderPack(false, new Item.Properties().group(tabEnderthing)).setRegistryName("pack"),
                new ItemEnderPack(true, new Item.Properties().group(tabEnderthing)).setRegistryName("pack_private"),

                new ItemEnderCard(new Item.Properties().maxStackSize(1).group(tabEnderthing)).setRegistryName("card")
        );
    }

    private void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event)
    {
        TileEntityType.register(location("key_chest").toString(), TileEntityType.Builder.create(TileEnderKeyChest::new));
        TileEntityType.register(location("key_chest_private").toString(), TileEntityType.Builder.create(TileEnderKeyChest.Private::new));
    }

    public void preInit(FMLCommonSetupEvent event)
    {
        /*
        config = new Configuration(event.getSuggestedConfigurationFile());
        Property breakChest = config.get("Balance", "BreakChestOnHarvest", true);
        breakChestOnHarvest = breakChest.getBoolean();

        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);
        */

        int messageNumber = 0;
        channel.registerMessage(messageNumber++, UpdatePlayersUsing.class, UpdatePlayersUsing::encode, UpdatePlayersUsing::new, UpdatePlayersUsing::handle);
        logger.debug("Final message number: " + messageNumber);
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

    public static int getIdFromItem(ItemStack stack)
    {
        int color1 = 0;
        int color2 = 0;
        int color3 = 0;

        NBTTagCompound tag = stack.getTag();
        if (tag != null)
        {
            color1 = tag.getByte("Color1");
            color2 = tag.getByte("Color2");
            color3 = tag.getByte("Color3");
        }

        return getId(color1, color2, color3);
    }

    public static int getIdFromBlock(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTag();
        if (tag != null)
        {
            NBTTagCompound etag = tag.getCompound("BlockEntityTag");
            if (etag != null)
            {
                return etag.getInt("InventoryId");
            }
        }

        return -1;
    }

    private static int getId(int c1, int c2, int c3)
    {
        return c1 | (c2 << 4) | (c3 << 8);
    }

    public static int getId(ItemStack stack)
    {
        if (stack.getItem() instanceof ItemBlock)
        {
            return getIdFromBlock(stack);
        }

        return getIdFromItem(stack);
    }

    public static ItemStack getItem(Item item, int c1, int c2, int c3, boolean priv)
    {
        if (item instanceof ItemBlock)
        {
            return getItem(getId(c1, c2, c3), priv);
        }

        ItemStack key = new ItemStack(item, 1);

        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("Private", priv);
        tag.setByte("Color1", (byte) c1);
        tag.setByte("Color2", (byte) c2);
        tag.setByte("Color3", (byte) c3);

        key.setTag(tag);

        return key;
    }

    public static ItemStack getLock(int id, boolean priv)
    {
        int c1 = id & 15;
        int c2 = (id >> 4) & 15;
        int c3 = (id >> 8) & 15;

        return getItem(enderLock, c1, c2, c3, priv);
    }

    public static ItemStack getItem(int id, boolean priv)
    {
        ItemStack stack = new ItemStack(enderKeyChest, 1);

        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound etag = new NBTTagCompound();
        etag.setInt(INVENTORY_ID_KEY, id);
        etag.setBoolean("Private", priv);
        tag.setTag("BlockEntityTag", etag);

        stack.setTag(tag);

        return stack;
    }

    public static int getIdFromBlock(IBlockReader world, BlockPos pos)
    {
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEnderKeyChest)
        {
            return ((TileEnderKeyChest) te).getInventoryId();
        }

        return -1;
    }

    public static int getIdFromTE(TileEntity te)
    {
        if (te instanceof TileEnderKeyChest)
        {
            return ((TileEnderKeyChest) te).getInventoryId();
        }

        return -1;
    }

    public static void addStandardInformation(ItemStack stack, List<ITextComponent> tooltip, ITooltipFlag flag, boolean isPrivate)
    {
        if (isPrivate)
        {
            tooltip.add(new TextComponentTranslation("tooltip." + Enderthing.MODID + ".private").applyTextStyle(TextFormatting.BOLD));
        }

        int id = Enderthing.getId(stack);

        if (id < 0)
        {
            tooltip.add(new TextComponentTranslation("tooltip." + Enderthing.MODID + ".colorMissing").applyTextStyle(TextFormatting.ITALIC));
            return;
        }

        int color1 = id & 15;
        int color2 = (id >> 4) & 15;
        int color3 = (id >> 8) & 15;

        EnumDyeColor c1 = EnumDyeColor.byId(color1);
        EnumDyeColor c2 = EnumDyeColor.byId(color2);
        EnumDyeColor c3 = EnumDyeColor.byId(color3);

        tooltip.add(new TextComponentTranslation("tooltip." + Enderthing.MODID + ".colors", c1.getName(), c2.getName(), c3.getName()));
    }

    @Nullable
    public static String queryNameFromUUID(UUID uuid)
    {
        MinecraftServer svr = ServerLifecycleHooks.getCurrentServer();
        if (svr == null)
            return null;
        PlayerList playerList = svr.getPlayerList();
        if (playerList == null)
            return null;
        EntityPlayer player = playerList.getPlayerByUUID(uuid);
        if (player != null)
            return player.getName().getString();
        return null;
    }
}

