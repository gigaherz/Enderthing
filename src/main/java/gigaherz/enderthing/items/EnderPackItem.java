package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
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
    public EnderPackItem(boolean isprivate, Properties properties)
    {
        super(isprivate, properties);
        //setMaxStackSize(1);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(new TranslationTextComponent("tooltip." + Enderthing.MODID + ".ender_pack.right_click").applyTextStyle(TextFormatting.ITALIC));

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

        openPackGui(player, id);

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

        openPackGui(playerIn, id);

        return ActionResult.newResult(ActionResultType.SUCCESS, stack);
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
        });
    }

    public void openPackGui(PlayerEntity playerIn, long id)
    {
        if (playerIn instanceof ServerPlayerEntity)
        {
            Containers.openItemGui(
                    (ServerPlayerEntity) playerIn,
                    isPrivate(),
                    playerIn.inventory.currentItem,
                    id, null, null);
        }
    }
}
