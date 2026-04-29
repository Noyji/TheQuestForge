package net.noyji.thequestforge.data.template.components;

import net.minecraft.nbt.CompoundTag;
import net.noyji.thequestforge.TheQuestForge;
import org.slf4j.Marker;

public class SimplyItemStack {
    private String id;
    private int count;
    private String tag;

    public boolean isEmpty(Marker marker, int index){
        TheQuestForge.LOGGER.debug(marker, "ItemStack from index {}", index);

        if (id == null || id.isEmpty()){
            TheQuestForge.LOGGER.debug(marker, "Error, missing id item");
            return false;
        }
        if (count <= 0){
            TheQuestForge.LOGGER.debug(marker, "Error count is incorrect");
            return false;
        }
        return true;
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", this.id == null ? "" : this.id);
        nbt.putInt("count", this.count);
        nbt.putString("tag", this.tag == null ? "" : this.tag);
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.id = nbt.getString("id");
        this.count = nbt.getInt("count");
        this.tag = nbt.getString("tag");
    }
}
