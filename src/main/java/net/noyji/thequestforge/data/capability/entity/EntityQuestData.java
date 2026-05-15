package net.noyji.thequestforge.data.capability.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.noyji.thequestforge.data.quest.entity.Quest;

import java.util.ArrayList;
import java.util.List;

public class EntityQuestData {
    private List<Quest> entityQuests = new ArrayList<>();
    private boolean isQuestGiver = false;
    private boolean isCheck = false;
    private long lastResetCycle = 0;


    public boolean isEmpty(){
        return ((entityQuests == null || entityQuests.isEmpty()) && !isQuestGiver && !isCheck && lastResetCycle == 0);
    }

    public void resetQuest(){
        entityQuests.clear();
    }

    public long getLastResetCycle() {
        return lastResetCycle;
    }

    public void setLastResetCycle(long lastResetCycle) {
        this.lastResetCycle = lastResetCycle;
    }

    public boolean hasQuest(){
        return (entityQuests != null && !entityQuests.isEmpty());
    }

    public Quest getQuest(){
        if (entityQuests == null || entityQuests.isEmpty()) return null;
        return entityQuests.get(0);
    }

    public Quest getQuest(int index){
        if (entityQuests == null || entityQuests.isEmpty()) return null;

        if (index < 0 || index >= entityQuests.size()) {
            return null;
        }

        return entityQuests.get(index);
    }

    public boolean isQuestGiver(){
        return isQuestGiver;
    }

    public void makeHimQuestGiver(){
        isQuestGiver = true;
    }

    public boolean isCheck(){
        return isCheck;
    }

    public void checked(){
        isCheck = true;
    }

    public int getChainLength(){
        return entityQuests.size();
    }

    public void addQuest(Quest quest){
        if (quest == null) return;
        entityQuests.add(quest);
    }

    public CompoundTag serializeNBT() {
        CompoundTag save = new CompoundTag();

        ListTag questListTag = new ListTag();
        for (Quest quest : entityQuests) {
            questListTag.add(quest.serializeNBT());
        }
        save.put("quests", questListTag);

        save.putBoolean("is_quest_giver", isQuestGiver);
        save.putBoolean("is_check", isCheck);
        save.putLong("LastResetCycle", lastResetCycle);

        return save;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.entityQuests.clear();
        if (nbt.contains("quests", Tag.TAG_LIST)){
            ListTag questListTag = nbt.getList("quests", Tag.TAG_COMPOUND);
            for (int i = 0; i < questListTag.size(); i++) {
                Quest quest = new Quest();
                quest.deserializeNBT(questListTag.getCompound(i));
                this.entityQuests.add(quest);
            }
        }
        isQuestGiver = nbt.getBoolean("is_quest_giver");
        isCheck = nbt.getBoolean("is_check");
        lastResetCycle = nbt.getLong("LastResetCycle");
    }
}
