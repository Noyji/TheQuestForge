package net.noyji.thequestforge.data.template;

import com.google.gson.annotations.SerializedName;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.IWeighable;
import net.noyji.thequestforge.api.quest.registry.RequirementRegistry;
import net.noyji.thequestforge.api.quest.requirements.AbstractRequirement;
import net.noyji.thequestforge.api.quest.requirements.RequirementContext;
import net.noyji.thequestforge.common.util.QuestGenerator;
import net.noyji.thequestforge.common.util.Util;
import net.noyji.thequestforge.data.quest.entity.components.QuestDialog;
import net.noyji.thequestforge.data.quest.player.components.QuestType;
import net.noyji.thequestforge.data.template.components.Range;
import net.noyji.thequestforge.data.template.components.TemplateDialog;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestTemplate implements IWeighable {
    private String thisId;
    private String pool;
    private int weight;
    private QuestType type;
    @SerializedName("time_limit")
    private Range timeLimit;
    private List<String> requirement;
    private Map<String, List<String>> name;
    private Map<String, List<String>> description;
    @SerializedName("requirement_target")
    private List<String> requirementTarget;
    @SerializedName("guaranteed_reward")
    private List<String> guaranteedReward;
    private String group;
    @SerializedName("task_count")
    private Range taskCount;
    @SerializedName("max_reward")
    private int maxReward = 1;
    private Map<String, TemplateDialog> dialogs;
    @SerializedName("next_quest")
    private String nextQuest;

    public QuestType getType(){
        return (type == null) ? QuestType.LOCAL : type;
    }

    public List<String> getGuaranteedReward() {
        return (guaranteedReward == null) ? new ArrayList<>() : guaranteedReward;
    }

    @Nullable
    public ResourceLocation getNextQuest(){
        if (nextQuest == null || nextQuest.isEmpty()) return null;

        return TheQuestForge.parse(nextQuest);
    }

    public Component getQuestDescription(String language, int index){
        return Component.literal(Util.getTranslateTextFromMap(description, language, index));
    }

    public Component getQuestName(String language, int index){
       return Component.literal(Util.getTranslateTextFromMap(name, language, index));
    }

    @Nullable
    public TemplateDialog getDialogByKey(String key){
        if (key == null || key.isEmpty()) return null;
        return dialogs.get(key);
    }

    public Map<String, QuestDialog> getQuestDialog(RandomSource randomSource){
        Map<String, QuestDialog> result = new HashMap<>();

        for (String key : dialogs.keySet()){
            QuestDialog questDialog = dialogs.get(key).toQuestDialog(randomSource);

            if (questDialog != null){
                result.put(key, questDialog);
            }
        }
        return result;
    }

    public int getTaskCount(RandomSource randomSource){
        return taskCount.getRandomInRange(randomSource);
    }

    public int getRewardCount(){
        return maxReward;
    }

    public ResourceLocation getGroupKey(){
        return ResourceLocation.parse(group);
    }

    public int getNameIndex(RandomSource randomSource){
        return indexHelperMap(name, randomSource);
    }

    public int getDescription(RandomSource randomSource){
        return indexHelperMap(description, randomSource);
    }

    public void setThisId(String thisId) {
        this.thisId = thisId;
    }

    public ResourceLocation getThisId() {
        return ResourceLocation.parse(thisId);
    }

    public boolean hasTimeLimit(){
        return timeLimit != null && !timeLimit.isEmpty();
    }

    public int getTimeLimit(RandomSource randomSource){
        return timeLimit.getRandomInRange(randomSource);
    }

    public boolean isEmpty(){
        return  name == null || name.isEmpty();
    }

    @Override
    public int getWeight() {
        return weight;
    }

    public boolean hasPool(){
        return (pool != null && !pool.isEmpty());
    }

    public String getPool(){
        return this.pool;
    }

    public List<String> getRequirementTarget() {
        return requirementTarget;
    }

    public boolean checkRequirement(RequirementContext context){
        if (requirement == null || requirement.isEmpty()) return true;

        for (String req : requirement){
            String[] reqList = req.split(":", 3);
            String location = reqList[0] + ":" + reqList[1];

            AbstractRequirement thisRequirement = RequirementRegistry.getRequirement(location);

            context.setValue(reqList[2]);

            if (!thisRequirement.check(context)){
                TheQuestForge.LOGGER.debug(QuestGenerator.QUEST_GENERATOR, "The condition is not met: {}", location);
                return false;
            }
        }

        return true;
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        nbt.putString("thisId", this.thisId != null ? this.thisId : "");
        nbt.putString("pool", this.pool != null ? this.pool : "");
        nbt.putInt("weight", this.weight);
        nbt.putInt("rewardCount", this.maxReward);
        nbt.putString("group", this.group != null ? this.group : "");
        nbt.putString("nextQuest", this.nextQuest != null ? this.nextQuest : "");

        if (this.type != null) {
            nbt.putString("QuestType", this.type.name());
        }

        if (this.timeLimit != null) nbt.put("timeLimit", this.timeLimit.serializeNBT());
        if (this.taskCount != null) nbt.put("taskCount", this.taskCount.serializeNBT());

        if (this.requirement != null && !this.requirement.isEmpty()) {
            ListTag reqTag = new ListTag();
            for (String req : this.requirement) {
                reqTag.add(StringTag.valueOf(req));
            }
            nbt.put("requirement", reqTag);
        }

        if (this.requirementTarget != null && !this.requirementTarget.isEmpty()) {
            ListTag reqItemsTag = new ListTag();
            for (String item : this.requirementTarget) {
                reqItemsTag.add(StringTag.valueOf(item));
            }
            nbt.put("requirementItems", reqItemsTag);
        }

        if (this.guaranteedReward != null && !this.guaranteedReward.isEmpty()) {
            ListTag guarItemsTag = new ListTag();
            for (String item : this.guaranteedReward) {
                guarItemsTag.add(StringTag.valueOf(item));
            }
            nbt.put("guaranteedReward", guarItemsTag);
        }

        if (this.name != null && !this.name.isEmpty()) {
            CompoundTag nameTag = new CompoundTag();
            for (Map.Entry<String, List<String>> entry : this.name.entrySet()) {
                ListTag listTag = new ListTag();
                for (String str : entry.getValue()) {
                    listTag.add(StringTag.valueOf(str));
                }
                nameTag.put(entry.getKey(), listTag);
            }
            nbt.put("name", nameTag);
        }

        if (this.description != null && !this.description.isEmpty()) {
            CompoundTag descTag = new CompoundTag();
            for (Map.Entry<String, List<String>> entry : this.description.entrySet()) {
                ListTag listTag = new ListTag();
                for (String str : entry.getValue()) {
                    listTag.add(StringTag.valueOf(str));
                }
                descTag.put(entry.getKey(), listTag);
            }
            nbt.put("description", descTag);
        }

        if (this.dialogs != null && !this.dialogs.isEmpty()) {
            CompoundTag dialogsTag = new CompoundTag();
            for (Map.Entry<String, TemplateDialog> entry : this.dialogs.entrySet()) {
                dialogsTag.put(entry.getKey(), entry.getValue().serializeNBT());
            }
            nbt.put("dialogs", dialogsTag);
        }

        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.thisId = nbt.getString("thisId");
        this.pool = nbt.getString("pool");
        this.weight = nbt.getInt("weight");
        this.group = nbt.getString("group");
        this.nextQuest = nbt.getString("nextQuest");
        this.maxReward = nbt.getInt("rewardCount");

        if (nbt.contains("QuestType")) {
            try {
                this.type = QuestType.valueOf(nbt.getString("QuestType"));
            } catch (IllegalArgumentException e) {
                this.type = QuestType.LOCAL;
            }
        } else {
            this.type = QuestType.LOCAL;
        }

        if (nbt.contains("timeLimit", Tag.TAG_COMPOUND)) {
            this.timeLimit = new Range();
            this.timeLimit.deserializeNBT(nbt.getCompound("timeLimit"));
        }

        if (nbt.contains("taskCount", Tag.TAG_COMPOUND)) {
            this.taskCount = new Range();
            this.taskCount.deserializeNBT(nbt.getCompound("taskCount"));
        }

        this.requirement = new ArrayList<>();
        if (nbt.contains("requirement", Tag.TAG_LIST)) {
            ListTag reqTag = nbt.getList("requirement", Tag.TAG_STRING);
            for (int i = 0; i < reqTag.size(); i++) {
                this.requirement.add(reqTag.getString(i));
            }
        }

        this.guaranteedReward = new ArrayList<>();
        if (nbt.contains("guaranteedReward", Tag.TAG_LIST)) {
            ListTag guarTag = nbt.getList("guaranteedReward", Tag.TAG_STRING);
            for (int i = 0; i < guarTag.size(); i++) {
                this.guaranteedReward.add(guarTag.getString(i));
            }
        }

        this.requirementTarget = new ArrayList<>();
        if (nbt.contains("requirementItems", Tag.TAG_LIST)) {
            ListTag reqItemsTag = nbt.getList("requirementItems", Tag.TAG_STRING);
            for (int i = 0; i < reqItemsTag.size(); i++) {
                this.requirementTarget.add(reqItemsTag.getString(i));
            }
        }

        this.name = new HashMap<>();
        if (nbt.contains("name", Tag.TAG_COMPOUND)) {
            CompoundTag nameTag = nbt.getCompound("name");
            for (String key : nameTag.getAllKeys()) {
                ListTag listTag = nameTag.getList(key, Tag.TAG_STRING);
                List<String> list = new ArrayList<>();
                for (int i = 0; i < listTag.size(); i++) {
                    list.add(listTag.getString(i));
                }
                this.name.put(key, list);
            }
        }

        this.description = new HashMap<>();
        if (nbt.contains("description", Tag.TAG_COMPOUND)) {
            CompoundTag descTag = nbt.getCompound("description");
            for (String key : descTag.getAllKeys()) {
                ListTag listTag = descTag.getList(key, Tag.TAG_STRING);
                List<String> list = new ArrayList<>();
                for (int i = 0; i < listTag.size(); i++) {
                    list.add(listTag.getString(i));
                }
                this.description.put(key, list);
            }
        }

        this.dialogs = new HashMap<>();
        if (nbt.contains("dialogs", Tag.TAG_COMPOUND)) {
            CompoundTag dialogsTag = nbt.getCompound("dialogs");
            for (String key : dialogsTag.getAllKeys()) {
                TemplateDialog dialog = new TemplateDialog();
                dialog.deserializeNBT(dialogsTag.getCompound(key));
                this.dialogs.put(key, dialog);
            }
        }
    }

    private int indexHelperMap(Map<String, List<String>> source, RandomSource randomSource){
        List<String> stringList = source.get("en_us");
        return randomSource.nextInt(stringList.size());
    }
}
