package dev.gigaherz.enderthing.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import dev.gigaherz.enderthing.KeyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class EnderKeyChestSpecialRenderer implements SpecialModelRenderer<ItemStack>
{
    private final ChestModel model;
    private final Material material;
    private final MaterialSet materials;

    public EnderKeyChestSpecialRenderer(ChestModel model, Material material, MaterialSet materials)
    {
        this.model = model;
        this.material = material;
        this.materials = materials;
    }

    @Override
    public void submit(@Nullable ItemStack lock, ItemDisplayContext displayContext, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int packedOverlay, boolean hasFoil, int outlineColor)
    {
        collector.submitModel(
                this.model,
                0.0f,
                poseStack,
                this.material.renderType(RenderTypes::entitySolid),
                packedLight,
                packedOverlay,
                -1,
                this.materials.get(this.material),
                outlineColor,
                null
        );
        if (lock != null)
        {
            final ItemStackRenderState lockState = new ItemStackRenderState();

            var itemModelResolver = Minecraft.getInstance().getItemModelResolver();

            itemModelResolver.updateForTopItem(lockState, lock, ItemDisplayContext.FIXED, null, null, 0);

            EnderKeyChestRenderer.renderLockOnChest(lockState, poseStack, collector, LightTexture.FULL_BRIGHT, 0);
        }
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer)
    {
        PoseStack posestack = new PoseStack();
        this.model.setupAnim(0.0f);
        this.model.root().getExtentsForGui(posestack, consumer);
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
        public @org.jspecify.annotations.Nullable SpecialModelRenderer<?> bake(BakingContext context)
        {
            ChestModel model = new ChestModel(context.entityModelSet().bakeLayer(ModelLayers.CHEST));
            Material material = Sheets.ENDER_CHEST_LOCATION;
            return new EnderKeyChestSpecialRenderer(model, material, context.materials());
        }
    }
}
