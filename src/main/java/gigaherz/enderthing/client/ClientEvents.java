package gigaherz.enderthing.client;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid=Enderthing.MODID, bus= Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents
{
    @SubscribeEvent
    public static void modelRegistry(ModelRegistryEvent event)
    {
        OBJLoader.INSTANCE.addDomain(Enderthing.MODID);

        ClientRegistry.bindTileEntitySpecialRenderer(TileEnderKeyChest.class, new RenderEnderKeyChest());

        /*
        registerBlockModelAsItem(Enderthing.enderKeyChest, 0, "facing=north,inventory=true,private=false");
        registerBlockModelAsItem(Enderthing.enderKeyChest, 8, "facing=north,inventory=true,private=true");
        registerItemModel(Enderthing.enderKey, 0, "ender_key");
        registerItemModel(Enderthing.enderKey, 1, "ender_key_private");
        registerItemModel(Enderthing.enderLock, 0, "ender_lock");
        registerItemModel(Enderthing.enderLock, 1, "ender_lock_private");
        registerItemModel(Enderthing.enderPack, 0, "ender_pack");
        registerItemModel(Enderthing.enderPack, 1, "ender_pack_private");
        registerItemModel(Enderthing.enderCard, 0, "ender_card");
        */
    }

    @SubscribeEvent
    public static void itemColors(ColorHandlerEvent.Item event)
    {
        event.getItemColors().register(new ItemColorHandler.ItemTag(),
                Enderthing.enderKey, Enderthing.enderLock, Enderthing.enderPack,
                Enderthing.enderKeyPrivate, Enderthing.enderLockPrivate, Enderthing.enderPackPrivate);
        event.getItemColors().register(new ItemColorHandler.BlockTag(),
                Enderthing.enderKeyChest.asItem(), Enderthing.enderKeyChestPrivate.asItem(), Enderthing.enderKeyChestBound.asItem());
    }
}
