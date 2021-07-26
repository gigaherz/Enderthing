package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.KeyUtils;
import gigaherz.enderthing.gui.Containers;
import gigaherz.enderthing.util.ILongAccessor;
import joptsimple.internal.Strings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import net.minecraft.item.Item.Properties;

public class EnderthingItem extends Item implements KeyUtils.IKeyHolder
{
    public EnderthingItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
    {
        if (isInGroup(group))
        {
            items.add(new ItemStack(this));
            items.add(KeyUtils.setPrivate(new ItemStack(this), true));
        }
    }

    @Override
    public Optional<CompoundNBT> findHolderTag(ItemStack stack)
    {
        return Optional.ofNullable(stack.getTag());
    }

    @Override
    public CompoundNBT getOrCreateHolderTag(ItemStack stack)
    {
        return stack.getOrCreateTag();
    }

    protected void openPasscodeScreen(PlayerEntity playerIn, ItemStack stack)
    {
        Containers.openPasscodeScreen((ServerPlayerEntity) playerIn, new ILongAccessor()
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
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        Enderthing.Client.addStandardInformation(stack, tooltip);
    }
}
