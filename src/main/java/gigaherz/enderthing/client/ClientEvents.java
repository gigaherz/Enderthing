package gigaherz.enderthing.client;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.KeyUtils;
import gigaherz.enderthing.blocks.EnderKeyChestRenderer;
import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Enderthing.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents
{
    @SubscribeEvent
    public static void modelRegistry(ModelRegistryEvent event)
    {
        BlockEntityRenderers.register(EnderKeyChestTileEntity.TYPE, EnderKeyChestRenderer::new);
    }

    @SubscribeEvent
    public static void itemColors(ColorHandlerEvent.Item event)
    {
        event.getItemColors().register((stack, layer) -> {
                    if (layer < 1 || layer > 3)
                        return -1;

                    long id = KeyUtils.getKey(stack);
                    if (id < 0)
                        return 0;

                    int r = ((int) (id >>> ((layer - 1) * 21)) & 0x7f) * 255 / 127;
                    int g = ((int) (id >>> ((layer - 1) * 21 + 7)) & 0x7f) * 255 / 127;
                    int b = ((int) (id >>> ((layer - 1) * 21 + 14)) & 0x7f) * 255 / 127;

                    return (r << 16) | (g << 8) | (b);
                },
                Enderthing.KEY, Enderthing.LOCK, Enderthing.PACK);
    }
}
