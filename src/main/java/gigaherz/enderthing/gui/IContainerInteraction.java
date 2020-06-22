package gigaherz.enderthing.gui;

import net.minecraft.entity.player.PlayerEntity;

public interface IContainerInteraction
{
    boolean canBeUsed(PlayerEntity player);
    void openChest();
    void closeChest();
}
