package dev.gigaherz.enderthing.storage;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class EnderInventory extends SimpleContainer implements ValueIOSerializable
{
    public static final int SLOT_COUNT = 27;

    public static final Codec<EnderInventory> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ItemStack.OPTIONAL_CODEC.listOf().fieldOf("Items").forGetter(SimpleContainer::getItems),
                    Codec.LONG.optionalFieldOf("created", -1L).forGetter(EnderInventory::getCreationTimestamp),
                    Codec.LONG.optionalFieldOf("lastLoaded", -1L).forGetter(EnderInventory::getLastLoadedTimestamp),
                    Codec.LONG.optionalFieldOf("lastModified", -1L).forGetter(EnderInventory::getLastModifiedTimestamp)
                ).apply(instance, EnderInventory::new));

    private IInventoryManager manager;

    private final List<Reference<? extends EnderKeyChestBlockEntity>> listeners = Lists.newArrayList();

    private long created;
    private long lastLoaded;
    private long lastModified;

    EnderInventory()
    {
        this(List.of(), getTimestamp(), 0, getTimestamp());
    }

    EnderInventory(List<ItemStack> stacks, long created, long lastLoaded, long lastModified)
    {
        super(SLOT_COUNT);
        for(int i=0;i<Math.min(SLOT_COUNT, stacks.size());i++)
        {
            this.setItem(i, Objects.requireNonNullElse(stacks.get(i), ItemStack.EMPTY));
        }
        this.created = created == -1 ? getTimestamp() : created;
        this.lastLoaded = getTimestamp(); // ignore the provided value and set the current timestamp
        this.lastModified = lastModified == -1  ? getTimestamp() : lastLoaded;
    }

    private static long getTimestamp()
    {
        return System.currentTimeMillis() / 1000;
    }

    public void addWeakListener(EnderKeyChestBlockEntity e)
    {
        listeners.add(new WeakReference<>(e));
        lastLoaded = getTimestamp();
        manager.makeDirty();
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
    public void setChanged()
    {
        boolean b = processWeakListeners();

        if (b)
            lastLoaded = getTimestamp();
        lastModified = getTimestamp();

        if (manager != null)
            manager.makeDirty();

        super.setChanged();
    }

    private boolean processWeakListeners()
    {
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

        return !dirty.isEmpty();
    }

    @Override
    public void serialize(ValueOutput output) {
        ValueOutput.TypedOutputList<ItemStackWithSlot> itemList = output.list("Items", ItemStackWithSlot.CODEC);
        for (int i = 0; i < this.getContainerSize(); i++) {
            var stack = getItem(i);
            if (!stack.isEmpty()) {
                itemList.add(new ItemStackWithSlot(i, stack));
            }
        }

        output.putLong("created", created);
        output.putLong("lastLoaded", lastLoaded);
        output.putLong("lastModified", lastModified);
    }

    @Override
    public void deserialize(ValueInput input) {
        input.listOrEmpty("Items", ItemStackWithSlot.CODEC).forEach(slot -> {
            if (slot.isValidInContainer(this.getContainerSize())) {
                setItem(slot.slot(), slot.stack());
            }
        });

        created = input.getLongOr("created", getTimestamp());
        lastLoaded = input.getLongOr("lastLoaded", getTimestamp());
        lastModified = input.getLongOr("lastModified", lastLoaded);
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

    public void setManager(IInventoryManager manager)
    {
        this.manager = manager;
    }
}
