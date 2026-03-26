package dev.gigaherz.enderthing.client;

import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import dev.gigaherz.enderthing.gui.KeyScreen;
import dev.gigaherz.enderthing.gui.PasscodeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

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

    private static final SpriteId BASE_PUBLIC = new SpriteId(TextureAtlas.LOCATION_ITEMS, Enderthing.location("item/lock_public"));
    private static final SpriteId BASE_PRIVATE = new SpriteId(TextureAtlas.LOCATION_ITEMS, Enderthing.location("item/lock_private"));
    private static final SpriteId BASE_BOUND = new SpriteId(TextureAtlas.LOCATION_ITEMS, Enderthing.location("item/lock_bound"));
    private static final SpriteId LAYER1 = new SpriteId(TextureAtlas.LOCATION_ITEMS, Enderthing.location("item/lock_layer1"));
    private static final SpriteId LAYER2 = new SpriteId(TextureAtlas.LOCATION_ITEMS, Enderthing.location("item/lock_layer2"));
    private static final SpriteId LAYER3 = new SpriteId(TextureAtlas.LOCATION_ITEMS, Enderthing.location("item/lock_layer3"));

    @SubscribeEvent
    public static void specialModels(RegisterItemDecorationsEvent event)
    {
        event.register(Enderthing.KEY_CHEST.get(), (guiGraphics, font, stack, xOffset, yOffset) -> {
            var key = KeyUtils.getKey(stack);
            var priv = KeyUtils.isPrivate(stack);
            var bound = KeyUtils.getBound(stack);

            var baseMat = (priv ? (bound != null ? BASE_BOUND : BASE_PRIVATE) : BASE_PUBLIC);

            var baseSprite = Minecraft.getInstance().getAtlasManager().get(baseMat);
            var layer1Sprite = Minecraft.getInstance().getAtlasManager().get(LAYER1);
            var layer2Sprite = Minecraft.getInstance().getAtlasManager().get(LAYER2);
            var layer3Sprite = Minecraft.getInstance().getAtlasManager().get(LAYER3);

            xOffset += 2;
            yOffset -= 7;

            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, baseSprite, xOffset, yOffset, 16, 16, -1);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, layer1Sprite, xOffset, yOffset, 16, 16, key < 0 ? 0xFF000000 : KeyColor.getLayerColor(key, 1));
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, layer2Sprite, xOffset, yOffset, 16, 16, key < 0 ? 0xFF000000 : KeyColor.getLayerColor(key, 2));
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, layer3Sprite, xOffset, yOffset, 16, 16, key < 0 ? 0xFF000000 : KeyColor.getLayerColor(key, 3));

            return false;
        });
    }
}
