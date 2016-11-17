package gigaherz.enderthing;

import com.google.common.collect.Maps;
import com.mojang.realmsclient.gui.ChatFormatting;
import gigaherz.enderthing.blocks.BlockEnderKeyChest;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import gigaherz.enderthing.gui.GuiHandler;
import gigaherz.enderthing.items.ItemEnderCard;
import gigaherz.enderthing.items.ItemEnderKey;
import gigaherz.enderthing.items.ItemEnderLock;
import gigaherz.enderthing.items.ItemEnderPack;
import gigaherz.enderthing.network.UpdatePlayersUsing;
import gigaherz.enderthing.recipes.*;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.RecipeSorter;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

@Mod.EventBusSubscriber
@Mod(   modid = Enderthing.MODID,
        version = Enderthing.VERSION,
        acceptedMinecraftVersions = "[1.9.4,1.11.0)")
public class Enderthing
{
    public static final String MODID = "enderthing";
    public static final String VERSION = "@VERSION@";
    public static final String CHANNEL = "Enderthing";
    public static final String INVENTORY_ID_KEY = "InventoryId";
    public static final int ITEM_PRIVATE_BIT = 1;
    public static final int BLOCK_PRIVATE_BIT = 8;

    public static BlockEnderKeyChest enderKeyChest;
    public static ItemEnderKey enderKey;
    public static ItemEnderLock enderLock;
    public static ItemEnderPack enderPack;
    public static ItemEnderCard enderCard;

    @Mod.Instance(value = Enderthing.MODID)
    public static Enderthing instance;

    @SidedProxy(clientSide = "gigaherz.enderthing.client.ClientProxy", serverSide = "gigaherz.enderthing.server.ServerProxy")
    public static IModProxy proxy;

    public static GuiHandler guiHandler = new GuiHandler();

    public static CreativeTabs tabEnderthing = new CreativeTabs("tabEnderthing")
    {
        @Nonnull
        @Override
        public Item getTabIconItem()
        {
            return enderKey;
        }
    };

    public static SimpleNetworkWrapper channel;
    public static Logger logger;
    public static Configuration config;

    public static boolean breakChestOnHarvest = true;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(
                enderKeyChest = new BlockEnderKeyChest("ender_key_chest")
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                enderKeyChest.createItemBlock(),

                enderKey = new ItemEnderKey("ender_key"),
                enderLock = new ItemEnderLock("ender_lock"),
                enderPack = new ItemEnderPack("ender_pack"),
                enderCard = new ItemEnderCard("ender_card")
        );
    }

    private void registerTileEntities()
    {
        GameRegistry.registerTileEntityWithAlternatives(TileEnderKeyChest.class, location("ender_key_chest").toString(), "tileEnderKeyChest");
        GameRegistry.registerTileEntityWithAlternatives(TileEnderKeyChest.Private.class, location("ender_key_chest_private").toString(), "tileEnderKeyPrivateChest");
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        config = new Configuration(event.getSuggestedConfigurationFile());
        Property breakChest = config.get("Balance", "BreakChestOnHarvest", true);
        if (!breakChest.wasRead())
            config.save();
        else
            breakChestOnHarvest = breakChest.getBoolean();

        registerTileEntities();

        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);

        int messageNumber = 0;
        channel.registerMessage(UpdatePlayersUsing.Handler.class, UpdatePlayersUsing.class, messageNumber++, Side.CLIENT);
        logger.debug("Final message number: " + messageNumber);

        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();

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

    public static ResourceLocation location(String path)
    {
        return new ResourceLocation(MODID, path);
    }

    public static int getIdFromItem(ItemStack stack)
    {
        int color1 = 0;
        int color2 = 0;
        int color3 = 0;

        NBTTagCompound tag = stack.getTagCompound();
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
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null)
        {
            NBTTagCompound etag = tag.getCompoundTag("BlockEntityTag");
            if (etag != null)
            {
                return etag.getInteger("InventoryId");
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

    public static int getPrivateBit(Item item)
    {
        if (item instanceof ItemBlock)
            return BLOCK_PRIVATE_BIT;
        return ITEM_PRIVATE_BIT;
    }

    public static ItemStack getItem(Item item, int c1, int c2, int c3, boolean priv)
    {
        if (item instanceof ItemBlock)
        {
            return getItem(getId(c1, c2, c3), priv);
        }

        ItemStack key = new ItemStack(item, 1, priv ? getPrivateBit(item) : 0);

        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Color1", (byte) c1);
        tag.setByte("Color2", (byte) c2);
        tag.setByte("Color3", (byte) c3);

        key.setTagCompound(tag);

        return key;
    }

    public static ItemStack getLock(int id, boolean priv)
    {
        int c1 = id & 15;
        int c2 = (id >> 4) & 15;
        int c3 = (id >> 8) & 15;

        return getItem(enderLock, c1, c2, c3, priv);
    }

    public static boolean isPrivate(ItemStack input)
    {
        if (input.getItem() instanceof ItemBlock)
            return (input.getMetadata() & BLOCK_PRIVATE_BIT) != 0;
        return (input.getMetadata() & ITEM_PRIVATE_BIT) != 0;
    }

    public static ItemStack getItem(int id, boolean priv)
    {
        ItemStack stack = new ItemStack(enderKeyChest, 1, priv ? BLOCK_PRIVATE_BIT : 0);

        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound etag = new NBTTagCompound();
        etag.setInteger(INVENTORY_ID_KEY, id);
        tag.setTag("BlockEntityTag", etag);

        stack.setTagCompound(tag);

        return stack;
    }

    public static int getIdFromBlock(IBlockAccess world, BlockPos pos)
    {
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEnderKeyChest)
        {
            return ((TileEnderKeyChest) te).getInventoryId();
        }

        return -1;
    }

    public static void addStandardInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced)
    {
        if (Enderthing.isPrivate(stack))
        {
            tooltip.add(ChatFormatting.BOLD + I18n.format("tooltip." + Enderthing.MODID + ".private"));
        }

        int id = Enderthing.getId(stack);

        if (id < 0)
        {
            tooltip.add(ChatFormatting.ITALIC + I18n.format("tooltip." + Enderthing.MODID + ".colorMissing"));
            return;
        }

        int color1 = id & 15;
        int color2 = (id >> 4) & 15;
        int color3 = (id >> 8) & 15;

        EnumDyeColor c1 = EnumDyeColor.byMetadata(color1);
        EnumDyeColor c2 = EnumDyeColor.byMetadata(color2);
        EnumDyeColor c3 = EnumDyeColor.byMetadata(color3);

        tooltip.add(I18n.format("tooltip." + Enderthing.MODID + ".colors", c1.getName(), c2.getName(), c3.getName()));
    }
}

