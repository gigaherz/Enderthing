package dev.gigaherz.enderthing.gui;

import net.minecraft.world.entity.player.Player;

public interface IContainerInteraction
{
    boolean canBeUsed(Player player);

    void openChest(Player player);

    void closeChest(Player player);
}
