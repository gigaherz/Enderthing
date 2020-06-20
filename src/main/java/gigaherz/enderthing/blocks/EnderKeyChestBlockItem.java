package gigaherz.enderthing.blocks;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.KeyUtils;
import gigaherz.enderthing.client.ClientEvents;
import gigaherz.enderthing.gui.Containers;
import gigaherz.enderthing.util.ILongAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EnderKeyChestBlockItem extends BlockItem implements KeyUtils.IBindableKeyHolder
{
    public EnderKeyChestBlockItem(Block block, Properties properties)
    {
        super(block, properties.setISTER(() -> ClientEvents::getKeyChestRenderer));
    }

    @Override
    public Optional<CompoundNBT> findHolderTag(ItemStack stack)
    {
        CompoundNBT tag = stack.getTag();
        if (tag == null) return Optional.empty();
        if (!tag.contains("BlockEntityTag", Constants.NBT.TAG_COMPOUND))
            return Optional.empty();
        return Optional.of(tag.getCompound("BlockEntityTag"));
    }

    @Override
    public CompoundNBT getOrCreateHolderTag(ItemStack stack)
    {
        return stack.getOrCreateChildTag("BlockEntityTag");
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
    {
        // Don't show in creative menu
        //super.fillItemGroup(group, items);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(new TranslationTextComponent("tooltip.enderthing.ender_key_chest.right_click").applyTextStyle(TextFormatting.ITALIC));

        Enderthing.Client.addStandardInformation(stack, tooltip);
    }

    private void openPasscodeScreen(PlayerEntity playerIn, ItemStack stack)
    {
        Containers.openPasscodeScreen((ServerPlayerEntity) playerIn, new ILongAccessor()
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
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand)
    {
        ItemStack stack = playerIn.getHeldItem(hand);

        long oldId = getKey(stack);

        if (oldId < 0)
        {
            if (!worldIn.isRemote)
                openPasscodeScreen(playerIn, stack);
            return ActionResult.resultSuccess(stack);
        }

        if (playerIn.isSneaking())
        {
            ItemStack oldStack = KeyUtils.getLock(oldId, isPrivate(stack));

            if (!playerIn.inventory.addItemStackToInventory(oldStack))
            {
                playerIn.dropItem(oldStack, false);
            }

            if (stack.getCount() > 1)
            {
                ItemStack newStack = new ItemStack(Blocks.ENDER_CHEST);
                if (!playerIn.inventory.addItemStackToInventory(newStack))
                {
                    playerIn.dropItem(newStack, false);
                }

                stack.grow(-1);
                return ActionResult.resultSuccess(stack);
            }

            return ActionResult.resultSuccess(new ItemStack(Blocks.ENDER_CHEST));
        }

        return super.onItemRightClick(worldIn, playerIn, hand);
    }
}
