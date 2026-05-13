package net.noyji.thequestforge.data.group.components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IParseItems {
    ResourceLocation getTarget();
    @Nullable
    CompoundTag getTag();
    @Nullable
    List<EnchantmentContext> getEnchantments(RandomSource randomSource);
}
