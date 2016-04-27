package gigaherz.enderthing.storage;

public interface IInventoryManager
{
    void setDirty();

    EnderKeyInventory getInventory(int id);
}
