package net.noyji.thequestforge.data.quest.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.task.AbstractTask;
import net.noyji.thequestforge.api.quest.task.TaskType;
import net.noyji.thequestforge.api.registry.TaskHandlerRegistry;
import net.noyji.thequestforge.data.quest.player.components.QuestRarity;
import net.noyji.thequestforge.data.quest.player.components.QuestType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerQuest {
    protected ResourceLocation sourceTemplate;
    protected UUID id;
    protected int timeLimit;
    protected QuestType type;
    protected QuestRarity rarity;
    protected int nameIndex;
    protected int descriptionIndex;
    protected int xp;
    protected int currency;
    protected boolean complete;
    protected List<AbstractTask<?>> tasks;
    protected List<ItemStack> rewards;

    public PlayerQuest(){}

    public PlayerQuest(ResourceLocation sourceTemplate, UUID id, int timeLimit, QuestType type, QuestRarity rarity, int nameIndex, int descriptionIndex,
                       int xp, int currency, boolean complete, List<AbstractTask<?>> tasks, List<ItemStack> rewards) {
        this.sourceTemplate = sourceTemplate;
        this.id = id;
        this.timeLimit = timeLimit;
        this.type = type;
        this.rarity = rarity;
        this.nameIndex = nameIndex;
        this.descriptionIndex = descriptionIndex;
        this.xp = xp;
        this.currency = currency;
        this.complete = complete;
        this.tasks = tasks;
        this.rewards = rewards;
    }

    public int getNameIndex(){
        return nameIndex;
    }

    public QuestRarity getRarity(){
        return rarity;
    }

    public int getDescriptionIndex(){
        return descriptionIndex;
    }

    public int getTimeLimit(){
        return timeLimit;
    }

    public boolean updateTask(ResourceLocation taskTypeKey, ResourceLocation target, Event event){
        boolean progressChanged = false;
        int completedCount = 0;

        for (AbstractTask<?> abstractTask : tasks){
            if (!abstractTask.isComplete() && abstractTask.taskIs(taskTypeKey, target)){
                abstractTask.tryHandle(event);
                progressChanged = true;
            }

            if (abstractTask.isComplete()){
                completedCount++;
            }
        }

        boolean wasComplete = this.complete;
        this.complete = (completedCount == this.tasks.size());

        return progressChanged || (!wasComplete && this.complete);
    }

    public List<String> getLocationAndTarget(){
        List<String> result = new ArrayList<>();
        for (AbstractTask<?> task : tasks){
            result.add(task.getLocation() + "-" + task.getTarget());
        }
        return result;
    }

    public UUID getId(){
        return id;
    }

    public ResourceLocation getSourceTemplate(){
        return sourceTemplate;
    }

    public boolean isEmpty(){
        return (id == null);
    }

    public boolean isComplete(){
        return complete;
    }

    public CompoundTag serializeNBT() {
        CompoundTag save = new CompoundTag();

        if (sourceTemplate != null) save.putString("source_template", sourceTemplate.toString());
        if (id != null) save.putUUID("id", id);
        save.putInt("time_limit", timeLimit);
        save.putString("type", type.name());
        save.putString("rarity", rarity.name());
        save.putInt("name_index", nameIndex);
        save.putInt("description_index", descriptionIndex);
        save.putInt("xp", xp);
        save.putInt("currency", currency);
        save.putBoolean("complete", complete);

        ListTag tasks = new ListTag();
        for (AbstractTask<?> abstractTask : this.tasks){

            if (abstractTask == null) continue;
            CompoundTag taskTag = new CompoundTag();
            taskTag.putString("task_type", abstractTask.getLocation().toString());

            CompoundTag taskData = new CompoundTag();
            abstractTask.serializeNBT(taskData);
            taskTag.put("task_data", taskData);

            tasks.add(taskTag);
        }
        save.put("tasks", tasks);

        ListTag rewards = new ListTag();
        for (ItemStack itemStack : this.rewards){
            if (itemStack.isEmpty()) continue;

            rewards.add(itemStack.save(new CompoundTag()));
        }
        save.put("rewards", rewards);

        return save;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.timeLimit = nbt.getInt("time_limit");
        this.nameIndex = nbt.getInt("name_index");
        this.descriptionIndex = nbt.getInt("description_index");
        this.xp = nbt.getInt("xp");
        this.currency = nbt.getInt("currency");
        this.complete = nbt.getBoolean("complete");

        if (nbt.hasUUID("id")) {
            this.id = nbt.getUUID("id");
        }

        if (nbt.contains("source_template", Tag.TAG_STRING)) {
            this.sourceTemplate = ResourceLocation.parse(nbt.getString("source_template"));
        }

        if (nbt.contains("type", Tag.TAG_STRING)) {
            this.type = QuestType.valueOf(nbt.getString("type"));
        }
        if (nbt.contains("rarity", Tag.TAG_STRING)) {
            this.rarity = QuestRarity.valueOf(nbt.getString("rarity"));
        }

        this.rewards = new ArrayList<>();
        if (nbt.contains("rewards", Tag.TAG_LIST)) {
            ListTag rewardsList = nbt.getList("rewards", Tag.TAG_COMPOUND);
            for (int i = 0; i < rewardsList.size(); i++) {
                ItemStack stack = ItemStack.of(rewardsList.getCompound(i));
                if (!stack.isEmpty()) {
                    this.rewards.add(stack);
                }
            }
        }
        this.tasks = new ArrayList<>();
        if (nbt.contains("tasks", Tag.TAG_LIST)) {
            ListTag taskList = nbt.getList("tasks", Tag.TAG_COMPOUND);
            for (int i = 0; i < taskList.size(); i++) {
                CompoundTag taskTag = taskList.getCompound(i);

                ResourceLocation typeId = ResourceLocation.parse(taskTag.getString("task_type"));

                TaskType<?> taskType = TaskHandlerRegistry.REGISTRY.get().getValue(typeId);

                if (taskType != null) {
                    AbstractTask<?> task = taskType.createInstance();

                    task.deserializeNBT(taskTag.getCompound("task_data"));

                    this.tasks.add(task);
                }
            }
        }
    }

    public void questInfo(){
        TheQuestForge.LOGGER.debug("\n Quest info \n Source template: {} \n ID: {} \n Time limit: {} \n Type: {} \n Rarity: {} \n Name index: {} \n Description index: {} \n Xp: {} \n Currency: {} \n",
                sourceTemplate, id, timeLimit, type, rarity, nameIndex, descriptionIndex, xp, currency);
        for (AbstractTask<?> task : tasks){
            task.info();
        }
        for (ItemStack itemStack : rewards){
            TheQuestForge.LOGGER.debug(itemStack.toString());
        }
    }
}
