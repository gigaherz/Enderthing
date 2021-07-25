package gigaherz.enderthing.storage;

public interface IInventoryManager
{
    void makeDirty();

    EnderInventory getInventory(long id);
}
