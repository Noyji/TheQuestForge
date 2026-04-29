package net.noyji.thequestforge.data.quest.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.task.AbstractTask;
import net.noyji.thequestforge.data.quest.entity.components.QuestDialog;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;
import net.noyji.thequestforge.data.quest.player.components.QuestRarity;
import net.noyji.thequestforge.data.quest.player.components.QuestType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Quest extends PlayerQuest {
    private Map<String, QuestDialog> dialogs = new HashMap<>()  ;

    public Quest(){}

    public Quest(ResourceLocation sourceTemplate, UUID id, int timeLimit, QuestType type, QuestRarity rarity, int nameIndex,
                 int descriptionIndex, int xp, int currency, boolean complete, List<AbstractTask<?>> tasks, List<ItemStack> rewards,
                 Map<String, QuestDialog> dialogs) {
        super(sourceTemplate, id, timeLimit, type, rarity, nameIndex, descriptionIndex, xp, currency, complete, tasks, rewards);
        this.dialogs = dialogs;
    }

    public PlayerQuest copyToPlayerQuest(){
        return new PlayerQuest(sourceTemplate ,id, timeLimit, type, rarity, nameIndex, descriptionIndex, xp, currency, complete, tasks, rewards);
    }

    @Nullable
    public QuestDialog getDialog(String key){
        return dialogs.get(key);
    }

    @Override
    public void questInfo() {
        super.questInfo();

        for (Map.Entry<String, QuestDialog> entry : dialogs.entrySet()){
            TheQuestForge.LOGGER.debug("Key: {}", entry.getKey());
            entry.getValue().info();
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag save = super.serializeNBT();

        if (dialogs == null || dialogs.isEmpty()) return save;

        CompoundTag dialogTag = new CompoundTag();
        for (Map.Entry<String, QuestDialog> entry : dialogs.entrySet()){
            dialogTag.put(entry.getKey(), entry.getValue().serializeNBT());
        }
        save.put("dialogs", dialogTag);
        return save;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);

        if (nbt.contains("dialogs", Tag.TAG_COMPOUND)){
            CompoundTag dialogTag = nbt.getCompound("dialogs");

            for (String key : dialogTag.getAllKeys()){
                QuestDialog dialog = new QuestDialog();
                dialog.deserializeNBT(dialogTag.getCompound(key));
                dialogs.put(key, dialog);
            }
        }
    }
}
