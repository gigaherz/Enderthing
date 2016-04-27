package gigaherz.enderthing.network;

import gigaherz.enderthing.blocks.TileEnderKeyChest;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdatePlayersUsing
        implements IMessage
{
    public BlockPos tilePosition;
    public int field;
    public int value;

    public UpdatePlayersUsing()
    {
    }

    public UpdatePlayersUsing(BlockPos tilePosition, int field, int value)
    {
        this.tilePosition = tilePosition;
        this.field = field;
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        tilePosition = new BlockPos(
                buf.readInt(),
                buf.readInt(),
                buf.readInt()
        );
        field = buf.readByte();
        value = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(tilePosition.getX());
        buf.writeInt(tilePosition.getY());
        buf.writeInt(tilePosition.getZ());
        buf.writeByte(field);
        buf.writeInt(value);
    }

    public static class Handler implements IMessageHandler<UpdatePlayersUsing, IMessage>
    {
        @Override
        public IMessage onMessage(UpdatePlayersUsing message, MessageContext ctx)
        {
            final BlockPos pos = message.tilePosition;
            final int field = message.field;
            final int value = message.value;

            Minecraft.getMinecraft().addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(pos);
                    if (te instanceof TileEnderKeyChest)
                    {
                        TileEnderKeyChest chest = (TileEnderKeyChest) te;
                        chest.receiveUpdate(field, value);
                    }
                }
            });

            return null; // no response in this case
        }
    }
}
