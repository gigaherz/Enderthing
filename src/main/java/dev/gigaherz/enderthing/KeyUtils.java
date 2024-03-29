package dev.gigaherz.enderthing;

import com.google.common.primitives.Longs;
import com.mojang.logging.LogUtils;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import joptsimple.internal.Strings;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import javax.annotation.Nullable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class KeyUtils
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public interface IKeyHolder
    {
        private Item self() { return (Item)this; }

        Optional<CompoundTag> findHolderTag(ItemStack stack);

        CompoundTag getOrCreateHolderTag(ItemStack stack);

        default boolean isPrivate(ItemStack stack)
        {
            return findHolderTag(stack).map(blockTag -> blockTag.getBoolean("IsPrivate")).orElse(false);
        }

        default void setPrivate(ItemStack stack, boolean priv)
        {
            getOrCreateHolderTag(stack).putBoolean("IsPrivate", priv);
        }

        default long getKey(ItemStack stack)
        {
            return findHolderTag(stack).map(blockTag -> blockTag.contains("Key", Tag.TAG_LONG) ? blockTag.getLong("Key") : -1).orElse(-1L);
        }

        default void setKey(ItemStack stack, long key)
        {
            getOrCreateHolderTag(stack).putLong("Key", key);
        }

        default ItemStack makeStack(boolean isPrivate)
        {
            var stack = self().getDefaultInstance();
            if (isPrivate) setPrivate(stack, true);
            return stack;
        }

        default ItemStack makeStack(boolean isPrivate, long key)
        {
            var stack = self().getDefaultInstance();
            if (isPrivate) setPrivate(stack, true);
            if (key != -1) setKey(stack, key);
            return stack;
        }
    }

    public interface IBindable
    {
        Optional<CompoundTag> findHolderTag(ItemStack stack);

        CompoundTag getOrCreateHolderTag(ItemStack stack);

        boolean isBound(ItemStack stack);

        @Nullable
        UUID getBound(ItemStack stack);

        void setBound(ItemStack stack, @Nullable UUID uuid);
    }

    public interface IBindableKeyHolder extends IKeyHolder, IBindable
    {
        default boolean isBound(ItemStack stack)
        {
            if (!isPrivate(stack))
                return false;
            return findHolderTag(stack).map(tag -> !Strings.isNullOrEmpty(tag.getString("Bound"))).orElse(false);
        }

        @Nullable
        default String getBoundStr(ItemStack stack)
        {
            if (!isPrivate(stack))
                return null;
            return findHolderTag(stack).map(tag -> tag.getString("Bound")).orElse(null);
        }

        @Nullable
        default UUID getBound(ItemStack stack)
        {
            if (!isPrivate(stack))
                return null;
            return findHolderTag(stack).map(tag -> {
                if (!tag.contains("Bound", Tag.TAG_STRING))
                    return null;
                try
                {
                    return UUID.fromString(tag.getString("Bound"));
                }
                catch (IllegalArgumentException e)
                {
                    LOGGER.warn("Stack contained wrong UUID", e);
                    return null;
                }
            }).orElse(null);
        }

        default void setBound(ItemStack stack, @Nullable UUID uuid)
        {
            if (uuid == null)
                findHolderTag(stack).ifPresent(blockTag -> blockTag.remove("Bound"));
            else
                getOrCreateHolderTag(stack).putString("Bound", uuid.toString());
        }
    }

    public static long getKey(ItemStack stack)
    {
        if (stack.getItem() instanceof IKeyHolder holder)
            return holder.getKey(stack);
        return -1;
    }

    public static ItemStack setKey(ItemStack stack, long key)
    {
        if (stack.getItem() instanceof IKeyHolder holder)
            holder.setKey(stack, key);
        return stack;
    }

    public static boolean isPrivate(ItemStack stack)
    {
        if (stack.getItem() instanceof IKeyHolder holder)
            return holder.isPrivate(stack);
        return false;
    }

    public static ItemStack setPrivate(ItemStack stack, boolean priv)
    {
        if (stack.getItem() instanceof IKeyHolder  holder)
            holder.setPrivate(stack, priv);
        return stack;
    }

    public static boolean isBound(ItemStack stack)
    {
        if (stack.getItem() instanceof IBindable bindable)
            return bindable.isBound(stack);
        return false;
    }

    @Nullable
    public static UUID getBound(ItemStack stack)
    {
        if (stack.getItem() instanceof IBindable bindable)
            return bindable.getBound(stack);
        return null;
    }

    public static ItemStack setBound(ItemStack stack, @Nullable UUID uuid)
    {
        if (stack.getItem() instanceof IBindable bindable)
            bindable.setBound(stack, uuid);
        return stack;
    }

    public static long getKey(BlockEntity te)
    {
        if (te instanceof EnderKeyChestBlockEntity chest)
        {
            return chest.getKey();
        }

        return -1;
    }

    public static ItemStack getItem(ItemLike itemProvider, long key, boolean priv)
    {
        return setPrivate(setKey(new ItemStack(itemProvider), key), priv);
    }

    public static ItemStack getLock(long key, boolean priv)
    {
        return getItem(Enderthing.LOCK.get(), key, priv);
    }

    public static ItemStack getLock(long key, boolean priv, @Nullable UUID bound)
    {
        ItemStack stack = getItem(Enderthing.LOCK.get(), key, priv);
        if (bound != null)
            setBound(stack, bound);
        return stack;
    }

    public static ItemStack getKeyChest(long key, boolean priv, @Nullable UUID bound)
    {
        ItemStack stack = getItem(Enderthing.KEY_CHEST_ITEM.get(), key, priv);
        if (bound != null)
            setBound(stack, bound);
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
        Player player = playerList.getPlayer(uuid);
        if (player != null)
            return player.getName().getString();
        return null;
    }

    public static long getKeyFromPasscode(String passcode)
    {
        if (Strings.isNullOrEmpty(passcode))
            return -1;

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
            return Longs.fromByteArray(md.digest()) & 0x7fffffffffffffffL;
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException)
        {
            throw new RuntimeException(noSuchAlgorithmException);
        }
    }

    public static long getKeyFromPasscode(List<ItemStack> passcode)
    {
        if (passcode.size() == 0)
            return -1;

        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            for (ItemStack st : passcode)
            {
                md.update(BuiltInRegistries.ITEM.getKey(st.getItem()).toString().getBytes());
                if (st.hasCustomHoverName())
                    md.update(st.getHoverName().getString().getBytes());
            }
            return Longs.fromByteArray(md.digest()) & 0x7fffffffffffffffL;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }
}
