package net.noyji.thequestforge.client.gui.book;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.noyji.thequestforge.api.quest.task.AbstractTask;
import net.noyji.thequestforge.common.items.TheQuestForgeItems;
import net.noyji.thequestforge.common.items.custom.QuestCompassItem;
import net.noyji.thequestforge.common.util.Util;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;
import net.noyji.thequestforge.data.template.QuestTemplate;
import net.noyji.thequestforge.network.TheQuestForgeNetworking;
import net.noyji.thequestforge.network.c2s.RemovePlayerQuestC2SPacket;
import net.noyji.thequestforge.network.c2s.SetTargetCompassC2SPacket;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QuestBookManager {
    private int questIndex = 0;

    private final String languageKey = Minecraft.getInstance().options.languageCode;

    private final Player player = Minecraft.getInstance().player;

    private List<PlayerQuest> playerQuests;
    private PlayerQuest selectQuest;

    private QuestTemplate template;

    public QuestBookManager(){
    }
    public void init(){
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        playerQuests = CapabilityUtil.getPlayerQuestData(player).getQuests();
        updateQuest();
    }

    public boolean setQuestIndex(int index){
        if (index < 0 || index > playerQuests.size()) index = 0;
        questIndex = index;
        updateQuest();
        return true;
    }

    public boolean questIndexUp(){
        if (questIndex >= playerQuests.size() - 1) return false;
        questIndex++;
        updateQuest();
        return true;
    }

    public boolean questIndexDown(){
        if (questIndex <= 0) return false;
        questIndex--;
        updateQuest();
        return true;
    }

    private void updateQuest(){
        if (playerQuests.isEmpty()) return;
        selectQuest = playerQuests.get(questIndex);
        ResourceLocation source = selectQuest.getSourceTemplate();
        template = QuestTemplateManager.INSTANCE.getQuestTemplate(source);
    }

    public Component getQuestName(){
        if (selectQuest == null) return Component.literal("Empty");
        return template.getQuestName(languageKey, selectQuest.getNameIndex());
    }

    public Component getQuestRarity(){
        if (selectQuest == null) return Component.literal("Empty");
        return Component.translatable("gui.thequestforge.rarity.info").append(selectQuest.getRarity().getTranslateName());
    }
    @Nullable
    public Component getTimeLimit(){
        if (selectQuest == null) return null;
        int time = selectQuest.getTimeLimit();
        if (time == 0) return Component.translatable("gui.thequestforge.time_limit.info")
                .append(Component.translatable("gui.thequestforge.quest.time_limit"));
        if (time > 0) return Component.translatable("gui.thequestforge.time_limit.info")
                .append(Component.literal(selectQuest.getTimeLimit() + ""));
        return null;
    }

    public Component getQuestDescription(){
        if (selectQuest == null) return Component.literal("Empty");
        return template.getQuestDescription(languageKey, selectQuest.getDescriptionIndex());
    }

    public List<AbstractTask<?>> getCurrentQuestTasks(){
        if (selectQuest == null) return null;
        return selectQuest.getTasks();
    }

    public List<ItemStack> getCurrentQuestRewards(){
        if (selectQuest == null) return null;
        return selectQuest.getRewards();
    }

    public int getQuestXp(){
        if (selectQuest == null) return -1;
        return selectQuest.getXp();
    }

    public int getQuestCurrency(){
        if (selectQuest == null) return -1;
        return selectQuest.getCurrency();
    }

    public boolean isComplete(){
        if (selectQuest == null) return  false;
        return selectQuest.isComplete();
    }

    public boolean removeQuest() {
        if (selectQuest == null) return false;

        Player player = Minecraft.getInstance().player;
        if (player == null) return false;

        TheQuestForgeNetworking.sendToServer(new RemovePlayerQuestC2SPacket(selectQuest.getId()));
        CapabilityUtil.getPlayerQuestData(player).removeQuest(selectQuest.getId());

        playerQuests = CapabilityUtil.getPlayerQuestData(player).getQuests();

        if (playerQuests.isEmpty()) {
            selectQuest = null;
            questIndex = 0;
            updateQuest();
            return true;
        }

        if (questIndex >= playerQuests.size()) {
            questIndex = playerQuests.size() - 1;
        }

        updateQuest();
        return true;
    }

    public boolean targetQuestForCompass(){
        if (player == null || selectQuest == null) return false;

        ItemStack compass = Util.findItemInInventory(player, TheQuestForgeItems.QUEST_COMPASS.get());
        if (compass == null) return false;

        TheQuestForgeNetworking.sendToServer(new SetTargetCompassC2SPacket(selectQuest.getId()));
        return true;

    }

    public int getQuestIndex() {
        return questIndex;
    }

    public int getMaxQuestsCount() {
        return  playerQuests.size();
    }
}
