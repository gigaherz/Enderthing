package gigaherz.enderthing.client;

import gigaherz.enderthing.Enderthing;
import gigaherz.enderthing.IModProxy;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import javax.annotation.Nullable;
import java.util.UUID;

public class ClientProxy implements IModProxy
{
    @Override
    public void preInit()
    {
        OBJLoader.INSTANCE.addDomain(Enderthing.MODID);

        ClientRegistry.bindTileEntitySpecialRenderer(TileEnderKeyChest.class, new RenderEnderKeyChest());

        registerBlockModelAsItem(Enderthing.enderKeyChest, 0, "facing=north,inventory=true,private=false");
        registerBlockModelAsItem(Enderthing.enderKeyChest, 8, "facing=north,inventory=true,private=true");
        registerItemModel(Enderthing.enderKey, 0, "ender_key");
        registerItemModel(Enderthing.enderKey, 1, "ender_key_private");
        registerItemModel(Enderthing.enderLock, 0, "ender_lock");
        registerItemModel(Enderthing.enderLock, 1, "ender_lock_private");
        registerItemModel(Enderthing.enderPack, 0, "ender_pack");
        registerItemModel(Enderthing.enderPack, 1, "ender_pack_private");
        registerItemModel(Enderthing.enderCard, 0, "ender_card");
    }

    public void registerBlockModelAsItem(final Block block, int meta, final String itemModelVariant)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta,
                new ModelResourceLocation(block.getRegistryName(), itemModelVariant));
    }

    public void registerItemModel(final Item item, int meta, final String itemModelName)
    {
        ModelLoader.setCustomModelResourceLocation(item, meta,
                new ModelResourceLocation(Enderthing.location(itemModelName), "inventory"));
    }

    @Override
    public void init()
    {
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                new ItemColorHandler.ItemTag(), Enderthing.enderKey, Enderthing.enderLock, Enderthing.enderPack);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                new ItemColorHandler.BlockTag(), Enderthing.enderKeyChest);
    }

    @Nullable
    @Override
    public String queryNameFromUUID(UUID uuid)
    {
        MinecraftServer svr = FMLClientHandler.instance().getServer();
        if (svr == null)
            return null;
        PlayerList playerList = svr.getPlayerList();
        if (playerList == null)
            return null;
        EntityPlayer player = playerList.getPlayerByUUID(uuid);
        if (player != null)
            return player.getName();
        return null;
    }
}
