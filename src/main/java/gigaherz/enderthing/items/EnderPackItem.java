package gigaherz.enderthing.items;

import gigaherz.enderthing.KeyUtils;
import gigaherz.enderthing.gui.Containers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
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
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        tooltip.add(new TranslatableComponent("tooltip.enderthing.ender_pack.right_click").withStyle(ChatFormatting.ITALIC));

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        if (context.getHand() != InteractionHand.MAIN_HAND)
            return InteractionResult.PASS;

        Level world = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (world.isClientSide)
            return InteractionResult.SUCCESS;

        long id = KeyUtils.getKey(stack);

        if (id < 0)
        {
            openPasscodeScreen(player, stack);
            return InteractionResult.SUCCESS;
        }

        openPackGui(player, id, isPrivate(stack));

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand)
    {
        ItemStack stack = playerIn.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND)
            return InteractionResultHolder.pass(stack);

        if (worldIn.isClientSide)
            return InteractionResultHolder.success(stack);

        long id = KeyUtils.getKey(stack);

        if (id < 0)
        {
            openPasscodeScreen(playerIn, stack);
            return InteractionResultHolder.success(stack);
        }

        openPackGui(playerIn, id, isPrivate(stack));

        return InteractionResultHolder.success(stack);
    }

    public void openPackGui(Player playerIn, long id, boolean priv)
    {
        if (playerIn instanceof ServerPlayer)
        {
            Containers.openItemGui(
                    (ServerPlayer) playerIn,
                    priv,
                    playerIn.getInventory().selected,
                    id, null);
        }
    }
}
