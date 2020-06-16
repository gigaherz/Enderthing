package gigaherz.enderthing;

import gigaherz.enderthing.blocks.BlockEnderKeyChest;
import gigaherz.enderthing.blocks.TileEnderKeyChest;
import gigaherz.enderthing.gui.GuiHandler;
import gigaherz.enderthing.items.ItemEnderCard;
import gigaherz.enderthing.items.ItemEnderKey;
import gigaherz.enderthing.items.ItemEnderLock;
import gigaherz.enderthing.items.ItemEnderPack;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber
@Mod(Enderthing.MODID)
public class Enderthing
{
    public static final String MODID = "enderthing";

    @ObjectHolder("enderthing:key_chest")
    public static BlockEnderKeyChest enderKeyChest;

    @ObjectHolder("enderthing:key_chest_private")
    public static BlockEnderKeyChest enderKeyChestPrivate;

    @ObjectHolder("enderthing:key_chest_bound")
    public static BlockEnderKeyChest enderKeyChestBound;

    @ObjectHolder("enderthing:key")
    public static ItemEnderKey enderKey;

    @ObjectHolder("enderthing:key_private")
    public static ItemEnderKey enderKeyPrivate;

    @ObjectHolder("enderthing:lock")
    public static ItemEnderLock enderLock;

    @ObjectHolder("enderthing:lock_private")
    public static ItemEnderLock enderLockPrivate;

    @ObjectHolder("enderthing:pack")
    public static ItemEnderPack enderPack;

    @ObjectHolder("enderthing:pack_private")
    public static ItemEnderPack enderPackPrivate;

    @ObjectHolder("enderthing:card")
    public static ItemEnderCard enderCard;

    @ObjectHolder("enderthing:key_chest")
    public static TileEntityType<TileEnderKeyChest> tileKeyChest;
    @ObjectHolder("enderthing:key_chest_private")
    public static TileEntityType<TileEnderKeyChest.Private> tileKeyChestPrivate;

    public static Enderthing instance;

    //public static GuiHandler guiHandler = new GuiHandler();

