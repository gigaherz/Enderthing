package gigaherz.enderthing;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.Random;

public class InventoryHelper
{
    private static final Random RANDOM = new Random();

    public static void spawnItemStack(World worldIn, double x, double y, double z, ItemStack stack)
    {
        float randX = RANDOM.nextFloat() * 0.8F + 0.1F;
        float randY = RANDOM.nextFloat() * 0.8F + 0.1F;
        float randZ = RANDOM.nextFloat() * 0.8F + 0.1F;

        while (stack.stackSize > 0)
        {
            int i = RANDOM.nextInt(21) + 10;

            if (i > stack.stackSize)
            {
                i = stack.stackSize;
            }

            stack.stackSize -= i;
            EntityItem entityitem = new EntityItem(worldIn, x + randX, y + randY, z + randZ,
                    new ItemStack(stack.getItem(), i, stack.getMetadata()));

            if (stack.hasTagCompound())
            {
                entityitem.getEntityItem().setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
            }

            float velocity = 0.05F;
            entityitem.motionX = RANDOM.nextGaussian() * velocity;
            entityitem.motionY = RANDOM.nextGaussian() * velocity + 0.20000000298023224D;
            entityitem.motionZ = RANDOM.nextGaussian() * velocity;
            worldIn.spawnEntityInWorld(entityitem);
        }
    }
}
