package gigaherz.enderthing.items;

import gigaherz.enderthing.Enderthing;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemEnderthing extends Item
{
    private final boolean isPrivate;

    public boolean isPrivate()
    {
        return isPrivate;
    }

    public ItemEnderthing(boolean isPrivate, Properties properties)
    {
        super(properties);
        this.isPrivate = isPrivate;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        Enderthing.Client.addStandardInformation(stack, tooltip, flagIn, isPrivate);
    }
}
