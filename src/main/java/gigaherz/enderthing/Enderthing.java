package gigaherz.enderthing;

import com.google.common.collect.Maps;
import gigaherz.enderthing.blocks.BlockEnderKeyChest;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import gigaherz.enderthing.gui.GuiHandler;
import gigaherz.enderthing.items.*;
import gigaherz.enderthing.network.UpdatePlayersUsing;
import gigaherz.enderthing.recipes.*;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.RecipeSorter;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Map;

@Mod(name = Enderthing.NAME,
        modid = Enderthing.MODID,
        version = Enderthing.VERSION,
        acceptedMinecraftVersions = "[1.9.4,1.11.0)")
public class Enderthing
{
    public static final String NAME = "Enderthing";
    public static final String MODID = "enderthing";
    public static final String VERSION = "@VERSION@";
    public static final String CHANNEL = "Enderthing";

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

        enderKeyChest = new BlockEnderKeyChest("ender_key_chest");
        GameRegistry.register(enderKeyChest);
        GameRegistry.register(enderKeyChest.createItemBlock());
        GameRegistry.registerTileEntity(TileEnderKeyChest.class, location("ender_key_chest").toString());
        GameRegistry.registerTileEntity(TileEnderKeyChest.Private.class, location("ender_key_chest_private").toString());
        addAlternativeName(enderKeyChest, "blockEnderKeyChest");
        addAlternativeName(TileEnderKeyChest.class, "tileEnderKeyChest");
        addAlternativeName(TileEnderKeyChest.Private.class, "tileEnderKeyPrivateChest");

        enderKey = new ItemEnderKey("ender_key");
        GameRegistry.register(enderKey);
        addAlternativeName(enderKey, "enderKey");

        enderLock = new ItemEnderLock("ender_lock");
        GameRegistry.register(enderLock);
        addAlternativeName(enderLock, "enderLock");

        enderPack = new ItemEnderPack("ender_pack");
        GameRegistry.register(enderPack);
        addAlternativeName(enderPack, "enderPack");

        enderCard = new ItemEnderCard("ender_card");
        GameRegistry.register(enderCard);
        addAlternativeName(enderCard, "enderCard");

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

    Map<String, Class<? extends TileEntity >> nameToClassMap = ReflectionHelper.getPrivateValue(TileEntity.class, null, "field_145855_i", "nameToClassMap");
    private void addAlternativeName(Class<? extends TileEntity> clazz, String altName)
    {
        nameToClassMap.put(altName, clazz);
    }

    private Map<ResourceLocation, Item> upgradeItemNames = Maps.newHashMap();
    private void addAlternativeName(Item item, String altName)
    {
        upgradeItemNames.put(new ResourceLocation(MODID, altName), item);
    }

    private Map<ResourceLocation, Block> upgradeBlockNames = Maps.newHashMap();
    private void addAlternativeName(Block block, String altName)
    {
        upgradeBlockNames.put(new ResourceLocation(MODID, altName), block);
        Item item = Item.getItemFromBlock(block);
        if (item != null)
            addAlternativeName(item, altName);
    }

    @Mod.EventHandler
    public void onMissingMapping(FMLMissingMappingsEvent ev)
    {
        for (FMLMissingMappingsEvent.MissingMapping missing : ev.get())
        {
            if (missing.type == GameRegistry.Type.ITEM
                    && upgradeItemNames.containsKey(missing.resourceLocation))
            {
                missing.remap(upgradeItemNames.get(missing.resourceLocation));
            }

            if (missing.type == GameRegistry.Type.BLOCK
                    && upgradeBlockNames.containsKey(missing.resourceLocation))
            {
                missing.remap(upgradeBlockNames.get(missing.resourceLocation));
            }
        }
    }

    public static ResourceLocation location(String path)
    {
        return new ResourceLocation(MODID, path);
    }
}

