package gigaherz.enderthing.client;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid=Enderthing.MODID, bus= Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents
{
    @SubscribeEvent
    public static void modelRegistry(ModelRegistryEvent event)
    {
        ClientRegistry.bindTileEntitySpecialRenderer(EnderKeyChestTileEntity.class, new EnderKeyChestRenderer());
    }
}
