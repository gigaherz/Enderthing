package gigaherz.enderthing.items;

import gigaherz.enderthing.KeyUtils;
import gigaherz.enderthing.gui.Containers;
import gigaherz.enderthing.util.ILongAccessor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class EnderPackItem extends EnderthingItem
{
    public EnderPackItem(Properties properties)
    {
        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(new TranslationTextComponent("tooltip.enderthing.ender_pack.right_click").applyTextStyle(TextFormatting.ITALIC));

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        if (context.getHand() != Hand.MAIN_HAND)
            return ActionResultType.PASS;

        World world = context.getWorld();
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

        openPackGui(player, id, isPrivate(stack));

        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand)
    {
        ItemStack stack = playerIn.getHeldItem(hand);

        if (hand != Hand.MAIN_HAND)
            return ActionResult.newResult(ActionResultType.PASS, stack);

        if (worldIn.isRemote)
            return ActionResult.newResult(ActionResultType.SUCCESS, stack);

        long id = KeyUtils.getKey(stack);

        if (id < 0)
        {
            openPasscodeScreen(playerIn, stack);
            return ActionResult.newResult(ActionResultType.SUCCESS, stack);
        }

        openPackGui(playerIn, id, isPrivate(stack));

        return ActionResult.newResult(ActionResultType.SUCCESS, stack);
    }

    public void openPackGui(PlayerEntity playerIn, long id, boolean priv)
    {
        if (playerIn instanceof ServerPlayerEntity)
        {
            Containers.openItemGui(
                    (ServerPlayerEntity) playerIn,
                    priv,
                    playerIn.inventory.currentItem,
                    id, null, null);
        }
    }
}
