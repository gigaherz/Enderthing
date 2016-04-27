package gigaherz.enderthing.client;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.IModProxy;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy implements IModProxy
{
    @Override
    public void preInit()
    {
        OBJLoader.instance.addDomain(Enderthing.MODID);

        ClientRegistry.bindTileEntitySpecialRenderer(TileEnderKeyChest.class, new RenderEnderKeyChest());

        registerBlockModelAsItem(Enderthing.blockEnderKeyChest, 0, "facing=north,inventory=true,private=false");
        registerBlockModelAsItem(Enderthing.blockEnderKeyChest, 8, "facing=north,inventory=true,private=true");
        registerItemModel(Enderthing.enderKey, 0, "enderKey");
        registerItemModel(Enderthing.enderKey, 1, "privateEnderKey");
        registerItemModel(Enderthing.enderLock, 0, "enderLock");
        registerItemModel(Enderthing.enderLock, 1, "privateEnderLock");
        registerItemModel(Enderthing.enderPack, 0, "enderPack");
        registerItemModel(Enderthing.enderPack, 1, "privateEnderPack");
    }

    public void registerBlockModelAsItem(final Block block, int meta, final String itemModelVariant)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta,
                new ModelResourceLocation(block.getRegistryName(), itemModelVariant));
    }

    public void registerItemModel(final Item item, int meta, final String itemModelName)
    {
        ModelLoader.setCustomModelResourceLocation(item, meta,
                new ModelResourceLocation(Enderthing.MODID + ":" + itemModelName, "inventory"));
    }

    @Override
    public void init()
    {
    }
}
