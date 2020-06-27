package gigaherz.enderthing;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Longs;
import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import joptsimple.internal.Strings;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.entity.passive.horse.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class KeyUtils
{
    public interface IKeyHolder
    {
        Optional<CompoundNBT> findHolderTag(ItemStack stack);
        CompoundNBT getOrCreateHolderTag(ItemStack stack);

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
            return findHolderTag(stack).map(blockTag -> blockTag.contains("Key", Constants.NBT.TAG_LONG) ? blockTag.getLong("Key") : -1).orElse(-1L);
        }

        default void setKey(ItemStack stack, long key)
        {
            getOrCreateHolderTag(stack).putLong("Key", key);
        }
    }

    public interface IBindable
    {
        Optional<CompoundNBT> findHolderTag(ItemStack stack);
        CompoundNBT getOrCreateHolderTag(ItemStack stack);

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
                if (!tag.contains("Bound", Constants.NBT.TAG_STRING))
                    return null;
                try
                {
                    return UUID.fromString(tag.getString("Bound"));
                }
                catch(IllegalArgumentException e)
                {
                    Enderthing.LOGGER.warn("Stack contained wrong UUID", e);
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
        Item item = stack.getItem();
        if (item instanceof IKeyHolder)
            return ((IKeyHolder) item).getKey(stack);
        return -1;
    }

    public static ItemStack setKey(ItemStack stack, long key)
    {
        Item item = stack.getItem();
        if (item instanceof IKeyHolder)
            ((IKeyHolder) item).setKey(stack, key);
        return stack;
    }

    public static boolean isPrivate(ItemStack stack)
    {
        Item item = stack.getItem();
        if (item instanceof IKeyHolder)
            return ((IKeyHolder) item).isPrivate(stack);
        return false;
    }

    public static ItemStack setPrivate(ItemStack stack, boolean priv)
    {
        Item item = stack.getItem();
        if (item instanceof IKeyHolder)
            ((IKeyHolder) item).setPrivate(stack, priv);
        return stack;
    }

    public static boolean isBound(ItemStack stack)
    {
        Item item = stack.getItem();
        if (item instanceof IBindable)
            return ((IBindable) item).isBound(stack);
        return false;
    }

    @Nullable
    public static UUID getBound(ItemStack stack)
    {
        Item item = stack.getItem();
        if (item instanceof IBindable)
            return ((IBindable) item).getBound(stack);
        return null;
    }

    public static ItemStack setBound(ItemStack stack, @Nullable UUID uuid)
    {
        Item item = stack.getItem();
        if (item instanceof IBindable)
            ((IBindable) item).setBound(stack, uuid);
        return stack;
    }

    public static long getKey(TileEntity te)
    {
        if (te instanceof EnderKeyChestTileEntity)
        {
            return ((EnderKeyChestTileEntity) te).getKey();
        }

        return -1;
    }

    public static ItemStack getItem(IItemProvider itemProvider, long key, boolean priv)
    {
        ItemStack stack = new ItemStack(itemProvider);

        setKey(stack, key);
        setPrivate(stack, priv);

        return stack;
    }

    public static ItemStack getLock(long key, boolean priv)
    {
        return getItem(Enderthing.LOCK, key, priv);
    }

    public static ItemStack getLock(long key, boolean priv, @Nullable UUID bound)
    {
        ItemStack stack = getItem(Enderthing.LOCK, key, priv);
        if (bound != null)
            setBound(stack, bound);
        return stack;
    }

    public static ItemStack getKeyChest(long key, boolean priv, @Nullable UUID bound)
    {
        ItemStack stack = getItem(Enderthing.KEY_CHEST_ITEM, key, priv);
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
        PlayerEntity player = playerList.getPlayerByUUID(uuid);
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
            return -1;

        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            for(ItemStack st : passcode)
            {
                md.update(st.getItem().getRegistryName().toString().getBytes());
                if (st.hasDisplayName())
                    md.update(st.getDisplayName().getUnformattedComponentText().getBytes());
            }
            return Longs.fromByteArray(md.digest())&0x7fffffffffffffffL;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

}
