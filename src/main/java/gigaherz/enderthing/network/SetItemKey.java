package gigaherz.enderthing.network;

import gigaherz.enderthing.gui.PasscodeContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class SetItemKey
{
    public final long key;

    public SetItemKey(long key)
    {
        this.key = key;
    }

    public SetItemKey(FriendlyByteBuf buffer)
    {
        this.key = buffer.readLong();
    }

    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeLong(key);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx)
    {
        if (key >= 0)
        {
            ctx.get().enqueueWork(() -> {
                Player sender = ctx.get().getSender();
                if (sender != null && sender.containerMenu instanceof PasscodeContainer)
                {
                    ((PasscodeContainer) sender.containerMenu).keyHolder.set(key);
                    sender.closeContainer();
                    sender.displayClientMessage(new TranslatableComponent("text.enderthing.key_change", key), true);
                }
            });
        }
        return true;
    }
}
