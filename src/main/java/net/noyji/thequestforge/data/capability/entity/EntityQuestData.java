package net.noyji.thequestforge.data.capability.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.noyji.thequestforge.data.quest.entity.Quest;

public class EntityQuestData {
    //TODO: ахитектура не годится для квестовой цепочки!!

    private Quest entityQuest = null;
    private boolean isQuestGiver = false;
    private boolean isCheck = false;

    public boolean hasQuest(){
        return (entityQuest != null);
    }

    public Quest getQuest(){
        return entityQuest;
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

    public void addQuest(Quest quest){
        if (quest == null) return;
        entityQuest = quest;
    }

    public CompoundTag serializeNBT() {
        CompoundTag save = new CompoundTag();
        if (entityQuest != null) save.put("quest", entityQuest.serializeNBT());
        save.putBoolean("is_quest_giver", isQuestGiver);
        save.putBoolean("is_check", isCheck);

        return save;
    }

    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("quest", Tag.TAG_COMPOUND)){
            Quest quest = new Quest();
            quest.deserializeNBT(nbt.getCompound("quest"));
            entityQuest = quest;
        } else {
            entityQuest = null;
        }
        isQuestGiver = nbt.getBoolean("is_quest_giver");
        isCheck = nbt.getBoolean("is_check");
    }
}
