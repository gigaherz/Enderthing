package gigaherz.enderthing.network;

import gigaherz.enderthing.blocks.TileEnderKeyChest;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdatePlayersUsing
{
    public BlockPos tilePosition;
    public int field;
    public int value;

    public UpdatePlayersUsing(BlockPos tilePosition, int field, int value)
    {
        this.tilePosition = tilePosition;
        this.field = field;
        this.value = value;
    }

    public UpdatePlayersUsing(PacketBuffer buf)
    {
        tilePosition = buf.readBlockPos();
        field = buf.readByte();
        value = buf.readInt();
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeBlockPos(tilePosition);
        buf.writeByte(field);
        buf.writeInt(value);
    }

    public void handle(Supplier<NetworkEvent.Context> context)
    {
        final BlockPos pos = this.tilePosition;
        final int field = this.field;
        final int value = this.value;

        Minecraft.getInstance().addScheduledTask(() -> {
            TileEntity te = Minecraft.getInstance().world.getTileEntity(pos);
            if (te instanceof TileEnderKeyChest)
            {
                TileEnderKeyChest chest = (TileEnderKeyChest) te;
                chest.receiveUpdate(field, value);
            }
        });
    }
}
