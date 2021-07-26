package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.KeyUtils;
import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import gigaherz.enderthing.gui.Containers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.TileEntity;
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

import net.minecraft.item.Item.Properties;

public class EnderKeyItem extends EnderthingItem
{
    public EnderKeyItem(Properties properties)
    {
        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(new TranslationTextComponent("tooltip.enderthing.ender_key.right_click").mergeStyle(TextFormatting.ITALIC));

        super.addInformation(stack, worldIn, tooltip, flagIn);
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
            return ActionResult.resultSuccess(stack);
        }

        return ActionResult.resultPass(stack);
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

        if (id < 0 || player.isSneaking())
        {
            openPasscodeScreen(player, stack);
            return ActionResultType.SUCCESS;
        }

        BlockState state = world.getBlockState(pos);

        Block b = state.getBlock();
        if (b != Blocks.ENDER_CHEST && b != Enderthing.KEY_CHEST)
            return ActionResultType.PASS;

        if (player instanceof ServerPlayerEntity)
        {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof EnderKeyChestTileEntity)
                Containers.openItemGui((ServerPlayerEntity) player, isPrivate(stack), -1, id, null, (EnderKeyChestTileEntity)te);
            else if (te instanceof EnderChestTileEntity)
                Containers.openItemGui((ServerPlayerEntity) player, isPrivate(stack), -1, id, null, (EnderChestTileEntity)te);
        }

        return ActionResultType.SUCCESS;
    }
}