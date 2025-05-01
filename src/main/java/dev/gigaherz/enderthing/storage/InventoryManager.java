package dev.gigaherz.enderthing.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class InventoryManager extends SavedData implements IInventoryManager
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DATA_NAME = "enderthing_InventoryStorageManager";

    public static MapCodec<UUID> uuidPairOfLongs(String baseName)
    {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.LONG.fieldOf(baseName + "1").forGetter(UUID::getMostSignificantBits),
                Codec.LONG.fieldOf(baseName + "0").forGetter(UUID::getLeastSignificantBits)
        ).apply(instance, UUID::new));
    }

    public static final Codec<Pair<UUID, Container>> ENTRY_CODEC = Named.of(RecordCodecBuilder.create(
            instance -> instance.group(
                    uuidPairOfLongs("PlayerUUID").forGetter(Pair::getFirst),
                    Container.CODEC.forGetter(Pair::getSecond)
            ).apply(instance, Pair::of)), "InventoryManager ENTRY_CODEC");

    public static final Codec<InventoryManager> CODEC = Named.of(RecordCodecBuilder.create(
            instance -> instance.group(
                    Container.CODEC.forGetter(i -> i.global),
                    ENTRY_CODEC.listOf().fieldOf("Private").forGetter(i -> i.perPlayer.entrySet().stream().map(kv -> Pair.of(kv.getKey(), kv.getValue())).toList())
            ).apply(instance, InventoryManager::new)), "InventoryManage CODEC");

    private static final SavedDataType<InventoryManager> TYPE = new SavedDataType<>(
            DATA_NAME, InventoryManager::new, CODEC);

    private final Container global;
    private final Map<UUID, Container> perPlayer = Maps.newHashMap();

    public InventoryManager()
    {
        this(new Container(), List.of());
    }

    private InventoryManager(Container global, List<Pair<UUID, Container>> pairs)
    {
        this.global = global;
        this.global.setManager(this);
        pairs.forEach(pair -> {
            var inv = pair.getSecond();
            inv.setManager(this);
            perPlayer.put(pair.getFirst(), inv);
        });
    }

    private static boolean errorLogged = false;

    public static InventoryManager get(Level world)
    {
        if (!(world instanceof ServerLevel))
        {
            if (!errorLogged)
            {
                RuntimeException exc = new RuntimeException("Attempted to get the data from a client world. This is wrong.");

                LOGGER.error("Some mod attempted to get the inventory contents of an Ender Key from the client. This is not supported.", exc);
                errorLogged = true;
            }
            return null;
        }
        ServerLevel overworld = world.getServer().overworld();

        DimensionDataStorage storage = overworld.getDataStorage();
        return storage.computeIfAbsent(TYPE);
    }

    @Override
    public void makeDirty()
    {
        setDirty();
    }

    public EnderInventory getInventory(long id)
    {
        return global.getInventory(id);
    }

    public IInventoryManager getPrivate(UUID uuid)
    {
        Container container = perPlayer.get(uuid);
        if (container == null)
        {
            container = new Container();
            perPlayer.put(new UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()), container);
            makeDirty();
        }

        return container;
    }

    public static class Container implements IInventoryManager
    {
        public static final Codec<Pair<Long, EnderInventory>> ENTRY_CODEC = Named.of(RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.LONG.fieldOf("Key").forGetter(Pair::getFirst),
                        EnderInventory.CODEC.fieldOf("Contents").forGetter(Pair::getSecond)
                ).apply(instance, Pair::of)), "Container ENTRY_CODEC");

        public static final MapCodec<Container> CODEC = MapNamed.of(RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        ENTRY_CODEC.listOf().fieldOf("Inventories").forGetter(i -> i.inventories.entrySet().stream().map(kv -> Pair.of(kv.getKey(), kv.getValue())).toList())
                ).apply(instance, Container::new)), "Container CODEC");


        private InventoryManager manager;
        private final Map<Long, EnderInventory> inventories = Maps.newHashMap();

        private Container()
        {
        }

        private Container(List<Pair<Long, EnderInventory>> pairs)
        {
            pairs.forEach(pair -> {
                var inv = pair.getSecond();
                inv.setManager(this);
                inventories.put(pair.getFirst(), inv);
            });
        }

        @Override
        public EnderInventory getInventory(long id)
        {
            EnderInventory inventory = inventories.get(id);

            if (inventory == null)
            {
                inventory = new EnderInventory();
                inventory.setManager(this);
                inventories.put(id, inventory);
            }

            return inventory;
        }

        @Override
        public void makeDirty()
        {
            manager.setDirty();
        }

        public void setManager(InventoryManager manager)
        {
            this.manager = manager;
        }
    }

    public static class Named<T> implements Codec<T>
    {
        public static <X> Named<X> of(Codec<X> codec, String debugName) { return new Named<>(codec, debugName); }

        private final Codec<T> inner;
        private final String debugName;

        public Named(Codec<T> inner, String debugName)
        {
            this.inner = inner;
            this.debugName = debugName;
        }

        @Override
        public <T1> DataResult<Pair<T, T1>> decode(DynamicOps<T1> ops, T1 input)
        {
            return inner.decode(ops, input).mapError(err -> "Error decoding [[" + debugName + "]]: " + err);
        }

        @Override
        public <T1> DataResult<T1> encode(T input, DynamicOps<T1> ops, T1 prefix)
        {
            return inner.encode(input, ops, prefix).mapError(err -> "Error encoding [[" + debugName + "]]: " + err);
        }
    }

    public static class MapNamed<T> extends MapCodec<T>
    {
        public static <X> MapNamed<X> of(MapCodec<X> codec, String debugName) { return new MapNamed<>(codec, debugName); }

        private final MapCodec<T> inner;
        private final String debugName;

        public MapNamed(MapCodec<T> inner, String debugName)
        {
            this.inner = inner;
            this.debugName = debugName;
        }

        @Override
        public <T1> Stream<T1> keys(DynamicOps<T1> ops)
        {
            return inner.keys(ops);
        }

        @Override
        public <T1> DataResult<T> decode(DynamicOps<T1> ops, MapLike<T1> input)
        {
            return inner.decode(ops, input).mapError(err -> "Error decoding [[" + debugName + "]]: " + err);
        }

        @Override
        public <T1> RecordBuilder<T1> encode(T input, DynamicOps<T1> ops, RecordBuilder<T1> prefix)
        {
            return inner.encode(input, ops, prefix).mapError(err -> "Error encoding [[" + debugName + "]]: " + err);
        }
    }
}
