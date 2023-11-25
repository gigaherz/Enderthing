package dev.gigaherz.enderthing.items;

import dev.gigaherz.enderthing.gui.Containers;
import dev.gigaherz.enderthing.util.ILongAccessor;
import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.KeyUtils;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class EnderthingItem extends Item implements KeyUtils.IKeyHolder
{
    public EnderthingItem(Properties properties)
    {
        super(properties);
    }

    public void fillItemCategory(CreativeModeTab.Output output)
    {
        output.accept(new ItemStack(this), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        output.accept(KeyUtils.setPrivate(new ItemStack(this), true), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        output.accept(KeyUtils.setBound(KeyUtils.setPrivate(new ItemStack(this), true), Util.NIL_UUID), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
    }

    @Override
    public Optional<CompoundTag> findHolderTag(ItemStack stack)
    {
        return Optional.ofNullable(stack.getTag());
    }

    @Override
    public CompoundTag getOrCreateHolderTag(ItemStack stack)
    {
        return stack.getOrCreateTag();
    }

    protected void openPasscodeScreen(Player playerIn, ItemStack stack)
    {
        Containers.openPasscodeScreen((ServerPlayer) playerIn, new ILongAccessor()
        {
            @Override
            public long get()
            {
                return getKey(stack);
            }

            @Override
            public void set(long value)
            {
                setKey(stack, value);
            }
        }, stack.copy());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        Enderthing.Client.addStandardInformation(stack, tooltip);
    }
}
