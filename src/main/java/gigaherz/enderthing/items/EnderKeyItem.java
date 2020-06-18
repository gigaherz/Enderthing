package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.KeyUtils;
import gigaherz.enderthing.gui.Containers;
import gigaherz.enderthing.util.ILongAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class EnderKeyItem extends EnderthingItem
{
    public EnderKeyItem(boolean isprivate, Properties properties)
    {
        super(isprivate, properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(new TranslationTextComponent("tooltip.enderthing.ender_key.right_click").applyTextStyle(TextFormatting.ITALIC));

        super.addInformation(stack, worldIn, tooltip, flagIn);
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
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn)
    {
        ItemStack stack = playerIn.getHeldItem(handIn);

        long id = KeyUtils.getKey(stack);

        if (id < 0)
        {
            if (!worldIn.isRemote)
                openPasscodeScreen(playerIn, stack);
            return ActionResult.newResult(ActionResultType.SUCCESS, stack);
        }

        return ActionResult.newResult(ActionResultType.PASS, stack);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getItem();

        if (world.isRemote)
            return ActionResultType.SUCCESS;

        long id = KeyUtils.getKey(stack);

        if (id < 0)
        {
            openPasscodeScreen(player, stack);
            return ActionResultType.SUCCESS;
        }

        BlockState state = world.getBlockState(pos);

        Block b = state.getBlock();
        if (b != Blocks.ENDER_CHEST && b != Enderthing.enderKeyChest)
            return ActionResultType.PASS;

        if (player instanceof ServerPlayerEntity)
            Containers.openItemGui((ServerPlayerEntity) player, isPrivate(), -1, id, null, world.getTileEntity(pos));

        return ActionResultType.SUCCESS;
    }
}