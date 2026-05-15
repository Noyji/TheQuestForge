package net.noyji.thequestforge.data.capability.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.noyji.thequestforge.config.ServerConfig;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class DialogStage {
    private final Map<UUID, String> dialogProgress = new LinkedHashMap<>(16, 0.75F, true){
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, String> eldest) {
            return size() > ServerConfig.MAX_REMEMBERED_NPCS.get();
        }
    };

    public void put(UUID uuid, String stage){
        dialogProgress.put(uuid, stage);
    }

    public String get(UUID uuid){
       return dialogProgress.get(uuid);
    }

    public void remove(UUID uuid){
        dialogProgress.remove(uuid);
    }

    public void clear(){
        dialogProgress.clear();
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag dialogList = new ListTag();

        for (Map.Entry<UUID, String> entry : this.dialogProgress.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("giver_uuid", entry.getKey());
            entryTag.putString("dialog_node", entry.getValue());
            dialogList.add(entryTag);
        }

        nbt.put("dialog_progress", dialogList);

        return nbt;
    }

    public void deserializeNBT(@NotNull CompoundTag nbt) {
        this.dialogProgress.clear();

        if (nbt.contains("dialog_progress", Tag.TAG_LIST)) {
            ListTag dialogList = nbt.getList("dialog_progress", Tag.TAG_COMPOUND);

            for (int i = 0; i < dialogList.size(); i++) {
                CompoundTag entryTag = dialogList.getCompound(i);

                if (entryTag.hasUUID("giver_uuid") && entryTag.contains("dialog_node")) {
                    UUID giverId = entryTag.getUUID("giver_uuid");
                    String dialogNode = entryTag.getString("dialog_node");
                    this.dialogProgress.put(giverId, dialogNode);
                }
            }
        }
    }
}
