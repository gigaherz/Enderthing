package dev.gigaherz.enderthing.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import dev.gigaherz.enderthing.gui.Containers;
import dev.gigaherz.enderthing.util.ILongAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.util.NonNullLazy;
import net.minecraft.nbt.Tag;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class EnderKeyChestBlockItem extends BlockItem implements KeyUtils.IBindableKeyHolder
{
    public EnderKeyChestBlockItem(Block block, Properties properties)
    {
        super(block, properties);
    }

    @Override
    public Optional<CompoundTag> findHolderTag(ItemStack stack)
    {
        CompoundTag tag = stack.getTag();
        if (tag == null) return Optional.empty();
        if (!tag.contains("BlockEntityTag", Tag.TAG_COMPOUND))
            return Optional.empty();
        return Optional.of(tag.getCompound("BlockEntityTag"));
    }

    @Override
    public CompoundTag getOrCreateHolderTag(ItemStack stack)
    {
        return stack.getOrCreateTagElement("BlockEntityTag");
    }

    public void fillItemCategory(CreativeModeTab.Output output)
    {
        output.accept(new ItemStack(this), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        output.accept(KeyUtils.setPrivate(new ItemStack(this), true), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        output.accept(KeyUtils.setBound(KeyUtils.setPrivate(new ItemStack(this), true), Util.NIL_UUID), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        tooltip.add(Component.translatable("tooltip.enderthing.ender_key_chest.right_click").withStyle(ChatFormatting.ITALIC));

        Enderthing.Client.addStandardInformation(stack, tooltip);

        if (isBound(stack))
            tooltip.add(Component.translatable("tooltip.enderthing.ender_lock.bound", getBoundStr(stack)));

    }

    private void openPasscodeScreen(Player playerIn, ItemStack stack)
    {
        Containers.openPasscodeScreen((ServerPlayer) playerIn, new ILongAccessor()
        {
            @Override
            public long get()
            {
                return KeyUtils.getKey(stack);
            }

            @Override
            public void set(long value)
            {
                KeyUtils.setKey(stack, value);
            }
        }, stack.copy());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand)
    {
        ItemStack stack = playerIn.getItemInHand(hand);

        long oldId = getKey(stack);

        if (oldId < 0)
        {
            if (!worldIn.isClientSide)
                openPasscodeScreen(playerIn, stack);
            return InteractionResultHolder.success(stack);
        }

        if (playerIn.isShiftKeyDown())
        {
            ItemStack oldStack = KeyUtils.getLock(oldId, isPrivate(stack));

            if (!playerIn.getInventory().add(oldStack))
            {
                playerIn.drop(oldStack, false);
            }

            if (stack.getCount() > 1)
            {
                ItemStack newStack = new ItemStack(Blocks.ENDER_CHEST);
                if (!playerIn.getInventory().add(newStack))
                {
                    playerIn.drop(newStack, false);
                }

                stack.grow(-1);
                return InteractionResultHolder.success(stack);
            }

            return InteractionResultHolder.success(new ItemStack(Blocks.ENDER_CHEST));
        }

        return super.use(worldIn, playerIn, hand);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer)
    {
        consumer.accept(new IClientItemExtensions()
        {
            static final NonNullLazy<BlockEntityWithoutLevelRenderer> renderer = NonNullLazy.of(() -> new BlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels())
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
        });
    }
}
