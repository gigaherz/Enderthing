package gigaherz.enderthing;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public interface IModProxy
{
    void preInit();

    void init();

    String queryNameFromUUID(ItemStack stack, UUID uuid);
}
