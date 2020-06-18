package gigaherz.enderthing.network;

import gigaherz.enderthing.gui.PasscodeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SetItemKey
{
    public final long key;

    public SetItemKey(long key)
    {
        this.key = key;
    }

    public SetItemKey(PacketBuffer buffer)
    {
        this.key = buffer.readLong();
    }

    public void encode(PacketBuffer buffer)
    {
        buffer.writeLong(key);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx)
    {
        if (key >= 0)
        {
            ctx.get().enqueueWork(() -> {
                PlayerEntity sender = ctx.get().getSender();
                if (sender != null && sender.openContainer instanceof PasscodeContainer)
                {
                    ((PasscodeContainer) sender.openContainer).keyHolder.set(key);
                    sender.closeScreen();
                    sender.sendStatusMessage(new TranslationTextComponent("text.enderthing.key_change", key), true);
                }
            });
        }
        return true;
    }
}
