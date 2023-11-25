package dev.gigaherz.enderthing.client;

import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import dev.gigaherz.enderthing.blocks.EnderKeyChestRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Enderthing.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents
{
    @SubscribeEvent
    public static void modelRegistry(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(Enderthing.KEY_CHEST_BLOCK_ENTITY.get(), EnderKeyChestRenderer::new);
    }

    @SubscribeEvent
    public static void itemColors(RegisterColorHandlersEvent.Item event)
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
                Enderthing.KEY.get(), Enderthing.LOCK.get(), Enderthing.PACK.get());
    }
}
