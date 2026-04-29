package net.noyji.thequestforge.data.capability.player;

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

public class PlayerQuestProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<PlayerQuestData> PLAYER_QUEST_DATA = CapabilityManager.get(new CapabilityToken<>() {});

    private PlayerQuestData instance = null;

    private PlayerQuestData getOrCreate(){
        if (instance == null) instance = new PlayerQuestData();
        return instance;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_QUEST_DATA){
            return LazyOptional.of(this::getOrCreate).cast();
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
}
