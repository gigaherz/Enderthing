package gigaherz.enderthing.storage;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public class InventoryManager extends WorldSavedData implements IInventoryManager
{
    private static final String DATA_NAME = "enderthing_InventoryStorageManager";

    private Container global = new Container();
    private Map<UUID, Container> perPlayer = Maps.newHashMap();

    public InventoryManager()
    {
        super(DATA_NAME);
    }

    public static InventoryManager get(World world)
    {
        if (!(world instanceof ServerWorld))
        {
            throw new RuntimeException("Attempted to get the data from a client world. This is wrong.");
        }

        ServerWorld overworld = world.getServer().getWorld(DimensionType.OVERWORLD);

        DimensionSavedDataManager storage = overworld.getSavedData();
        return storage.getOrCreate(InventoryManager::new, DATA_NAME);
    }

    @Override
    public void setDirty()
    {
        markDirty();
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
            markDirty();
        }

        return container;
    }

    @Override
    public void read(CompoundNBT nbt)
    {
        global.deserializeNBT(nbt);

        if (nbt.contains("Private", Constants.NBT.TAG_LIST))
        {
            ListNBT list = nbt.getList("Private", Constants.NBT.TAG_COMPOUND);

            perPlayer.clear();

            for (int i = 0; i < list.size(); ++i)
            {
                CompoundNBT containerTag = list.getCompound(i);
                UUID uuid = uuidFromNBT(containerTag);

                Container container = new Container();
                container.deserializeNBT(containerTag);

                perPlayer.put(uuid, container);
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        CompoundNBT temp = global.serializeNBT();

        compound.put("Inventories", temp.get("Inventories"));

        ListNBT list = new ListNBT();
        for (Map.Entry<UUID, Container> e : perPlayer.entrySet())
        {
            CompoundNBT tag = e.getValue().serializeNBT();
            uuidToNBT(tag, e.getKey());
            list.add(tag);
        }

        compound.put("Private", list);

        return compound;
    }

    public static void uuidToNBT(CompoundNBT tag, UUID uuid)
    {
        tag.putLong("PlayerUUID0", uuid.getLeastSignificantBits());
        tag.putLong("PlayerUUID1", uuid.getMostSignificantBits());
    }

    @Nullable
    public static UUID uuidFromNBT(CompoundNBT tag)
    {
        if (!tag.contains("PlayerUUID0", Constants.NBT.TAG_LONG) ||
                !tag.contains("PlayerUUID1", Constants.NBT.TAG_LONG))
            return null;

        long uuid0 = tag.getLong("PlayerUUID0");
        long uuid1 = tag.getLong("PlayerUUID1");

        return new UUID(uuid1, uuid0);
    }

    private class Container implements INBTSerializable<CompoundNBT>, IInventoryManager
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
        public CompoundNBT serializeNBT()
        {
            CompoundNBT tag = new CompoundNBT();
            ListNBT inventories = new ListNBT();

            for (Map.Entry<Long, EnderInventory> entry : this.inventories.entrySet())
            {
                EnderInventory inventory = entry.getValue();

                CompoundNBT inventoryTag = new CompoundNBT();
                inventoryTag.putLong("Key", entry.getKey());
                inventoryTag.put("Contents", inventory.serializeNBT());
                inventories.add(inventoryTag);
            }

            tag.put("Inventories", inventories);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt)
        {
            ListNBT nbtTagList = nbt.getList("Inventories", Constants.NBT.TAG_COMPOUND);

            inventories.clear();

            for (int i = 0; i < nbtTagList.size(); ++i)
            {
                CompoundNBT inventoryTag = nbtTagList.getCompound(i);
                long j = inventoryTag.getLong("Key");

                EnderInventory inventory = new EnderInventory(this);

                inventory.deserializeNBT(inventoryTag.getCompound("Contents"));

                inventories.put(j, inventory);
            }
        }

        @Override
        public void setDirty()
        {
            markDirty();
        }
    }
}
