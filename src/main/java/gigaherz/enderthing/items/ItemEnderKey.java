package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.gui.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemEnderKey extends ItemEnderthing
{
    public ItemEnderKey(boolean isprivate, Properties properties)
    {
        super(isprivate, properties);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(new TextComponentTranslation("tooltip." + Enderthing.MODID + ".ender_key.rightClick").applyTextStyle(TextFormatting.ITALIC));

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public EnumActionResult onItemUse(ItemUseContext context)
    {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        EntityPlayer player = context.getPlayer();
        ItemStack stack = context.getItem();

        if (world.isRemote)
            return EnumActionResult.SUCCESS;

        IBlockState state = world.getBlockState(pos);

        Block b = state.getBlock();
        if (b != Blocks.ENDER_CHEST && b != Enderthing.enderKeyChest)
            return EnumActionResult.PASS;

        if (player instanceof EntityPlayerMP)
            GuiHandler.openKeyGui(pos, (EntityPlayerMP) player, Enderthing.getIdFromItem(stack), isPrivate());

        return EnumActionResult.SUCCESS;
    }
}