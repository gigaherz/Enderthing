package gigaherz.enderthing.gui;

import gigaherz.enderthing.blocks.EnderKeyChestTileEntity;
import gigaherz.enderthing.util.ILongAccessor;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class Containers
{
    private static final String GLOBAL_TITLE = "container.enderthing.global";
    private static final String PRIVATE_TITLE = "container.enderthing.private";
    private static final String CODE_TITLE = "container.enderthing.passcode";

    private static ITextComponent getDisplayName(boolean priv)
    {
        return new TranslationTextComponent(priv ? PRIVATE_TITLE : GLOBAL_TITLE);
    }

    public static void openBlockGui(ServerPlayerEntity player, EnderKeyChestTileEntity chest)
    {
        openGui(player, chest.isPrivate(), -1, (id, inv, p) -> new KeyContainer(id, inv, chest));
    }

    public static void openItemGui(ServerPlayerEntity player, boolean isPrivate, int slot, long key, @Nullable UUID bound, @Nullable TileEntity te)
    {
        openGui(player, isPrivate, slot, (id, inv, p) -> new KeyContainer(id, inv, te, isPrivate, slot, key, bound));
    }

    private static void openGui(ServerPlayerEntity player, boolean isPrivate, int slot, IContainerProvider provider)
    {
        NetworkHooks.openGui(player, new SimpleNamedContainerProvider(
                provider,
                getDisplayName(isPrivate)
        ), packet -> packet.writeInt(slot));
        player.addStat(Stats.OPEN_ENDERCHEST);
    }

    public static void openPasscodeScreen(ServerPlayerEntity player, ILongAccessor code, ItemStack previewBase)
    {
        NetworkHooks.openGui(player, new SimpleNamedContainerProvider(
                (id, inv, p) -> new PasscodeContainer(id, inv, code, previewBase),
                new TranslationTextComponent(CODE_TITLE)
        ), packet -> {
            packet.writeLong(code.get());
            packet.writeItemStack(previewBase);
        });
    }
}
