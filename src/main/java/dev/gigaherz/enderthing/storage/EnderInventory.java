package dev.gigaherz.enderthing.storage;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.enderthing.blocks.EnderKeyChestBlockEntity;
import dev.gigaherz.enderthing.client.KeyColor;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EnderInventory extends ItemStackHandler
{
    public static final int SLOT_COUNT = 27;

    public static final Codec<EnderInventory> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ItemStack.OPTIONAL_CODEC.listOf().fieldOf("Items").forGetter(i -> i.stacks),
                    Codec.LONG.optionalFieldOf("created").forGetter(i -> Optional.of(i.created)),
                    Codec.LONG.optionalFieldOf("lastLoaded").forGetter(i -> Optional.of(i.lastLoaded)),
                    Codec.LONG.optionalFieldOf("layer").forGetter(i -> Optional.of(i.created))
                ).apply(instance, EnderInventory::new));

    private IInventoryManager manager;

    private final List<Reference<? extends EnderKeyChestBlockEntity>> listeners = Lists.newArrayList();

    private long created = 0;
    private long lastLoaded = 0;
    private long lastModified = 0;

    EnderInventory()
    {
        this(List.of(), getTimestamp(), 0, getTimestamp());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private EnderInventory(List<ItemStack> stacks, Optional<Long> created, Optional<Long> lastLoaded, Optional<Long> lastModified)
    {
        this(stacks, created.orElseGet(EnderInventory::getTimestamp), getTimestamp(), lastModified.orElseGet(EnderInventory::getTimestamp));
    }

    EnderInventory(List<ItemStack> stacks, long created, long lastLoaded, long lastModified)
    {
        super(SLOT_COUNT);
        setSize(SLOT_COUNT); // FIXME: HACK -- Remove me
        for(int i=0;i<Math.min(SLOT_COUNT, stacks.size());i++)
        {
            this.stacks.set(i, Objects.requireNonNullElse(stacks.get(i), ItemStack.EMPTY));
        }
        this.created = created;
        this.lastLoaded = lastLoaded;
        this.lastModified = lastModified;
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

    @Override
    public void serialize(ValueOutput output)
    {
        super.serialize(output);
        output.putLong("created", created);
        output.putLong("lastLoaded", lastLoaded);
        output.putLong("lastModified", lastModified);
    }

    @Override
    public void deserialize(ValueInput input)
    {
        super.deserialize(input);
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
