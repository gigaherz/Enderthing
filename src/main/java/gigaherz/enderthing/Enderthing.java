package gigaherz.enderthing;

import gigaherz.enderthing.blocks.BlockEnderKeyChest;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import gigaherz.enderthing.gui.GuiHandler;
import gigaherz.enderthing.items.ItemEnderCard;
import gigaherz.enderthing.items.ItemEnderKey;
import gigaherz.enderthing.items.ItemEnderLock;
import gigaherz.enderthing.items.ItemEnderPack;
import gigaherz.enderthing.items.ItemEnderCard;
import gigaherz.enderthing.network.UpdatePlayersUsing;
import gigaherz.enderthing.recipes.KeyRecipe;
import gigaherz.enderthing.recipes.LockRecipe;
import gigaherz.enderthing.recipes.MakePrivateRecipe;
import gigaherz.enderthing.recipes.PackRecipe;
import gigaherz.enderthing.storage.PrivateInventoryCapability;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.RecipeSorter;
import org.apache.logging.log4j.Logger;

@Mod(name = Enderthing.NAME,
        modid = Enderthing.MODID,
        version = Enderthing.VERSION)
public class Enderthing
{
    public static final String NAME = "Enderthing";
    public static final String MODID = "enderthing";
    public static final String VERSION = "@VERSION@";
    public static final String CHANNEL = "Enderthing";

    public static BlockEnderKeyChest blockEnderKeyChest;
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
        @Override
        public Item getTabIconItem()
        {
            return enderKey;
        }
    };

    public static KeyRecipe keyRecipe;
    public static LockRecipe lockRecipe;
    public static PackRecipe packRecipe;
    public static MakePrivateRecipe makePrivate;

    public static SimpleNetworkWrapper channel;
    public static Logger logger;
    public static Configuration config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        config = new Configuration(event.getSuggestedConfigurationFile());

        blockEnderKeyChest = new BlockEnderKeyChest("blockEnderKeyChest");
        GameRegistry.registerBlock(blockEnderKeyChest, BlockEnderKeyChest.AsItem.class);
        GameRegistry.registerTileEntity(TileEnderKeyChest.class, "tileEnderKeyChest");
        GameRegistry.registerTileEntity(TileEnderKeyChest.Private.class, "tileEnderKeyPrivateChest");

        enderKey = new ItemEnderKey("enderKey");
        GameRegistry.registerItem(enderKey);

        enderLock = new ItemEnderLock("enderLock");
        GameRegistry.registerItem(enderLock);

        enderPack = new ItemEnderPack("enderPack");
        GameRegistry.registerItem(enderPack);

        enderCard = new ItemEnderCard("enderCard");
        GameRegistry.registerItem(enderCard);

        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        Property prop = config.get("Compatibility", "ImportCapabilityPrivateData", true);
        if(prop.getBoolean())
        {
            PrivateInventoryCapability.register();
            prop.set(false);
            config.save();
        }

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

        GameRegistry.addRecipe(keyRecipe = new KeyRecipe());
        GameRegistry.addRecipe(lockRecipe = new LockRecipe());
        GameRegistry.addRecipe(packRecipe = new PackRecipe());
        GameRegistry.addRecipe(makePrivate = new MakePrivateRecipe());
        RecipeSorter.register(MODID + ":key_recipe", KeyRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register(MODID + ":lock_recipe", LockRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register(MODID + ":pack_recipe", PackRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register(MODID + ":make_private", MakePrivateRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
    }
}
