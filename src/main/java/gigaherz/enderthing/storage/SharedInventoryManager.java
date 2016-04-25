package gigaherz.enderthing.storage;

import gigaherz.enderthing.Enderthing;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class SharedInventoryManager extends WorldSavedData
{
    private static final String StorageKey = Enderthing.MODID + "_InventoryStorageManager";

    private Map<Integer, SharedInventory> inventories = new HashMap<Integer, SharedInventory>();

    public SharedInventoryManager()
    {
        super(StorageKey);
    }

    public SharedInventoryManager(String s)
    {
        super(s);
    }

    public static SharedInventoryManager get(World world)
    {
        MapStorage storage = world.getMapStorage();
        SharedInventoryManager instance = (SharedInventoryManager) storage.loadData(SharedInventoryManager.class, StorageKey);
        if (instance == null)
        {
            instance = new SharedInventoryManager();
            storage.setData(StorageKey, instance);
        }

        return instance;
    }

    public SharedInventory getInventory(int id)
    {
        SharedInventory rift = inventories.get(id);

        if (rift == null)
        {
            rift = new SharedInventory(this);
            inventories.put(id, rift);
        }

        return rift;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        NBTTagList nbtTagList = nbtTagCompound.getTagList("Inventories", Constants.NBT.TAG_COMPOUND);

        inventories.clear();

        for (int i = 0; i < nbtTagList.tagCount(); ++i)
        {
            NBTTagCompound inventoryTag = nbtTagList.getCompoundTagAt(i);
            int j = inventoryTag.getInteger("InventoryId");

            SharedInventory inventory = new SharedInventory(this);

            inventory.deserializeNBT(inventoryTag.getCompoundTag("InventoryContents"));

            inventories.put(j, inventory);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        NBTTagList inventories = new NBTTagList();

        for (Map.Entry<Integer, SharedInventory> entry : this.inventories.entrySet())
        {
            SharedInventory inventory = entry.getValue();

            NBTTagCompound inventoryTag = new NBTTagCompound();
            inventoryTag.setInteger("InventoryId", entry.getKey());
            inventoryTag.setTag("InventoryContents", inventory.serializeNBT());
            inventories.appendTag(inventoryTag);
        }

        nbtTagCompound.setTag("Inventories", inventories);
    }
}
