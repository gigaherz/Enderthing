package dev.gigaherz.enderthing.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import dev.gigaherz.enderthing.blocks.EnderKeyChestRenderer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class KeyChestSpecialRenderer implements SpecialModelRenderer<ItemStack>
{
    private final EnderKeyChestBlockEntity defaultChest = new EnderKeyChestBlockEntity(BlockPos.ZERO, Enderthing.KEY_CHEST.get().defaultBlockState());

    @Override
    public void render(@Nullable ItemStack lock, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean hasFoilType)
    {
        EnderKeyChestRenderer.INSTANCE.renderFromItem(lock, defaultChest, poseStack, bufferSource, packedLight, packedOverlay);
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
            return new KeyChestSpecialRenderer();
        }
    }
}
