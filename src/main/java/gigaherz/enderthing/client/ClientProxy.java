package gigaherz.enderthing.client;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.IModProxy;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
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
        OBJLoader.INSTANCE.addDomain(Enderthing.MODID);

        ClientRegistry.bindTileEntitySpecialRenderer(TileEnderKeyChest.class, new RenderEnderKeyChest());

        registerBlockModelAsItem(Enderthing.blockEnderKeyChest);
        registerItemModel(Enderthing.enderKey);
        registerItemModel(Enderthing.enderLock);
    }

    public void registerBlockModelAsItem(final Block block)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0,
                new ModelResourceLocation(block.getRegistryName(), "inventory"));
    }

    public void registerItemModel(final Item item)
    {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    @Override
    public void init()
    {
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                new IItemColor()
                {
                    @Override
                    public int getColorFromItemstack(ItemStack stack, int tintIndex)
                    {
                        int color1 = 0;
                        int color2 = 0;
                        int color3 = 0;

                        NBTTagCompound tag = stack.getTagCompound();
                        if (tag != null)
                        {
                            color1 = tag.getByte("Color1");
                            color2 = tag.getByte("Color2");
                            color3 = tag.getByte("Color3");
                        }

                        EnumDyeColor c1 = EnumDyeColor.byMetadata(color1);
                        EnumDyeColor c2 = EnumDyeColor.byMetadata(color2);
                        EnumDyeColor c3 = EnumDyeColor.byMetadata(color3);

                        switch (tintIndex)
                        {
                            case 1:
                                return c1.getMapColor().colorValue;
                            case 2:
                                return c2.getMapColor().colorValue;
                            case 3:
                                return c3.getMapColor().colorValue;
                        }

                        return 0xFFFFFFFF;
                    }
                }, Enderthing.enderKey, Enderthing.enderLock);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                new IItemColor()
                {
                    @Override
                    public int getColorFromItemstack(ItemStack stack, int tintIndex)
                    {
                        int color1 = 0;
                        int color2 = 0;
                        int color3 = 0;

                        NBTTagCompound tag = stack.getTagCompound();
                        if (tag != null)
                        {
                            NBTTagCompound etag = tag.getCompoundTag("BlockEntityTag");
                            if (etag != null)
                            {
                                int id = etag.getInteger("InventoryId");

                                color1 = id & 15;
                                color2 = (id >> 4) & 15;
                                color3 = (id >> 8) & 15;
                            }
                        }

                        EnumDyeColor c1 = EnumDyeColor.byMetadata(color1);
                        EnumDyeColor c2 = EnumDyeColor.byMetadata(color2);
                        EnumDyeColor c3 = EnumDyeColor.byMetadata(color3);

                        switch (tintIndex)
                        {
                            case 1:
                                return c1.getMapColor().colorValue;
                            case 2:
                                return c2.getMapColor().colorValue;
                            case 3:
                                return c3.getMapColor().colorValue;
                        }

                        return 0xFFFFFFFF;
                    }
                }, Enderthing.blockEnderKeyChest);
    }
}
