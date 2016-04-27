package gigaherz.enderthing.storage;

import gigaherz.enderthing.Enderthing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class PrivateInventoryManager
{
    private static final ResourceLocation KEY = new ResourceLocation(Enderthing.MODID, "PrivateInventories");

    @CapabilityInject(PrivateInventoryContainer.class)
    public static Capability<PrivateInventoryContainer> INSTANCE;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(PrivateInventoryContainer.class, new Capability.IStorage<PrivateInventoryContainer>()
        {
            @Override
            public NBTBase writeNBT(Capability<PrivateInventoryContainer> capability, PrivateInventoryContainer instance, EnumFacing side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<PrivateInventoryContainer> capability, PrivateInventoryContainer instance, EnumFacing side, NBTBase nbt)
            {

            }
        }, new Callable<PrivateInventoryContainer>()
        {
            @Override
            public PrivateInventoryContainer call() throws Exception
            {
                return null;
            }
        });

        MinecraftForge.EVENT_BUS.register(new PrivateInventoryManager());
    }

    public static PrivateInventoryContainer get(EntityPlayer player)
    {
        if (player.hasCapability(INSTANCE, null))
            return player.getCapability(INSTANCE, null);
        return null;
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent.Entity ev)
    {
        if (!(ev.getEntity() instanceof EntityPlayer))
            return;

        ev.addCapability(KEY, new ICapabilitySerializable<NBTTagCompound>()
        {
            final PrivateInventoryContainer container = new PrivateInventoryContainer();

            @Override
            public NBTTagCompound serializeNBT()
            {
                return container.serializeNBT();
            }

            @Override
            public void deserializeNBT(NBTTagCompound nbt)
            {
                container.deserializeNBT(nbt);
            }

            @Override
            public boolean hasCapability(Capability<?> capability, EnumFacing facing)
            {
                return capability == INSTANCE;
            }

            @Override
            public <T> T getCapability(Capability<T> capability, EnumFacing facing)
            {
                if (capability == INSTANCE)
                    return INSTANCE.cast(container);

                return null;
            }
        });
    }

    public static class PrivateInventoryContainer implements INBTSerializable<NBTTagCompound>, IInventoryManager
    {
        private Map<Integer, EnderKeyInventory> inventories = new HashMap<Integer, EnderKeyInventory>();

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
        public NBTTagCompound serializeNBT()
        {
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagList inventories = new NBTTagList();

            for (Map.Entry<Integer, EnderKeyInventory> entry : this.inventories.entrySet())
            {
                EnderKeyInventory inventory = entry.getValue();

                NBTTagCompound inventoryTag = new NBTTagCompound();
                inventoryTag.setInteger("InventoryId", entry.getKey());
                inventoryTag.setTag("InventoryContents", inventory.serializeNBT());
                inventories.appendTag(inventoryTag);
            }

            tag.setTag("Inventories", inventories);
            return tag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt)
        {
            NBTTagList nbtTagList = nbt.getTagList("Inventories", Constants.NBT.TAG_COMPOUND);

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
        public void markDirty()
        {

        }
    }
}
