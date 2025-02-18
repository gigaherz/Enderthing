package dev.gigaherz.enderthing.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.enderthing.KeyUtils;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record KeyColor(int layer) implements ItemTintSource
{
    public static final MapCodec<KeyColor> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(Codec.INT.fieldOf("layer").forGetter(KeyColor::layer))
                    .apply(instance, KeyColor::new));

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity)
    {
        if (layer < 1 || layer > 3)
            return -1;

        long id = KeyUtils.getKey(stack);
        if (id < 0)
            return 0xFF000000;

        int r = ((int) (id >>> ((layer - 1) * 21)) & 0x7f) * 255 / 127;
        int g = ((int) (id >>> ((layer - 1) * 21 + 7)) & 0x7f) * 255 / 127;
        int b = ((int) (id >>> ((layer - 1) * 21 + 14)) & 0x7f) * 255 / 127;

        return ARGB.color(255, r, g, b);
    }

    @Override
    public MapCodec<? extends ItemTintSource> type()
    {
        return CODEC;
    }
}
