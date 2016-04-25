package gigaherz.enderthing;

import gigaherz.enderthing.blocks.BlockSharedChest;
import gigaherz.enderthing.blocks.TileSharedChest;
import gigaherz.enderthing.items.ItemEnderKey;
import gigaherz.enderthing.gui.GuiHandler;
import gigaherz.enderthing.recipes.KeyRecipe;
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

    public static BlockSharedChest sharedChest;
    public static ItemEnderKey enderKey;

    @Mod.Instance(value = Enderthing.MODID)
    public static Enderthing instance;

    @SidedProxy(clientSide = "gigaherz.enderthing.client.ClientProxy", serverSide = "gigaherz.enderthing.server.ServerProxy")
    public static IModProxy proxy;

    public static GuiHandler guiHandler = new GuiHandler();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        sharedChest = new BlockSharedChest("blockSharedChest");
        GameRegistry.register(sharedChest);
        GameRegistry.register(sharedChest.createItemBlock());
        GameRegistry.registerTileEntity(TileSharedChest.class, "tileSharedChest");

        enderKey = new ItemEnderKey("enderKey");
        GameRegistry.register(enderKey);

        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();

        GameRegistry.addRecipe(new KeyRecipe());
        RecipeSorter.register(MODID + ":key_recipe", KeyRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");

    }
}
