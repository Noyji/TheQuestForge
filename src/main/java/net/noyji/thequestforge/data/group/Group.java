package net.noyji.thequestforge.data.group;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.task.AbstractTask;
import net.noyji.thequestforge.api.quest.task.TaskType;
import net.noyji.thequestforge.api.quest.registry.TaskHandlerRegistry;
import net.noyji.thequestforge.common.util.Util;
import net.noyji.thequestforge.data.group.components.JsonReward;
import net.noyji.thequestforge.data.group.components.JsonTask;
import net.noyji.thequestforge.data.quest.player.components.QuestRarity;
import net.noyji.thequestforge.data.template.components.Range;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private List<JsonTask> tasks;
    private List<JsonReward> rewards;
    @SerializedName("xp_reward")
    private Range xpReward;
    @SerializedName("currency_reward")
    private Range currencyReward;

    @Nullable
    public List<AbstractTask<?>> generateTasks(RandomSource randomSource, int taskCount, QuestRarity rarity, List<String> reqItem) {
        List<AbstractTask<?>> result = new ArrayList<>();
        List<JsonTask> source = new ArrayList<>(this.tasks);
        List<JsonTask> selectTask = new ArrayList<>();

        int remainingTasks = taskCount;

        if (reqItem != null && !reqItem.isEmpty()) {
            for (String reqItemId : reqItem) {
                if (remainingTasks <= 0) break;

                JsonTask taskToRemove = null;
                for (JsonTask jsonTask : source) {
                    if (jsonTask.targetIs(reqItemId)) {
                        selectTask.add(jsonTask);
                        taskToRemove = jsonTask;
                        remainingTasks--;
                        break;
                    }
                }

                if (taskToRemove != null) {
                    source.remove(taskToRemove);
                }
            }
        }

        if (remainingTasks > 0 && !source.isEmpty()) {
            List<JsonTask> randomTasks = Util.getWeightList(source, remainingTasks, randomSource);
            if (randomTasks != null) {
                selectTask.addAll(randomTasks);
            }
        }

        if (selectTask.isEmpty()) return null;

        for (JsonTask task : selectTask) {
            AbstractTask<?> abstractTask = buildTaskFromJson(task, randomSource, rarity);
            if (abstractTask != null) {
                result.add(abstractTask);
            }
        }

        return result;
    }
    @Nullable
    public List<ItemStack> generateReward(RandomSource randomSource, int rewardCount, QuestRarity rarity){
        List<ItemStack> result = new ArrayList<>();
        List<JsonReward> selectReward = Util.getWeightList(rewards, rewardCount, randomSource);

        if (selectReward == null || selectReward.isEmpty()) return null;

        for (JsonReward reward : selectReward){
            int goal = reward.getCount(randomSource, rarity);
            ItemStack itemStack = Util.parseItemStack(reward, goal, randomSource);

            if (itemStack != null){
                result.add(itemStack);
            }
        }

        return result;
    }

    public int getXp(RandomSource randomSource){
        return xpReward.getRandomInRange(randomSource);
    }

    public int getCurrencyReward(RandomSource randomSource){
        return currencyReward.getRandomInRange(randomSource);
    }
    @Nullable
    private AbstractTask<?> buildTaskFromJson(JsonTask jsonTask, RandomSource randomSource, QuestRarity rarity) {

        ResourceLocation taskTypeId = jsonTask.getTaskType();

        TaskType<?> taskType = TaskHandlerRegistry.REGISTRY.get().getValue(taskTypeId);

        if (taskType == null) {
            TheQuestForge.LOGGER.error("Error loading quest: Unknown task type {}", taskTypeId);
            return null;
        }

        AbstractTask<?> newTask = taskType.createInstance();

        newTask.parse(jsonTask, randomSource, rarity);

        return newTask;
    }
}
