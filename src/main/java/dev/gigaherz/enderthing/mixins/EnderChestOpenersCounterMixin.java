package dev.gigaherz.enderthing.mixins;

import dev.gigaherz.enderthing.gui.KeyContainer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets="net.minecraft.world.level.block.entity.EnderChestBlockEntity$1")
public class EnderChestOpenersCounterMixin
{
    @Inject(method = "isOwnContainer(Lnet/minecraft/world/entity/player/Player;)Z", at=@At("HEAD"), cancellable = true)
    private void inject_isOwnContainer_allowEnderKeyMenu(Player player, CallbackInfoReturnable<Boolean> ret)
    {
        if (player.containerMenu instanceof KeyContainer kc)
            ret.setReturnValue(true);
    }
}
