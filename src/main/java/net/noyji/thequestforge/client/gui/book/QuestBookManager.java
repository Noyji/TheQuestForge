package net.noyji.thequestforge.client.gui.book;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;
import net.noyji.thequestforge.data.template.QuestTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QuestBookManager {
    private int questIndex = 0;

    private final String languageKey = Minecraft.getInstance().options.languageCode;

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

}
