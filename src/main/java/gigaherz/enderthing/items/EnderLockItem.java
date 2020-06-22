package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.KeyUtils;
import gigaherz.enderthing.blocks.EnderKeyChestBlock;
import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class EnderLockItem extends EnderthingItem implements KeyUtils.IBindableKeyHolder
{
    public EnderLockItem(Properties properties)
    {
        super(properties);
        this.addPropertyOverride(new ResourceLocation("bound"),
                (stack, world, entity) -> isPrivate(stack) && isBound(stack) ? 1.0f : 0.0f);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(new TranslationTextComponent("tooltip.enderthing.ender_lock.right_click").applyTextStyle(TextFormatting.ITALIC));

        if (isBound(stack))
            tooltip.add(new TranslationTextComponent("tooltip.enderthing.ender_lock.bound", getBoundStr(stack)));

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
        World worldIn = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getItem();

        if (worldIn.isRemote)
            return ActionResultType.SUCCESS;

        BlockState state = worldIn.getBlockState(pos);

        long id = KeyUtils.getKey(stack);

        if (id < 0)
        {
            openPasscodeScreen(player, stack);
            return ActionResultType.SUCCESS;
        }

        Block b = state.getBlock();

        TileEntity te = worldIn.getTileEntity(pos);

        if (b == Blocks.ENDER_CHEST)
        {
            return replaceWithKeyChest(worldIn, pos, stack, state, id, true, player);
        }

        if (b instanceof EnderKeyChestBlock)
        {
            boolean oldPrivate = false;
            if (te instanceof EnderKeyChestTileEntity)
            {
                EnderKeyChestTileEntity chest = (EnderKeyChestTileEntity) te;
                long oldId = chest.getKey();
                oldPrivate = chest.isPrivate();
                UUID bound = chest.getPlayerBound();
                ItemStack oldStack = KeyUtils.getLock(oldId, oldPrivate, bound);

                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), oldStack);
            }

            boolean newPrivate = isPrivate(stack);
            return replaceWithKeyChest(worldIn, pos, stack, state, id, oldPrivate != newPrivate, player);
        }

        return ActionResultType.PASS;
    }

    private ActionResultType replaceWithKeyChest(World worldIn, BlockPos pos, ItemStack stack, BlockState state, long id, boolean replace, PlayerEntity player)
    {
        if (replace) setKeyChest(worldIn, pos, state, stack);

        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof EnderKeyChestTileEntity)
        {
            EnderKeyChestTileEntity chest = (EnderKeyChestTileEntity) te;
            chest.setKey(id);
            chest.setPrivate(isPrivate(stack));
            if (isPrivate(stack) && isBound(stack))
                chest.bindToPlayer(getBound(stack));
        }

        if (!player.isCreative())
            stack.grow(-1);

        return ActionResultType.SUCCESS;
    }

    private void setKeyChest(World worldIn, BlockPos pos, BlockState state, ItemStack stack)
    {
        worldIn.setBlockState(pos, Enderthing.KEY_CHEST.getDefaultState()
                    .with(EnderKeyChestBlock.WATERLOGGED, state.get(EnderChestBlock.WATERLOGGED))
                    .with(EnderKeyChestBlock.FACING, state.get(EnderChestBlock.FACING)));
    }
}