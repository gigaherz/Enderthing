package gigaherz.enderthing.storage;

import com.google.common.collect.Maps;
import gigaherz.enderthing.Enderthing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.storage.WorldSavedDataStorage;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryManager extends WorldSavedData implements IInventoryManager
{
    private static final String StorageKey = Enderthing.MODID + "_InventoryStorageManager";

    private Container global = new Container();
    private Map<UUID, Container> perPlayer = Maps.newHashMap();

    public InventoryManager()
    {
        super(StorageKey);
    }

    public InventoryManager(String s)
    {
        super(s);
    }

    public static InventoryManager get(World world)
    {
        WorldSavedDataStorage storage = world.getSavedDataStorage();
        InventoryManager instance = storage.get(DimensionType.OVERWORLD, InventoryManager::new, StorageKey);
        if (instance == null)
        {
            instance = new InventoryManager();
            storage.set(DimensionType.OVERWORLD, StorageKey, instance);
        }

        return instance;
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
    public void read(NBTTagCompound nbt)
    {
        global.deserializeNBT(nbt);

        if (nbt.contains("Private", Constants.NBT.TAG_LIST))
        {
            NBTTagList list = nbt.getList("Private", Constants.NBT.TAG_COMPOUND);

            perPlayer.clear();

            for (int i = 0; i < list.size(); ++i)
            {
                NBTTagCompound containerTag = list.getCompound(i);
                UUID uuid = uuidFromNBT(containerTag);

                Container container = new Container();
                container.deserializeNBT(containerTag);

                perPlayer.put(uuid, container);
            }
        }
    }

    @Override
    public NBTTagCompound write(NBTTagCompound compound)
    {
        NBTTagCompound temp = global.serializeNBT();

        compound.put("Inventories", temp.get("Inventories"));

        NBTTagList list = new NBTTagList();
        for (Map.Entry<UUID, Container> e : perPlayer.entrySet())
        {
            NBTTagCompound tag = e.getValue().serializeNBT();
            uuidToNBT(tag, e.getKey());
            list.add(tag);
        }

        compound.put("Private", list);

        return compound;
    }

    public static void uuidToNBT(NBTTagCompound tag, UUID uuid)
    {
        tag.putLong("PlayerUUID0", uuid.getLeastSignificantBits());
        tag.putLong("PlayerUUID1", uuid.getMostSignificantBits());
    }

    public static UUID uuidFromNBT(NBTTagCompound tag)
    {
        if (!tag.contains("PlayerUUID0", Constants.NBT.TAG_LONG))
            return null;

        long uuid0 = tag.getLong("PlayerUUID0");
        long uuid1 = tag.getLong("PlayerUUID1");

        return new UUID(uuid1, uuid0);
    }

    private class Container implements INBTSerializable<NBTTagCompound>, IInventoryManager
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
        public NBTTagCompound serializeNBT()
        {
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagList inventories = new NBTTagList();

            for (Map.Entry<Long, EnderInventory> entry : this.inventories.entrySet())
            {
                EnderInventory inventory = entry.getValue();

                NBTTagCompound inventoryTag = new NBTTagCompound();
                inventoryTag.putLong("Key", entry.getKey());
                inventoryTag.put("Contents", inventory.serializeNBT());
                inventories.add(inventoryTag);
            }

            tag.put("Inventories", inventories);
            return tag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt)
        {
            NBTTagList nbtTagList = nbt.getList("Inventories", Constants.NBT.TAG_COMPOUND);

            inventories.clear();

            for (int i = 0; i < nbtTagList.size(); ++i)
            {
                NBTTagCompound inventoryTag = nbtTagList.getCompound(i);
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
