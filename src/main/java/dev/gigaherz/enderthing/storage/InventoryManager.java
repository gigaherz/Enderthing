package dev.gigaherz.enderthing.storage;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public class InventoryManager extends SavedData implements IInventoryManager
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DATA_NAME = "enderthing_InventoryStorageManager";

    private final Container global = new Container();
    private final Map<UUID, Container> perPlayer = Maps.newHashMap();

    public InventoryManager()
    {
    }

    public InventoryManager(CompoundTag nbt, HolderLookup.Provider lookup)
    {
        global.deserializeNBT(lookup, nbt);

        if (nbt.contains("Private", Tag.TAG_LIST))
        {
            ListTag list = nbt.getList("Private", Tag.TAG_COMPOUND);

            for (int i = 0; i < list.size(); ++i)
            {
                CompoundTag containerTag = list.getCompound(i);
                UUID uuid = uuidFromNBT(containerTag);

                Container container = new Container();
                container.deserializeNBT(lookup, containerTag);

                perPlayer.put(uuid, container);
            }
        }
    }

    private static boolean errorLogged = false;

    private static final InventoryManager DUMMY_CLIENT = new InventoryManager()
    {
        private final EnderInventory inv = new EnderInventory(this)
        {
            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
            {
                return stack;
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack)
            {
                return false;
            }
        };

        @Override
        public EnderInventory getInventory(long id)
        {
            return inv;
        }

        @Override
        public IInventoryManager getPrivate(UUID uuid)
        {
            return this;
        }
    };

    public static InventoryManager get(Level world)
    {
        if (!(world instanceof ServerLevel))
        {
            if (!errorLogged)
            {
                RuntimeException exc = new RuntimeException("Attempted to get the data from a client world. This is wrong.");

                LOGGER.error("Some mod attempted to get the inventory contents of an Ender Key from the client. This is not supported.", exc);
                errorLogged = true;
            }
            return DUMMY_CLIENT;
        }
        ServerLevel overworld = world.getServer().overworld();

        DimensionDataStorage storage = overworld.getDataStorage();
        return storage.computeIfAbsent(new SavedData.Factory<>(InventoryManager::new, InventoryManager::new), DATA_NAME);
    }

    @Override
    public void makeDirty()
    {
        setDirty();
    }

    public EnderInventory getInventory(long id)
    {
        return global.getInventory(id);
    }

    public IInventoryManager getPrivate(UUID uuid)
    {
        Container container = perPlayer.get(uuid);
        if (container == null)
        {
            container = new Container();
            perPlayer.put(new UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()), container);
            makeDirty();
        }

        return container;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup)
    {
        CompoundTag temp = global.serializeNBT(lookup);

        tag.put("Inventories", temp.get("Inventories"));

        ListTag list = new ListTag();
        for (Map.Entry<UUID, Container> e : perPlayer.entrySet())
        {
            CompoundTag containerTag = e.getValue().serializeNBT(lookup);
            uuidToNBT(containerTag, e.getKey());
            list.add(containerTag);
        }

        tag.put("Private", list);

        return tag;
    }

    public static void uuidToNBT(CompoundTag tag, UUID uuid)
    {
        tag.putLong("PlayerUUID0", uuid.getLeastSignificantBits());
        tag.putLong("PlayerUUID1", uuid.getMostSignificantBits());
    }

    @Nullable
    public static UUID uuidFromNBT(CompoundTag tag)
    {
        if (!tag.contains("PlayerUUID0", Tag.TAG_LONG) ||
                !tag.contains("PlayerUUID1", Tag.TAG_LONG))
            return null;

        long uuid0 = tag.getLong("PlayerUUID0");
        long uuid1 = tag.getLong("PlayerUUID1");

        return new UUID(uuid1, uuid0);
    }

    private class Container implements INBTSerializable<CompoundTag>, IInventoryManager
    {
        private Map<Long, EnderInventory> inventories = Maps.newHashMap();

        @Override
        public EnderInventory getInventory(long id)
        {
            EnderInventory inventory = inventories.get(id);

            if (inventory == null)
            {
                inventory = new EnderInventory(this);
                inventories.put(id, inventory);
            }

            return inventory;
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider lookup)
        {
            CompoundTag tag = new CompoundTag();
            ListTag inventories = new ListTag();

            for (Map.Entry<Long, EnderInventory> entry : this.inventories.entrySet())
            {
                EnderInventory inventory = entry.getValue();

                CompoundTag inventoryTag = new CompoundTag();
                inventoryTag.putLong("Key", entry.getKey());
                inventoryTag.put("Contents", inventory.serializeNBT(lookup));
                inventories.add(inventoryTag);
            }

            tag.put("Inventories", inventories);
            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider lookup, CompoundTag nbt)
        {
            ListTag nbtTagList = nbt.getList("Inventories", Tag.TAG_COMPOUND);

            inventories.clear();

            for (int i = 0; i < nbtTagList.size(); ++i)
            {
                CompoundTag inventoryTag = nbtTagList.getCompound(i);
                long j = inventoryTag.getLong("Key");

                EnderInventory inventory = new EnderInventory(this);

                inventory.deserializeNBT(lookup, inventoryTag.getCompound("Contents"));

                inventories.put(j, inventory);
            }
        }

        @Override
        public void makeDirty()
        {
            setDirty();
        }
    }
}
