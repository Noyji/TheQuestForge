package net.noyji.thequestforge.common.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.registry.TaskHandlerRegistry;
import net.noyji.thequestforge.api.quest.requirements.RequirementContext;
import net.noyji.thequestforge.api.quest.task.AbstractTask;
import net.noyji.thequestforge.api.quest.task.TaskType;
import net.noyji.thequestforge.config.ServerConfig;
import net.noyji.thequestforge.data.group.Group;
import net.noyji.thequestforge.data.group.components.JsonReward;
import net.noyji.thequestforge.data.group.components.JsonTask;
import net.noyji.thequestforge.data.managers.QuestGroupManager;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;
import net.noyji.thequestforge.data.quest.entity.Quest;
import net.noyji.thequestforge.data.quest.entity.components.QuestDialog;
import net.noyji.thequestforge.data.quest.player.components.GiverData;
import net.noyji.thequestforge.data.quest.player.components.QuestRarity;
import net.noyji.thequestforge.data.quest.player.components.QuestType;
import net.noyji.thequestforge.data.template.QuestTemplate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.*;

public class QuestGenerator {
    private static final RandomSource RANDOM = RandomSource.create();
    public static final Marker QUEST_GENERATOR = MarkerFactory.getMarker("QUEST_GENERATOR");

    @Nullable
    public static Quest generateQuest(Player player, Entity entity, String pool) {
        QuestRarity rarity = getRarity();
        QuestTemplate template = QuestTemplateManager.EMPTY_TEMPLATE;
        int attempts = ServerConfig.ATTEMPTS_TO_CREATE_QUEST.get();
        Map<String, String> customData = new HashMap<>();

        for (int i = 0; i < attempts; i++) {
            RequirementContext context = new RequirementContext(player, entity, rarity, customData);
            QuestTemplate temp = QuestTemplateManager.INSTANCE.getRandomQuestTemplate(pool);

            if (temp == null) {
                TheQuestForge.LOGGER.warn(QUEST_GENERATOR, "Error template not found for quest. Pool: {}", pool);
                return null;
            }

            if (temp.checkRequirement(context)) {
                template = temp;
                break;
            }
        }

        if (template.isEmpty()) {
            TheQuestForge.LOGGER.warn(QUEST_GENERATOR, "Error, could not find a matching template. Pool: {}", pool);
            return null;
        }

        return buildQuestFromTemplate(template, entity, rarity, customData);
    }

    @Nullable
    public static Quest generateQuest(Entity entity, ResourceLocation templateId) {
        QuestRarity rarity = getRarity();
        QuestTemplate template = QuestTemplateManager.INSTANCE.getQuestTemplate(templateId);

        if (template == null || template.isEmpty()) {
            TheQuestForge.LOGGER.warn(QUEST_GENERATOR, "Error, template not found or empty! ID: {}", templateId);
            return null;
        }

        return buildQuestFromTemplate(template, entity, rarity, null);
    }

