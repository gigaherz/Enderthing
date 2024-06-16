package dev.gigaherz.enderthing.network;

import dev.gigaherz.enderthing.Enderthing;
import dev.gigaherz.enderthing.gui.PasscodeContainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetItemKey(long key) implements CustomPacketPayload
{
    public static final ResourceLocation ID = Enderthing.location("key_change");
    public static final Type<SetItemKey> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, SetItemKey> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, SetItemKey::key,
            SetItemKey::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public void handle(IPayloadContext context)
    {
        if (key >= 0)
        {
            context.enqueueWork(() -> {
                Player sender = context.player();
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
