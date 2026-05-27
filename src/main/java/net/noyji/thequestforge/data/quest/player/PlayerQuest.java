package net.noyji.thequestforge.data.quest.player;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.registry.TaskHandlerRegistry;
import net.noyji.thequestforge.api.quest.task.AbstractTask;
import net.noyji.thequestforge.api.quest.task.TaskType;
import net.noyji.thequestforge.data.quest.player.components.GiverData;
import net.noyji.thequestforge.data.quest.player.components.QuestRarity;
import net.noyji.thequestforge.data.quest.player.components.QuestType;
import net.noyji.thequestforge.network.TheQuestForgeNetworking;
import net.noyji.thequestforge.network.s2c.QuestToastS2CPacket;
import net.noyji.thequestforge.network.s2c.SyncGiverPosS2CPacket;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
    protected List<AbstractTask<?>> tasks = new ArrayList<>();
    protected List<ItemStack> rewards = new ArrayList<>();
    private GiverData giverData = new GiverData();

    protected Map<String, String> customData = new HashMap<>();


    private String spareDialogKey;

    public PlayerQuest(){}

    public PlayerQuest(ResourceLocation sourceTemplate, UUID id, int timeLimit, QuestType type, QuestRarity rarity, int nameIndex, int descriptionIndex,
                       int xp, int currency, boolean complete, List<AbstractTask<?>> tasks, List<ItemStack> rewards, GiverData giverData) {
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
        this.giverData = giverData;
    }

    public GiverData getGiverData(){
        return giverData;
    }

    public void setGiverDataAndSync(BlockPos blockPos, ResourceKey<Level> dimension, int entityId, String name, Player player){
        giverData.setGiverPos(blockPos);
        giverData.setDimension(dimension);
        giverData.setEntityId(entityId);
        giverData.setName(name);
        TheQuestForgeNetworking.sendToPlayer(new SyncGiverPosS2CPacket(id, giverData.serializeNBT()), player);
    }

    public void setCustomData(Map<String, String> customData) {
        this.customData = customData;
    }

    public void addCustomData(String key, String data){
        if (key == null || key.isEmpty()) return;
        if (data == null || data.isEmpty()) return;

        customData.put(key, data);
    }

    @Nullable
    public String getCustomData(String key){
        if (key == null || key.isEmpty()) return null;

        return customData.get(key);
    }

    public void setGiverDataAndSync(CompoundTag data){
        giverData.deserializeNBT(data);
    }

    public QuestType getType() {
        return type;
    }

    public String getSpareDialogKey() {
        return (spareDialogKey == null) ? "start" : spareDialogKey;
    }

    public void setSpareDialogKey(String spareDialogKey) {
        this.spareDialogKey = spareDialogKey;
    }

    public List<ItemStack> getRewards(){
        return rewards;
    }

    public ItemStack getReward(int index){
        if (rewards == null || rewards.isEmpty()) return null;
        if (index < 0 || index > rewards.size() - 1) return null;
        return rewards.get(index);
    }

    public int getXp() {
        return xp;
    }

    public int getCurrency() {
        return currency;
    }

    public List<AbstractTask<?>> getTasks(){
        return tasks;
    }
    @Nullable
    public AbstractTask<?> getTask(int index){
        if (tasks == null || tasks.isEmpty()) return null;
        if (index < 0 || index > tasks.size() - 1) return null;
        return tasks.get(index);
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

    public void setTimeLimit(int timeLimit){
        this.timeLimit = timeLimit;
    }

    public boolean updateTask(ResourceLocation taskTypeKey, ResourceLocation target, Event event, Player player){

        boolean progressChanged = false;
        int completedCount = 0;

        for (AbstractTask<?> abstractTask : tasks){
            if (abstractTask.taskIs(taskTypeKey, target)){
                abstractTask.tryHandle(event);
                progressChanged = true;
            }

            if (abstractTask.isComplete()){
                completedCount++;
            }
        }

        boolean wasComplete = this.complete;

        if (!complete){
            this.complete = (completedCount == this.tasks.size());

            if (complete) {
                TheQuestForgeNetworking.sendToPlayer(new QuestToastS2CPacket(this.id), player);
            }

        } else {
            this.complete = (completedCount == this.tasks.size());
        }

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

        if (this.sourceTemplate != null) {
            save.putString("source_template", this.sourceTemplate.toString());
        }
        if (this.id != null) {
            save.putUUID("id", this.id);
        }

        save.putInt("time_limit", this.timeLimit);
        save.putString("type", this.type.name());
        save.putString("rarity", this.rarity.name());
        save.putInt("name_index", this.nameIndex);
        save.putInt("description_index", this.descriptionIndex);
        save.putInt("xp", this.xp);
        save.putInt("currency", this.currency);
        save.putBoolean("complete", this.complete);

        if (this.spareDialogKey != null && !this.spareDialogKey.isEmpty()) {
            save.putString("spare_dialog_key", this.spareDialogKey);
        }

        if (!this.customData.isEmpty()) {
            CompoundTag customDataTag = new CompoundTag();
            for (Map.Entry<String, String> entry : this.customData.entrySet()) {
                if (entry.getKey() != null && !entry.getKey().isEmpty() &&
                        entry.getValue() != null && !entry.getValue().isEmpty()) {
                    customDataTag.putString(entry.getKey(), entry.getValue());
                }
            }
            if (!customDataTag.isEmpty()) {
                save.put("custom_data", customDataTag);
            }
        }

        if (!this.tasks.isEmpty()) {
            ListTag tasksList = new ListTag();
            for (AbstractTask<?> task : this.tasks) {
                if (task == null || task.getLocation() == null) continue;

                CompoundTag taskTag = new CompoundTag();
                taskTag.putString("task_type", task.getLocation().toString());

                CompoundTag taskData = new CompoundTag();
                task.serializeNBT(taskData);
                taskTag.put("task_data", taskData);

                tasksList.add(taskTag);
            }
            if (!tasksList.isEmpty()) {
                save.put("tasks", tasksList);
            }
        }

        if (!this.rewards.isEmpty()) {
            ListTag rewardsList = new ListTag();
            for (ItemStack itemStack : this.rewards) {
                if (itemStack != null && !itemStack.isEmpty()) {
                    rewardsList.add(itemStack.save(new CompoundTag()));
                }
            }
            if (!rewardsList.isEmpty()) {
                save.put("rewards", rewardsList);
            }
        }

        if (this.giverData != null) {
            save.put("giver_pos_data", this.giverData.serializeNBT());
        }

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
            this.sourceTemplate = ResourceLocation.tryParse(nbt.getString("source_template"));
        }

        if (nbt.contains("type", Tag.TAG_STRING)) {
            try {
                this.type = QuestType.valueOf(nbt.getString("type"));
            } catch (IllegalArgumentException e) {
                this.type = QuestType.LOCAL;
            }
        }

        if (nbt.contains("rarity", Tag.TAG_STRING)) {
            try {
                this.rarity = QuestRarity.valueOf(nbt.getString("rarity"));
            } catch (IllegalArgumentException e) {
                this.rarity = QuestRarity.COMMON;
            }
        }

        if (nbt.contains("spare_dialog_key", Tag.TAG_STRING)) {
            this.spareDialogKey = nbt.getString("spare_dialog_key");
        }

        if (this.rewards != null) this.rewards.clear();
        if (nbt.contains("rewards", Tag.TAG_LIST)) {
            ListTag rewardsList = nbt.getList("rewards", Tag.TAG_COMPOUND);
            for (int i = 0; i < rewardsList.size(); i++) {
                ItemStack stack = ItemStack.of(rewardsList.getCompound(i));
                if (!stack.isEmpty()) {
                    this.rewards.add(stack);
                }
            }
        }


        if (this.tasks != null) this.tasks.clear();
        if (nbt.contains("tasks", Tag.TAG_LIST)) {
            ListTag taskList = nbt.getList("tasks", Tag.TAG_COMPOUND);
            for (int i = 0; i < taskList.size(); i++) {
                CompoundTag taskTag = taskList.getCompound(i);
                ResourceLocation typeId = ResourceLocation.tryParse(taskTag.getString("task_type"));

                if (typeId != null) {
                    TaskType<?> taskType = TaskHandlerRegistry.REGISTRY.get().getValue(typeId);

                    if (taskType != null) {
                        AbstractTask<?> task = taskType.createInstance();
                        if (task != null) {
                            task.deserializeNBT(taskTag.getCompound("task_data"));
                            this.tasks.add(task);
                        }
                    }
                }
            }
        }

        this.giverData = new GiverData();
        if (nbt.contains("giver_pos_data", Tag.TAG_COMPOUND)) {
            this.giverData.deserializeNBT(nbt.getCompound("giver_pos_data"));
        }

        this.customData.clear();
        if (nbt.contains("custom_data", Tag.TAG_COMPOUND)) {
            CompoundTag dataTag = nbt.getCompound("custom_data");
            for (String key : dataTag.getAllKeys()) {
                this.customData.put(key, dataTag.getString(key));
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