    @Nullable
    private static Quest buildQuestFromTemplate(QuestTemplate template, Entity entity, QuestRarity rarity, Map<String, String> customData) {
        Group group = QuestGroupManager.INSTANCE.getGroup(template.getGroupKey());
        if (group == null) {
            TheQuestForge.LOGGER.warn(QUEST_GENERATOR, "Error, group is null! Group key: {}", template.getGroupKey());
            return null;
        }

        List<JsonTask> allTasks = new ArrayList<>(group.getTasks());
        if (allTasks.isEmpty()) return null;

        int totalValue = 0;
        int taskCount = template.getTaskCount(RANDOM);
        List<String> requirementTargets = template.getRequirementTarget();
        List<AbstractTask<?>> tasks = new ArrayList<>();
        List<JsonTask> selectedTask = new ArrayList<>();

        if (requirementTargets != null && !requirementTargets.isEmpty()) {
            for (String reqTarget : requirementTargets) {
                if (taskCount <= 0) break;

                JsonTask taskToRemove = null;
                for (JsonTask jsonTask : allTasks) {
                    if (jsonTask.targetIs(reqTarget)) {
                        selectedTask.add(jsonTask);
                        taskToRemove = jsonTask;
                        taskCount--;
                        break;
                    }
                }

                if (taskToRemove != null) {
                    allTasks.remove(taskToRemove);
                }
            }
        }

        if (taskCount > 0 && !allTasks.isEmpty()) {
            List<JsonTask> randomTasks = Util.getWeightList(allTasks, taskCount, RANDOM);
            if (randomTasks != null) {
                selectedTask.addAll(randomTasks);
            }
        }

        if (selectedTask.isEmpty()) {
            TheQuestForge.LOGGER.debug("Error select task is empty!");
            return null;
        }

        for (JsonTask task : selectedTask) {
            AbstractTask<?> abstractTask = buildTaskFromJson(task, rarity);
            if (abstractTask != null) {
                totalValue += (task.getValue() * abstractTask.getGoal());
                tasks.add(abstractTask);
            }
        }

        List<JsonReward> allRewards = new ArrayList<>(group.getRewards());
        List<ItemStack> rewards = new ArrayList<>();

        while (totalValue > 0 && !allRewards.isEmpty()) {
            int finaleValue = totalValue;

            List<JsonReward> affordable = allRewards.stream()
                    .filter(item -> item.getPrice() <= finaleValue)
                    .toList();

            if (affordable.isEmpty()) break;

            JsonReward chosen = Util.getWeightItem(affordable, RANDOM);
            if (chosen == null) return null;

            int desiredCount = chosen.getCount(RANDOM, rarity);
            int maxAffordableCount = totalValue / chosen.getPrice();
            int finalCount = Math.min(desiredCount, maxAffordableCount);

            if (finalCount <= 0) {
                allRewards.remove(chosen);
                continue;
            }

            totalValue -= (chosen.getPrice() * finalCount);
            allRewards.remove(chosen);

            ItemStack itemReward = Util.parseItemStack(chosen, finalCount, RANDOM);
            rewards.add(itemReward);

            if (rewards.size() >= template.getRewardCount()) break;
        }

        Map<String, QuestDialog> dialogMap = template.getQuestDialog(RANDOM);
        ResourceLocation sourceTemplate = template.getThisId();
        UUID questId = getQuestId(entity);
        int timeLimit = template.hasTimeLimit() ? template.getTimeLimit(RANDOM) : -2;
        QuestType type = template.getType();
        int nameIndex = template.getNameIndex(RANDOM);
        int descriptionIndex = template.getDescription(RANDOM);
        int xp = group.getXp(RANDOM);
        int currency = group.getCurrencyReward(RANDOM);

        GiverData giverData = new GiverData();
        giverData.setGiverPos(entity.getOnPos());
        giverData.setDimension(entity.level().dimension());
        giverData.setEntityId(entity.getId());

        TheQuestForge.LOGGER.debug(QUEST_GENERATOR, "Quest created: {}, source template: {}", questId, sourceTemplate);

        Quest quest = new Quest(sourceTemplate, questId, timeLimit, type, rarity, nameIndex,
                descriptionIndex, xp, currency, false, tasks, rewards, dialogMap, giverData);

        if (customData != null && !customData.isEmpty()){
            quest.setCustomData(customData);
        }

        quest.questInfo();

        return quest;
    }

    private static UUID getQuestId(Entity entity) {
        return entity == null ? UUID.randomUUID() : entity.getUUID();
    }

    private static QuestRarity getRarity() {
        int roll = RANDOM.nextInt(100);
        System.out.println(roll);

        int currentThreshold = ServerConfig.legendaryQuestChance;
        if (roll < currentThreshold) return QuestRarity.LEGENDARY;

        currentThreshold += ServerConfig.epicQuestChance;
        if (roll < currentThreshold) return QuestRarity.EPIC;

        currentThreshold += ServerConfig.rareQuestChance;
        if (roll < currentThreshold) return QuestRarity.RARE;

        currentThreshold += ServerConfig.uncommonQuestChance;
        if (roll < currentThreshold) return QuestRarity.UNCOMMON;

        return QuestRarity.COMMON;
    }

    @Nullable
    private static AbstractTask<?> buildTaskFromJson(JsonTask jsonTask, QuestRarity rarity) {
        ResourceLocation taskTypeId = jsonTask.getTaskType();
        TaskType<?> taskType = TaskHandlerRegistry.REGISTRY.get().getValue(taskTypeId);

        if (taskType == null) {
            TheQuestForge.LOGGER.error("Error loading quest: Unknown task type {}", taskTypeId);
            return null;
        }

        AbstractTask<?> newTask = taskType.createInstance();
        newTask.parse(jsonTask, RANDOM, rarity);

        return newTask;
    }
}