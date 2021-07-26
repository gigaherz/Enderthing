package gigaherz.enderthing.gui;

import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import gigaherz.enderthing.util.ILongAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class Containers
{
    private static final String GLOBAL_TITLE = "container.enderthing.global";
    private static final String PRIVATE_TITLE = "container.enderthing.private";
    private static final String CODE_TITLE = "container.enderthing.passcode";

    private static Component getDisplayName(boolean priv)
    {
        return new TranslatableComponent(priv ? PRIVATE_TITLE : GLOBAL_TITLE);
    }

    public static void openBlockGui(ServerPlayer player, EnderKeyChestTileEntity chest)
    {
        openGui(player, chest.isPrivate(), -1, (id, inv, p) -> new KeyContainer(id, inv, chest));
    }

    public static void openItemGui(ServerPlayer player, boolean isPrivate, int slot, long key, @Nullable UUID bound)
    {
        openGui(player, isPrivate, slot, (id, inv, p) -> new KeyContainer(id, inv, isPrivate, slot, key, bound));
    }

    public static void openItemGui(ServerPlayer player, boolean isPrivate, int slot, long key, @Nullable UUID bound, EnderKeyChestTileEntity te)
    {
        openGui(player, isPrivate, slot, (id, inv, p) -> new KeyContainer(id, inv, te, isPrivate, slot, key, bound));
    }

    public static void openItemGui(ServerPlayer player, boolean isPrivate, int slot, long key, @Nullable UUID bound, EnderChestBlockEntity te)
    {
        openGui(player, isPrivate, slot, (id, inv, p) -> new KeyContainer(id, inv, te, isPrivate, slot, key, bound));
    }

    private static void openGui(ServerPlayer player, boolean isPrivate, int slot, MenuConstructor provider)
    {
        NetworkHooks.openGui(player, new SimpleMenuProvider(
                provider,
                getDisplayName(isPrivate)
        ), packet -> packet.writeInt(slot));
        player.awardStat(Stats.OPEN_ENDERCHEST);
    }

    public static void openPasscodeScreen(ServerPlayer player, ILongAccessor code, ItemStack previewBase)
    {
        NetworkHooks.openGui(player, new SimpleMenuProvider(
                (id, inv, p) -> new PasscodeContainer(id, inv, code, previewBase),
                new TranslatableComponent(CODE_TITLE)
        ), packet -> {
            packet.writeLong(code.get());
            packet.writeItem(previewBase);
        });
    }
}
