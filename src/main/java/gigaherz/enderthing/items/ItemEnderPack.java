package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.gui.GuiHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemEnderPack extends ItemEnderthing
{
    public ItemEnderPack(boolean isprivate, Properties properties)
    {
        super(isprivate, properties);
        //setMaxStackSize(1);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(new TextComponentTranslation("tooltip." + Enderthing.MODID + ".ender_pack.rightClick").applyTextStyle(TextFormatting.ITALIC));

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public EnumActionResult onItemUse(ItemUseContext context)
    {
        World world = context.getWorld();
        EntityPlayer player = context.getPlayer();
        ItemStack stack = context.getItem();

        if (world.isRemote)
            return EnumActionResult.SUCCESS;

        openPackGui(player, stack);

        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        ItemStack stack = playerIn.getHeldItem(hand);

        if (worldIn.isRemote)
            return ActionResult.newResult(EnumActionResult.SUCCESS, stack);

        openPackGui(playerIn, stack);

        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    public void openPackGui(EntityPlayer playerIn, ItemStack stack)
    {
        if (playerIn instanceof EntityPlayerMP)
        {
            GuiHandler.openPackGui(Enderthing.getKey(stack),
                    (EntityPlayerMP) playerIn,
                    isPrivate(),
                    playerIn.inventory.currentItem);
        }
    }
}
