package gigaherz.enderthing.storage;

import gigaherz.enderthing.Enderthing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.Callable;

@Deprecated
public class PrivateInventoryCapability
{
    private static final ResourceLocation KEY = new ResourceLocation(Enderthing.MODID, "PrivateInventories");

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IInventoryManager.class, new Capability.IStorage<IInventoryManager>()
        {
            @Override
            public NBTBase writeNBT(Capability<IInventoryManager> capability, IInventoryManager instance, EnumFacing side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<IInventoryManager> capability, IInventoryManager instance, EnumFacing side, NBTBase nbt)
            {

            }
        }, new Callable<IInventoryManager>()
        {
            @Override
            public IInventoryManager call() throws Exception
            {
                return null;
            }
        });

        MinecraftForge.EVENT_BUS.register(new PrivateInventoryCapability());
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent.Entity ev)
    {
        if (!(ev.getEntity() instanceof EntityPlayer))
            return;

        final EntityPlayer player = (EntityPlayer)ev.getEntity();

        ev.addCapability(KEY, new ICapabilitySerializable<NBTTagCompound>()
        {
            final EntityPlayer owner = player;

            @Override
            public NBTTagCompound serializeNBT()
            {
                return new NBTTagCompound();
            }

            @Override
            public void deserializeNBT(NBTTagCompound nbt)
            {
                InventoryManager.get(owner.worldObj).importCapabilityData(owner, nbt);
            }

            @Override
            public boolean hasCapability(Capability<?> capability, EnumFacing facing)
            {
                return false;
            }

            @Override
            public <T> T getCapability(Capability<T> capability, EnumFacing facing)
            {
                return null;
            }
        });
    }

}
