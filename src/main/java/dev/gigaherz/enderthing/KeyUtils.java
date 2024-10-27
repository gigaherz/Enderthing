package dev.gigaherz.enderthing;

import com.google.common.primitives.Longs;
import com.mojang.serialization.Codec;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import joptsimple.internal.Strings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class KeyUtils
{
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, Enderthing.MODID);

    public static final Supplier<DataComponentType<UUID>> BINDING = DATA_COMPONENT_TYPES.register("binding", () ->
            DataComponentType.<UUID>builder().persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build()
    );

    public static final Supplier<DataComponentType<Boolean>> IS_PRIVATE = DATA_COMPONENT_TYPES.register("is_private", () ->
            DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build()
    );

    public static final Supplier<DataComponentType<Long>> KEY = DATA_COMPONENT_TYPES.register("key", () ->
            DataComponentType.<Long>builder().persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG).build()
    );

    public static final Supplier<DataComponentType<String>> CACHED_PLAYER_NAME = DATA_COMPONENT_TYPES.register("cached_player_name", () ->
            DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build()
    );

    public static final TagKey<Item> CAN_MAKE_BOUND = TagKey.create(Registries.ITEM, Enderthing.location("can_make_bound"));
    public static final TagKey<Item> CAN_MAKE_PRIVATE = TagKey.create(Registries.ITEM, Enderthing.location("can_make_private"));

    public static void init(IEventBus modBus)
    {
        DATA_COMPONENT_TYPES.register(modBus);
    }

    public static long getKey(ItemStack stack)
    {
        return Objects.requireNonNullElse(stack.get(KEY), -1L);
    }

    public static ItemStack setKey(ItemStack stack, long key)
    {
        stack.set(KEY, key);
        return stack;
    }

    public static boolean isPrivate(ItemStack stack)
    {
        return Boolean.TRUE.equals(stack.get(IS_PRIVATE));
    }

    public static ItemStack setPrivate(ItemStack stack, boolean priv)
    {
        stack.set(IS_PRIVATE, priv);
        return stack;
    }

    public static boolean isBound(ItemStack stack)
    {
        return stack.get(BINDING) != null;
    }

    @Nullable
    public static UUID getBound(ItemStack stack)
    {
        return stack.get(BINDING);
    }

    public static ItemStack setBound(ItemStack stack, @Nullable UUID uuid)
    {
        stack.set(BINDING, uuid);
        return stack;
    }

    @Nullable
    public static String getBoundStr(ItemStack stack)
    {
        var uuid = getBound(stack);
        return uuid != null ? uuid.toString() : null;
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
                if (st.has(DataComponents.CUSTOM_NAME))
                    md.update(st.getHoverName().getString().getBytes());
            }
            return Longs.fromByteArray(md.digest()) & 0x7fffffffffffffffL;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void setCachedPlayerName(ItemStack stack, String string)
    {
        stack.set(CACHED_PLAYER_NAME, string);
    }

    @Nullable
    public static String getCachedPlayerName(ItemStack stack)
    {
        return stack.get(CACHED_PLAYER_NAME);
    }

    public static void addStandardInformation(ItemStack stack, List<Component> tooltip)
    {
        if (KeyUtils.isPrivate(stack))
        {
            tooltip.add(Component.translatable("tooltip.enderthing.private").withStyle(ChatFormatting.ITALIC, ChatFormatting.BOLD));
        }

        long key = KeyUtils.getKey(stack);
        if (key >= 0)
        {
            tooltip.add(Component.translatable("tooltip.enderthing.key", key).withStyle(ChatFormatting.ITALIC));
        }
        else
        {
            tooltip.add(Component.translatable("tooltip.enderthing.key_missing").withStyle(ChatFormatting.ITALIC));
        }
    }
}
