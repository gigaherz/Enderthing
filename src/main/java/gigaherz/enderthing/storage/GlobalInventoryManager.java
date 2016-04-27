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

public class GlobalInventoryManager extends WorldSavedData implements IInventoryManager
{
    private static final String StorageKey = Enderthing.MODID + "_InventoryStorageManager";

    private Map<Integer, EnderKeyInventory> inventories = new HashMap<Integer, EnderKeyInventory>();

    public GlobalInventoryManager()
    {
        super(StorageKey);
    }

    public GlobalInventoryManager(String s)
    {
        super(s);
    }

    public static GlobalInventoryManager get(World world)
    {
        MapStorage storage = world.getMapStorage();
        GlobalInventoryManager instance = (GlobalInventoryManager) storage.loadData(GlobalInventoryManager.class, StorageKey);
        if (instance == null)
        {
            instance = new GlobalInventoryManager();
            storage.setData(StorageKey, instance);
        }

        return instance;
    }

    @Override
    public void setDirty()
    {
        markDirty();
    }

    public EnderKeyInventory getInventory(int id)
    {
        EnderKeyInventory rift = inventories.get(id);

        if (rift == null)
        {
            rift = new EnderKeyInventory(this);
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

            EnderKeyInventory inventory = new EnderKeyInventory(this);

            inventory.deserializeNBT(inventoryTag.getCompoundTag("InventoryContents"));

            inventories.put(j, inventory);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        NBTTagList inventories = new NBTTagList();

        for (Map.Entry<Integer, EnderKeyInventory> entry : this.inventories.entrySet())
        {
            EnderKeyInventory inventory = entry.getValue();

            NBTTagCompound inventoryTag = new NBTTagCompound();
            inventoryTag.setInteger("InventoryId", entry.getKey());
            inventoryTag.setTag("InventoryContents", inventory.serializeNBT());
            inventories.appendTag(inventoryTag);
        }

        nbtTagCompound.setTag("Inventories", inventories);
    }
}
