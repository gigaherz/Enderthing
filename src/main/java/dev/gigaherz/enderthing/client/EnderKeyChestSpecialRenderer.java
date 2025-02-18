package dev.gigaherz.enderthing.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import dev.gigaherz.enderthing.KeyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EnderKeyChestSpecialRenderer implements SpecialModelRenderer<ItemStack>
{
    private final ChestModel model;
    private final Material material;

    public EnderKeyChestSpecialRenderer(ChestModel model, Material material)
    {
        this.model = model;
        this.material = material;
    }

    @Override
    public void render(@Nullable ItemStack lock, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean hasFoilType)
    {
        var buffer = this.material.buffer(bufferSource, RenderType::entitySolid);
        this.model.setupAnim(0);
        this.model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay);
        if (lock != null)
        {
            EnderKeyChestRenderer.renderLockOnChest(lock, Minecraft.getInstance().level, poseStack, bufferSource, packedLight, packedOverlay, 0);
        }
    }

    @Nullable
    @Override
    public ItemStack extractArgument(ItemStack stack)
    {
        return KeyUtils.getLock(KeyUtils.getKey(stack), KeyUtils.isPrivate(stack), KeyUtils.getBound(stack));
    }

    public static class Unbaked implements SpecialModelRenderer.Unbaked
    {
        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(INSTANCE);

        public Unbaked()
        {
        }

        @Override
        public MapCodec<Unbaked> type()
        {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet modelSet)
        {
            ChestModel model = new ChestModel(modelSet.bakeLayer(ModelLayers.CHEST));
            Material material = Sheets.ENDER_CHEST_LOCATION;
            return new EnderKeyChestSpecialRenderer(model, material);
        }
    }
}