    public static ItemGroup tabEnderthing = new ItemGroup("enderthing.things")
    {
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(enderKey);
        }
    };

    public static boolean breakChestOnHarvest = true;

    public static final Logger logger = LogManager.getLogger(MODID);

    public Enderthing()
    {
        instance = this;

        // TODO: Rune dust, rune, rune pattern

        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Block.class, this::registerBlocks);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, this::registerItems);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(TileEntityType.class, this::registerTileEntities);

        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.GUIFACTORY, () -> message -> GuiHandler.Client.getClientGuiElement(message));
    }

    public void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(
                new BlockEnderKeyChest(Block.Properties.create(Material.IRON)
                        .hardnessAndResistance(22.5F, 1000F)
                        .sound(SoundType.STONE)
                        .lightValue(5)).setRegistryName("key_chest"),
                new BlockEnderKeyChest.Private(Block.Properties.create(Material.IRON)
                        .hardnessAndResistance(22.5F, 1000F)
                        .sound(SoundType.STONE)
                        .lightValue(5)).setRegistryName("key_chest_private"),
                new BlockEnderKeyChest.Private(Block.Properties.create(Material.IRON)
                        .hardnessAndResistance(22.5F, 1000F)
                        .sound(SoundType.STONE)
                        .lightValue(5)).setRegistryName("key_chest_bound")

        );
    }

    public void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                new BlockEnderKeyChest.AsItem(enderKeyChest, false, new Item.Properties().group(tabEnderthing))
                        .setRegistryName(enderKeyChest.getRegistryName()),
                new BlockEnderKeyChest.AsItem(enderKeyChestPrivate, true, new Item.Properties().group(tabEnderthing))
                        .setRegistryName(enderKeyChestPrivate.getRegistryName()),

                new ItemEnderKey(false, new Item.Properties().group(tabEnderthing)).setRegistryName("key"),
                new ItemEnderKey(true, new Item.Properties().group(tabEnderthing)).setRegistryName("key_private"),
                new ItemEnderLock(false, new Item.Properties().group(tabEnderthing)).setRegistryName("lock"),
                new ItemEnderLock(true, new Item.Properties().group(tabEnderthing)).setRegistryName("lock_private"),
                new ItemEnderPack(false, new Item.Properties().group(tabEnderthing)).setRegistryName("pack"),
                new ItemEnderPack(true, new Item.Properties().group(tabEnderthing)).setRegistryName("pack_private"),

                new ItemEnderCard(new Item.Properties().maxStackSize(1).group(tabEnderthing)).setRegistryName("card")
        );
    }

    private void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event)
    {
        TileEntityType.register(location("key_chest").toString(), TileEntityType.Builder.create(TileEnderKeyChest::new));
        TileEntityType.register(location("key_chest_private").toString(), TileEntityType.Builder.create(TileEnderKeyChest.Private::new));
    }

        /*
    public void initRecipes()
    {
        GameRegistry.addRecipe(new ItemStack(enderCard),
                "nnn",
                "ppp",
                "nnn",
                'n', Items.GOLD_NUGGET,
                'p', Items.PAPER);

        GameRegistry.addRecipe(new KeyRecipe());
        GameRegistry.addRecipe(new LockRecipe());
        GameRegistry.addRecipe(new PackRecipe());
        GameRegistry.addRecipe(new MakePrivateRecipe(enderKey));
        GameRegistry.addRecipe(new MakePrivateRecipe(enderLock));
        GameRegistry.addRecipe(new MakePrivateRecipe(enderPack));
        GameRegistry.addRecipe(new MakePrivateRecipe(enderKeyChest));
        GameRegistry.addRecipe(new ChangeColorsRecipe(enderKey, false));
        GameRegistry.addRecipe(new ChangeColorsRecipe(enderLock, false));
        GameRegistry.addRecipe(new ChangeColorsRecipe(enderPack, false));
        GameRegistry.addRecipe(new ChangeColorsRecipe(enderKeyChest, false));
        GameRegistry.addRecipe(new ChangeColorsRecipe(enderKey, true));
        GameRegistry.addRecipe(new ChangeColorsRecipe(enderLock, true));
        GameRegistry.addRecipe(new ChangeColorsRecipe(enderPack, true));
        GameRegistry.addRecipe(new ChangeColorsRecipe(enderKeyChest, true));

        RecipeSorter.register(MODID + ":ender_key", KeyRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register(MODID + ":ender_lock", LockRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register(MODID + ":ender_pack", PackRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register(MODID + ":make_private", MakePrivateRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register(MODID + ":change_color", ChangeColorsRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");

    }
    */

    public static ResourceLocation location(String path)
    {
        return new ResourceLocation(MODID, path);
    }

    public static long getItemKey(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTag();
        if (tag != null)
        {
            return tag.getLong("Key");
        }

        return -1;
    }

    public static long getBlockKey(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTag();
        if (tag != null)
        {
            NBTTagCompound etag = tag.getCompound("BlockEntityTag");
            if (etag != null)
            {
                return etag.getLong("InventoryId");
            }
        }

        return -1;
    }

    public static long getKey(ItemStack stack)
    {
        if (stack.getItem() instanceof ItemBlock)
            return getBlockKey(stack);
        return getItemKey(stack);
    }

    public static long getKey(IBlockReader world, BlockPos pos)
    {
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEnderKeyChest)
        {
            return ((TileEnderKeyChest) te).getKey();
        }

        return -1;
    }

    public static long getKey(TileEntity te)
    {
        if (te instanceof TileEnderKeyChest)
        {
            return ((TileEnderKeyChest) te).getKey();
        }

        return -1;
    }

    public static ItemStack getItem(IItemProvider itemProvider, long key)
    {
        ItemStack stack = new ItemStack(itemProvider, 1);

        NBTTagCompound tag = new NBTTagCompound();
        tag.putLong("Key", key);

        stack.setTag(tag);

        return stack;
    }

    public static ItemStack getLock(long key, boolean priv)
    {
        return getItem(priv ? enderLockPrivate : enderLock, key);
    }

    public static ItemStack getBlockItem(long id, boolean priv)
    {
        ItemStack stack = new ItemStack(priv ? enderKeyChestPrivate : enderKeyChest, 1);

        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound etag = new NBTTagCompound();
        etag.putLong("Key", id);
        tag.put("BlockEntityTag", etag);

        stack.setTag(tag);

        return stack;
    }

    @Nullable
    public static String queryNameFromUUID(UUID uuid)
    {
        MinecraftServer svr = ServerLifecycleHooks.getCurrentServer();
        if (svr == null)
            return null;
        PlayerList playerList = svr.getPlayerList();
        if (playerList == null)
            return null;
        EntityPlayer player = playerList.getPlayerByUUID(uuid);
        if (player != null)
            return player.getName().getString();
        return null;
    }

    public static class Client
    {
        public static void addStandardInformation(ItemStack stack, List<ITextComponent> tooltip, ITooltipFlag flag, boolean isPrivate)
        {
            if (isPrivate)
            {
                tooltip.add(new TextComponentTranslation("tooltip." + Enderthing.MODID + ".private").applyTextStyle(TextFormatting.BOLD));
            }

            long key = Enderthing.getKey(stack);

            if (key < 0)
            {
                tooltip.add(new TextComponentTranslation("tooltip." + Enderthing.MODID + ".colorMissing").applyTextStyle(TextFormatting.ITALIC));
                return;
            }

            //tooltip.add(new TextComponentTranslation("tooltip." + Enderthing.MODID + ".password", words));
        }

    }
}

