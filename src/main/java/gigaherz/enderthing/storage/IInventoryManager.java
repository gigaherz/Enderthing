package gigaherz.enderthing.storage;

public interface IInventoryManager
{
    void setDirty();

    EnderInventory getInventory(int id);
}
