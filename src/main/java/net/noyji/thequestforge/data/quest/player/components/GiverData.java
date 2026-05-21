package net.noyji.thequestforge.data.quest.player.components;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class GiverData {
    private int entityId;
    private BlockPos giverPos;
    private ResourceKey<Level> dimension;

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public BlockPos getGiverPos() {
        return giverPos;
    }

    public void setGiverPos(BlockPos giverPos) {
        this.giverPos = giverPos;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public void setDimension(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        nbt.putInt("giver_id", this.entityId);

        if (this.giverPos != null) {
            nbt.putLong("giver_pos", this.giverPos.asLong());
        }

        if (this.dimension != null) {
            nbt.putString("giver_dimension", this.dimension.location().toString());
        }

        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.entityId = nbt.getInt("giver_id");

        if (nbt.contains("giver_pos")) {
            this.giverPos = BlockPos.of(nbt.getLong("giver_pos"));
        } else {
            this.giverPos = null;
        }

        if (nbt.contains("giver_dimension")) {
            String dimString = nbt.getString("giver_dimension");
            ResourceLocation dimLocation = ResourceLocation.parse(dimString);
            this.dimension = ResourceKey.create(Registries.DIMENSION, dimLocation);
        } else {
            this.dimension = Level.OVERWORLD;
        }
    }
}
