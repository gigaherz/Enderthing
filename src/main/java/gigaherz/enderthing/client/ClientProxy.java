package gigaherz.enderthing.client;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.IModProxy;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;

public class ClientProxy implements IModProxy
{
    @Override
    public void preInit()
    {
        OBJLoader.INSTANCE.addDomain(Enderthing.MODID);

        registerBlockModelAsItem(Enderthing.sharedChest);

    }

    public void registerBlockModelAsItem(final Block block)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0,
                new ModelResourceLocation(block.getRegistryName(), "inventory"));
    }


    @Override
    public void init()
    {
    }
}
