package gigaherz.enderthing;

import com.google.common.primitives.Longs;
import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import joptsimple.internal.Strings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

public class KeyUtils
{
    public static long getItemKey(ItemStack stack)
    {
        CompoundNBT tag = stack.getTag();
        if (tag != null)
        {
            return tag.getLong("Key");
        }

        return -1;
    }

    public static void setItemKey(ItemStack stack, long key)
    {
        stack.getOrCreateTag().putLong("Key", key);
    }

    public static long getBlockKey(ItemStack stack)
    {
        CompoundNBT tag = stack.getTag();
        if (tag != null)
        {
            CompoundNBT etag = tag.getCompound("BlockEntityTag");
            if (etag != null)
            {
                return etag.getLong("InventoryId");
            }
        }

        return -1;
    }

    public static void setBlockKey(ItemStack stack, long key)
    {
        stack.getOrCreateChildTag("BlockEntityTag").putLong("InventoryId", key);
    }

    public static long getKey(ItemStack stack)
    {
        if (stack.getItem() instanceof BlockItem)
            return getBlockKey(stack);
        return getItemKey(stack);
    }

    public static void setKey(ItemStack stack, long key)
    {
        if (stack.getItem() instanceof BlockItem)
            setBlockKey(stack, key);
        else
            setItemKey(stack, key);
    }

    public static long getKey(TileEntity te)
    {
        if (te instanceof EnderKeyChestTileEntity)
        {
            return ((EnderKeyChestTileEntity) te).getKey();
        }

        return -1;
    }

    public static ItemStack getItem(IItemProvider itemProvider, long key)
    {
        ItemStack stack = new ItemStack(itemProvider, 1);

        CompoundNBT tag = new CompoundNBT();
        tag.putLong("Key", key);

        stack.setTag(tag);

        return stack;
    }

    public static ItemStack getLock(long key, boolean priv)
    {
        return getItem(priv ? Enderthing.enderLockPrivate : Enderthing.enderLock, key);
    }

    public static ItemStack getBlockItem(long id, boolean priv)
    {
        ItemStack stack = new ItemStack(priv ? Enderthing.enderKeyChestPrivate : Enderthing.enderKeyChest, 1);

        CompoundNBT tag = new CompoundNBT();
        CompoundNBT etag = new CompoundNBT();
        etag.putLong("Key", id);
        tag.put("BlockEntityTag", etag);

        stack.setTag(tag);

        return stack;
    }

    @Nullable
    public static String queryNameFromUUID(UUID uuid)
    {
        MinecraftServer svr = ServerLifecycleHooks.getCurrentServer();
        if (svr == null)
            return null;
        PlayerList playerList = svr.getPlayerList();
        if (playerList == null)
            return null;
        PlayerEntity player = playerList.getPlayerByUUID(uuid);
        if (player != null)
            return player.getName().getString();
        return null;
    }

    public static long getKeyFromPasscode(String passcode)
    {
        if (Strings.isNullOrEmpty(passcode))
            return 0;

        if (!passcode.startsWith("-"))
        {
            try
            {
                return Long.parseLong(passcode);
            }
            catch (NumberFormatException e)
            {
                // ignore
            }
        }

        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(passcode.getBytes());
            return Longs.fromByteArray(md.digest())&0x7fffffffffffffffL;
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException)
        {
            throw new RuntimeException(noSuchAlgorithmException);
        }
    }

    public static long getKeyFromPasscode(List<ItemStack> passcode)
    {
        if (passcode.size() == 0)
            return 0;

        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            for(ItemStack st : passcode)
            {
                CompoundNBT tag = new CompoundNBT();
                st.write(tag);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                CompressedStreamTools.write(tag, new DataOutputStream(bytes));
                md.update(bytes.toByteArray());
            }
            return Longs.fromByteArray(md.digest())&0x7fffffffffffffffL;
        }
        catch (NoSuchAlgorithmException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }

}
