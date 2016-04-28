package gigaherz.enderthing.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.gui.GuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import java.util.List;

public class ItemEnderPack extends ItemEnderthing
{
    public ItemEnderPack(String name)
    {
        super(name);
        setMaxStackSize(1);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> information, boolean advanced)
    {
        information.add(ChatFormatting.ITALIC + I18n.translateToLocal("tooltip." + Enderthing.MODID + ".enderPack.rightClick"));

        super.addInformation(stack, player, information, advanced);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
            return EnumActionResult.SUCCESS;

        int id = getId(stack) | (stack.getMetadata() != 0 ? GuiHandler.GUI_PACK_PRIVATE : GuiHandler.GUI_PACK);

        //noinspection PointlessBitwiseExpression
        playerIn.openGui(Enderthing.instance, id | GuiHandler.GUI_KEY, worldIn, playerIn.inventory.currentItem, 0, 0);
        playerIn.addStat(StatList.ENDERCHEST_OPENED);

        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if (worldIn.isRemote)
            return ActionResult.newResult(EnumActionResult.SUCCESS, stack);

        int id = getId(stack) | (stack.getMetadata() != 0 ? GuiHandler.GUI_PACK_PRIVATE : GuiHandler.GUI_PACK);

        //noinspection PointlessBitwiseExpression
        playerIn.openGui(Enderthing.instance, id | GuiHandler.GUI_KEY, worldIn, playerIn.inventory.currentItem, 0, 0);
        playerIn.addStat(StatList.ENDERCHEST_OPENED);

        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }
}
