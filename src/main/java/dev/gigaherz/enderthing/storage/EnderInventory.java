package dev.gigaherz.enderthing.storage;

import com.google.common.collect.Lists;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

public class EnderInventory extends ItemStackHandler
{
    public static final int SLOT_COUNT = 27;

    private final IInventoryManager manager;

    private final List<Reference<? extends EnderKeyChestBlockEntity>> listeners = Lists.newArrayList();

    private long created = 0;
    private long lastLoaded = 0;
    private long lastModified = 0;

    public void addWeakListener(EnderKeyChestBlockEntity e)
    {
        listeners.add(new WeakReference<>(e));
        lastLoaded = getTimestamp();
        manager.makeDirty();
    }

    private long getTimestamp()
    {
        return System.currentTimeMillis() / 1000;
    }

    public void removeWeakListener(EnderKeyChestBlockEntity e)
    {
        for (Iterator<Reference<? extends EnderKeyChestBlockEntity>> it = listeners.iterator(); it.hasNext(); )
        {
            EnderKeyChestBlockEntity te = it.next().get();
            if (te == null || te.isRemoved() || te == e)
            {
                it.remove();
            }
        }
    }

    @Override
    protected void onContentsChanged(int slot)
    {
        super.onContentsChanged(slot);

        List<EnderKeyChestBlockEntity> dirty = Lists.newArrayList();
        for (Iterator<Reference<? extends EnderKeyChestBlockEntity>> it = listeners.iterator(); it.hasNext(); )
        {
            EnderKeyChestBlockEntity te = it.next().get();
            if (te == null || te.isRemoved())
            {
                it.remove();
            }
            else
            {
                dirty.add(te);
            }
        }

        dirty.forEach(BlockEntity::setChanged);

        if (dirty.size() > 0)
            lastLoaded = getTimestamp();
        lastModified = getTimestamp();

        manager.makeDirty();
    }

    EnderInventory(IInventoryManager manager)
    {
        super(SLOT_COUNT);
        setSize(SLOT_COUNT); // FIXME: HACK -- Remove me
        this.manager = manager;
        created = getTimestamp();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider lookup)
    {
        var tag = super.serializeNBT(lookup);
        tag.putLong("created", created);
        tag.putLong("lastLoaded", lastLoaded);
        tag.putLong("lastModified", lastModified);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider lookup, CompoundTag nbt)
    {
        super.deserializeNBT(lookup, nbt);
        if (nbt.contains("created", Tag.TAG_ANY_NUMERIC))
            created = nbt.getLong("created");
        else
            created = getTimestamp();
        if (nbt.contains("lastLoaded", Tag.TAG_ANY_NUMERIC))
            lastLoaded = nbt.getLong("lastLoaded");
        else
            lastLoaded = getTimestamp();
        if (nbt.contains("lastModified", Tag.TAG_ANY_NUMERIC))
            lastModified = nbt.getLong("lastModified");
        else
            lastModified = lastLoaded;
    }

    public long getCreationTimestamp()
    {
        return created;
    }

    public long getLastLoadedTimestamp()
    {
        return lastLoaded;
    }

    public long getLastModifiedTimestamp()
    {
        return lastModified;
    }
}
