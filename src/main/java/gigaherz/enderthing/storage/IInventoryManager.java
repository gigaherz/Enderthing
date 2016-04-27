package gigaherz.enderthing.storage;

public interface IInventoryManager
{
    void markDirty();
    EnderKeyInventory getInventory(int id);
}
