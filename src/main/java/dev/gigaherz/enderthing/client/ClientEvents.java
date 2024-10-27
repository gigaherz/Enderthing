package dev.gigaherz.enderthing.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import dev.gigaherz.enderthing.blocks.EnderKeyChestRenderer;
import dev.gigaherz.enderthing.gui.KeyScreen;
import dev.gigaherz.enderthing.gui.PasscodeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.util.Lazy;

@EventBusSubscriber(value = Dist.CLIENT, modid = Enderthing.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ClientEvents
{
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {

            ItemProperties.register(Enderthing.KEY.get(), Enderthing.location("private"), (stack, world, entity, i) -> KeyUtils.isPrivate(stack) ? 1.0f : 0.0f);
            ItemProperties.register(Enderthing.LOCK.get(), Enderthing.location("private"), (stack, world, entity, i) -> KeyUtils.isPrivate(stack) ? 1.0f : 0.0f);
            ItemProperties.register(Enderthing.PACK.get(), Enderthing.location("private"), (stack, world, entity, i) -> KeyUtils.isPrivate(stack) ? 1.0f : 0.0f);
            ItemProperties.register(Enderthing.KEY_CHEST_ITEM.get(), Enderthing.location("private"), (stack, world, entity, i) -> KeyUtils.isPrivate(stack) ? 1.0f : 0.0f);
            ItemProperties.register(Enderthing.LOCK.get(), Enderthing.location("bound"), (stack, world, entity, i) -> KeyUtils.isPrivate(stack) && KeyUtils.isBound(stack) ? 1.0f : 0.0f);
        });
    }

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
    public static void itemColors(RegisterColorHandlersEvent.Item event)
    {
        event.getItemColors().register((stack, layer) -> {
                    if (layer < 1 || layer > 3)
                        return -1;

                    long id = KeyUtils.getKey(stack);
                    if (id < 0)
                        return 0xFF000000;

                    int r = ((int) (id >>> ((layer - 1) * 21)) & 0x7f) * 255 / 127;
                    int g = ((int) (id >>> ((layer - 1) * 21 + 7)) & 0x7f) * 255 / 127;
                    int b = ((int) (id >>> ((layer - 1) * 21 + 14)) & 0x7f) * 255 / 127;

                    return 0xFF000000 | (r << 16) | (g << 8) | (b);
                },
                Enderthing.KEY.get(), Enderthing.LOCK.get(), Enderthing.PACK.get());
    }

    @SubscribeEvent
    public static void clientExtensions(RegisterClientExtensionsEvent event)
    {
        event.registerItem(new IClientItemExtensions()
        {
            static final Lazy<BlockEntityWithoutLevelRenderer> renderer = Lazy.of(() -> new BlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels())
            {
                final EnderKeyChestBlockEntity defaultChest = new EnderKeyChestBlockEntity(BlockPos.ZERO, Enderthing.KEY_CHEST.get().defaultBlockState());

                @Override
                public void renderByItem(ItemStack itemStackIn, ItemDisplayContext transformType, PoseStack matrixStackIn,
                                         MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
                {
                    EnderKeyChestRenderer.INSTANCE.renderFromItem(itemStackIn, defaultChest, transformType, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
                }
            });

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer()
            {
                return renderer.get();
            }
        }, Enderthing.KEY_CHEST_ITEM);
    }
}
