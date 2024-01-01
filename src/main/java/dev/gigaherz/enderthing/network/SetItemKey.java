package dev.gigaherz.enderthing.network;

import dev.gigaherz.enderthing.gui.PasscodeContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class SetItemKey implements CustomPacketPayload
{
    public static final ResourceLocation ID = new ResourceLocation("signbutton","update_spell_sequence");

    public final long key;

    public SetItemKey(long key)
    {
        this.key = key;
    }

    public SetItemKey(FriendlyByteBuf buffer)
    {
        this.key = buffer.readLong();
    }

    public void write(FriendlyByteBuf buffer)
    {
        buffer.writeLong(key);
    }

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    public void handle(PlayPayloadContext context)
    {
        if (key >= 0)
        {
            context.workHandler().execute(() -> {
                Player sender = context.player().orElseThrow();
                if (sender.containerMenu instanceof PasscodeContainer)
                {
                    ((PasscodeContainer) sender.containerMenu).keyHolder.set(key);
                    sender.closeContainer();
                    sender.displayClientMessage(Component.translatable("text.enderthing.key_change", key), true);
                }
            });
        }
    }
}
