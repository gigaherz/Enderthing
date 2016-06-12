package gigaherz.enderthing.storage;

import com.google.common.collect.Maps;
import gigaherz.enderthing.Enderthing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
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
        MapStorage storage = world.getMapStorage();
        InventoryManager instance = (InventoryManager) storage.getOrLoadData(InventoryManager.class, StorageKey);
        if (instance == null)
        {
            instance = new InventoryManager();
            storage.setData(StorageKey, instance);
        }

        return instance;
    }

    @Override
    public void setDirty()
    {
        markDirty();
    }

    public EnderInventory getInventory(int id)
    {
        return global.getInventory(id);
    }

    public IInventoryManager getPrivate(EntityPlayer owner)
    {
        UUID key = owner.getUniqueID();
        return getPrivate(key);
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
    public void readFromNBT(NBTTagCompound nbt)
    {
        global.deserializeNBT(nbt);

        if (nbt.hasKey("Private", Constants.NBT.TAG_LIST))
        {
            NBTTagList list = nbt.getTagList("Private", Constants.NBT.TAG_COMPOUND);

            perPlayer.clear();

            for (int i = 0; i < list.tagCount(); ++i)
            {
                NBTTagCompound containerTag = list.getCompoundTagAt(i);
                UUID uuid = uuidFromNBT(containerTag);

                Container container = new Container();
                container.deserializeNBT(containerTag);

                perPlayer.put(uuid, container);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        NBTTagCompound temp = global.serializeNBT();

        nbtTagCompound.setTag("Inventories", temp.getTag("Inventories"));

        NBTTagList list = new NBTTagList();
        for (Map.Entry<UUID, Container> e : perPlayer.entrySet())
        {
            NBTTagCompound tag = e.getValue().serializeNBT();
            uuidToNBT(tag, e.getKey());
            list.appendTag(tag);
        }

        nbtTagCompound.setTag("Private", list);
    }

    public void importCapabilityData(EntityPlayer player, NBTTagCompound nbt)
    {
        ((Container) getPrivate(player)).importNBT(nbt);
    }

    public static void uuidToNBT(NBTTagCompound tag, UUID uuid)
    {
        tag.setLong("PlayerUUID0", uuid.getLeastSignificantBits());
        tag.setLong("PlayerUUID1", uuid.getMostSignificantBits());
    }

    public static UUID uuidFromNBT(NBTTagCompound tag)
    {
        if (!tag.hasKey("PlayerUUID0", Constants.NBT.TAG_LONG))
            return null;

        long uuid0 = tag.getLong("PlayerUUID0");
        long uuid1 = tag.getLong("PlayerUUID1");

        return new UUID(uuid1, uuid0);
    }

    private class Container implements INBTSerializable<NBTTagCompound>, IInventoryManager
    {
        private Map<Integer, EnderInventory> inventories = new HashMap<Integer, EnderInventory>();

        public EnderInventory getInventory(int id)
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

            for (Map.Entry<Integer, EnderInventory> entry : this.inventories.entrySet())
            {
                EnderInventory inventory = entry.getValue();

                NBTTagCompound inventoryTag = new NBTTagCompound();
                inventoryTag.setInteger("InventoryId", entry.getKey());
                inventoryTag.setTag("InventoryContents", inventory.serializeNBT());
                inventories.appendTag(inventoryTag);
            }

            tag.setTag("Inventories", inventories);
            return tag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt)
        {
            NBTTagList nbtTagList = nbt.getTagList("Inventories", Constants.NBT.TAG_COMPOUND);

            inventories.clear();

            for (int i = 0; i < nbtTagList.tagCount(); ++i)
            {
                NBTTagCompound inventoryTag = nbtTagList.getCompoundTagAt(i);
                int j = inventoryTag.getInteger("InventoryId");

                EnderInventory inventory = new EnderInventory(this);

                inventory.deserializeNBT(inventoryTag.getCompoundTag("InventoryContents"));

                inventories.put(j, inventory);
            }
        }

        void importNBT(NBTTagCompound nbt)
        {
            NBTTagList nbtTagList = nbt.getTagList("Inventories", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < nbtTagList.tagCount(); ++i)
            {
                NBTTagCompound inventoryTag = nbtTagList.getCompoundTagAt(i);
                int j = inventoryTag.getInteger("InventoryId");

                if (!inventories.containsKey(j))
                {
                    EnderInventory inventory = new EnderInventory(this);


                    inventory.deserializeNBT(inventoryTag.getCompoundTag("InventoryContents"));

                    inventories.put(j, inventory);
                }
            }
        }

        @Override
        public void setDirty()
        {
            markDirty();
        }
    }
}
