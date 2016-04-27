package gigaherz.enderthing;

import gigaherz.enderthing.blocks.BlockEnderKeyChest;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import gigaherz.enderthing.gui.GuiHandler;
import gigaherz.enderthing.items.ItemEnderKey;
import gigaherz.enderthing.items.ItemEnderLock;
import gigaherz.enderthing.recipes.KeyRecipe;
import gigaherz.enderthing.recipes.LockRecipe;
import gigaherz.enderthing.recipes.MakePrivateRecipe;
import gigaherz.enderthing.storage.PrivateInventoryManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;

@Mod(name = Enderthing.NAME,
        modid = Enderthing.MODID,
        version = Enderthing.VERSION)
public class Enderthing
{
    public static final String NAME = "Enderthing";
    public static final String MODID = "enderthing";
    public static final String VERSION = "@VERSION@";

    public static BlockEnderKeyChest blockEnderKeyChest;
    public static ItemEnderKey enderKey;
    public static ItemEnderLock enderLock;

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
    public static MakePrivateRecipe makePrivate;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        blockEnderKeyChest = new BlockEnderKeyChest("blockEnderKeyChest");
        GameRegistry.register(blockEnderKeyChest);
        GameRegistry.register(blockEnderKeyChest.createItemBlock());
        GameRegistry.registerTileEntity(TileEnderKeyChest.class, "tileEnderKeyChest");
        GameRegistry.registerTileEntity(TileEnderKeyChest.Private.class, "tileEnderKeyPrivateChest");

        enderKey = new ItemEnderKey("enderKey");
        GameRegistry.register(enderKey);

        enderLock = new ItemEnderLock("enderLock");
        GameRegistry.register(enderLock);

        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        PrivateInventoryManager.register();

        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();

        GameRegistry.addRecipe(keyRecipe = new KeyRecipe());
        GameRegistry.addRecipe(lockRecipe = new LockRecipe());
        GameRegistry.addRecipe(makePrivate = new MakePrivateRecipe());
        RecipeSorter.register(MODID + ":key_recipe", KeyRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register(MODID + ":lock_recipe", LockRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register(MODID + ":make_private", MakePrivateRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
    }
}
