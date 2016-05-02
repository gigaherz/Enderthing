package gigaherz.enderthing.client;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.IModProxy;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.util.UUID;

public class ClientProxy implements IModProxy
{
    @Override
    public void preInit()
    {
        OBJLoader.INSTANCE.addDomain(Enderthing.MODID);

        ClientRegistry.bindTileEntitySpecialRenderer(TileEnderKeyChest.class, new RenderEnderKeyChest());

        registerBlockModelAsItem(Enderthing.blockEnderKeyChest, 0, "facing=north,inventory=true,private=false");
        registerBlockModelAsItem(Enderthing.blockEnderKeyChest, 8, "facing=north,inventory=true,private=true");
        registerItemModel(Enderthing.enderKey, 0, "enderKey");
        registerItemModel(Enderthing.enderKey, 1, "privateEnderKey");
        registerItemModel(Enderthing.enderLock, 0, "enderLock");
        registerItemModel(Enderthing.enderLock, 1, "privateEnderLock");
        registerItemModel(Enderthing.enderPack, 0, "enderPack");
        registerItemModel(Enderthing.enderPack, 1, "privateEnderPack");
        registerItemModel(Enderthing.enderCard, 0, "enderCard");
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
                }, Enderthing.enderKey, Enderthing.enderLock, Enderthing.enderPack);
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

    @Override
    public String queryNameFromUUID(ItemStack stack, UUID uuid)
    {
        MinecraftServer svr = FMLClientHandler.instance().getServer();
        if (svr == null)
            return null;
        PlayerList playerList = svr.getPlayerList();
        if(playerList == null)
            return null;
        EntityPlayer player = playerList.getPlayerByUUID(uuid);
        if(player != null)
            return player.getName();
        return null;
    }
}
