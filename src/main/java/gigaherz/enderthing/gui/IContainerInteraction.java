package gigaherz.enderthing.gui;

import net.minecraft.world.entity.player.Player;

public interface IContainerInteraction
{
    boolean canBeUsed(Player player);
    void openChest();
    void closeChest();
}
