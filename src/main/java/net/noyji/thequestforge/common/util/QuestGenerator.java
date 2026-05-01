package net.noyji.thequestforge.common.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.requirements.RequirementContext;
import net.noyji.thequestforge.api.quest.task.AbstractTask;
import net.noyji.thequestforge.config.ServerConfig;
import net.noyji.thequestforge.data.group.Group;
import net.noyji.thequestforge.data.managers.QuestGroupManager;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;
import net.noyji.thequestforge.data.quest.entity.Quest;
import net.noyji.thequestforge.data.quest.entity.components.QuestDialog;
import net.noyji.thequestforge.data.quest.player.components.QuestRarity;
import net.noyji.thequestforge.data.quest.player.components.QuestType;
import net.noyji.thequestforge.data.template.QuestTemplate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QuestGenerator {
    private static final RandomSource RANDOM = RandomSource.create();

    public static final Marker QUEST_GENERATOR = MarkerFactory.getMarker("QUEST_GENERATOR");
    @Nullable
    public static Quest generateQuest (Player player, Entity entity, String pool){

        QuestRarity rarity = getRarity();
        QuestTemplate template = new QuestTemplate();
        int attempts = ServerConfig.ATTEMPTS_TO_CREATE_QUEST.get();

        for (int i = 0; i < attempts; i++){
            RequirementContext context = new RequirementContext(player, entity, rarity);

            template = QuestTemplateManager.INSTANCE.getRandomQuestTemplate(pool);

            if (template == null) {
                TheQuestForge.LOGGER.warn(QUEST_GENERATOR, "Error template not found for quest. Pool: {}", pool);
                return null;
            }

            if (template.checkRequirement(context)) break;
            template = QuestTemplateManager.EMPTY_TEMPLATE;
        }
        if (template.isEmpty()){
            TheQuestForge.LOGGER.warn(QUEST_GENERATOR, "Error, could not find a matching template. Pool{}", pool);
            return null;
        }
        Group group = QuestGroupManager.INSTANCE.getGroup(template.getGroupKey());
        if (group == null) {
            TheQuestForge.LOGGER.warn(QUEST_GENERATOR, "Error, group is null! Group key: {}", template.getGroupKey());
            return null;
        }

        List<AbstractTask<?>> tasks = group.generateTasks(RANDOM, template.getTaskCount(RANDOM), rarity, template.getRequirementItems());
        if (tasks == null) {
            TheQuestForge.LOGGER.warn(QUEST_GENERATOR, "Error, tasks for quest is null!");
            return  null;
        }

        List<ItemStack> rewards = group.generateReward(RANDOM, template.getRewardCount(RANDOM), rarity);
        if (rewards == null) {
            TheQuestForge.LOGGER.warn(QUEST_GENERATOR, "Error, rewards is null!");
            return null;
        }

        Map<String, QuestDialog> dialogMap = template.getQuestDialog(RANDOM);
        ResourceLocation sourceTemplate = template.getThisId();
        UUID questId = getQuestId(entity);
        int timeLimit = -1;
        if (template.hasTimeLimit()) {
            timeLimit = template.getTimeLimit(RANDOM);
        }
        QuestType type = QuestType.LOCAL;
        int nameIndex = template.getNameIndex(RANDOM);
        int descriptionIndex = template.getDescription(RANDOM);
        int xp = group.getXp(RANDOM);
        int currency = group.getCurrencyReward(RANDOM);

        TheQuestForge.LOGGER.debug(QUEST_GENERATOR, "Quest created: {}, source template: {}", questId, sourceTemplate);

        Quest quest = new Quest(sourceTemplate ,questId, timeLimit, type, rarity, nameIndex,
                descriptionIndex, xp, currency, false, tasks, rewards, dialogMap);

        quest.questInfo();

        return quest;
    }

    private static UUID getQuestId(Entity entity){
        return entity == null ? UUID.randomUUID() : entity.getUUID();
    }

    private static QuestRarity getRarity(){
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
}
