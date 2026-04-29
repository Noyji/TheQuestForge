package net.noyji.thequestforge.data.capability.entity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityQuestProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<EntityQuestData> ENTITY_QUEST_DATA = CapabilityManager.get(new CapabilityToken<>() {});

    private EntityQuestData data = null;

    private final LazyOptional<EntityQuestData> optional = LazyOptional.of(this::getOrCreate);

    private EntityQuestData getOrCreate() {
        if (this.data == null) {
            this.data = new EntityQuestData();
        }
        return this.data;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ENTITY_QUEST_DATA) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return getOrCreate().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getOrCreate().deserializeNBT(nbt);
    }

    public void invalidate() {
        optional.invalidate();
    }
}