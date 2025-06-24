package dev.gigaherz.enderthing.client;

import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.gui.KeyScreen;
import dev.gigaherz.enderthing.gui.PasscodeScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = Enderthing.MODID)
public class ClientEvents
{
    @SubscribeEvent
    public static void clientSetup(RegisterMenuScreensEvent event)
    {
        event.register(Enderthing.KEY_CONTAINER.get(), KeyScreen::new);
        event.register(Enderthing.PASSCODE_CONTAINER.get(), PasscodeScreen::new);
    }

    @SubscribeEvent
    public static void modelRegistry(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(Enderthing.KEY_CHEST_BLOCK_ENTITY.get(), EnderKeyChestRenderer::new);
    }

    @SubscribeEvent
    public static void itemColors(RegisterColorHandlersEvent.ItemTintSources event)
    {
        event.register(Enderthing.location("key_color"), KeyColor.CODEC);
    }

    @SubscribeEvent
    public static void specialModels(RegisterSpecialModelRendererEvent event)
    {
        event.register(Enderthing.location("key_chest"), EnderKeyChestSpecialRenderer.Unbaked.CODEC);
    }
}
