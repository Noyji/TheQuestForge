package net.noyji.thequestforge.api.quest.action;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.common.util.QuestGenerator;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.capability.entity.EntityQuestData;
import net.noyji.thequestforge.data.capability.player.PlayerQuestData;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;
import net.noyji.thequestforge.data.quest.entity.Quest;
import net.noyji.thequestforge.data.template.QuestTemplate;
import net.noyji.thequestforge.network.TheQuestForgeNetworking;
import net.noyji.thequestforge.network.s2c.ChainProgressUpdateS2CPacket;
import net.noyji.thequestforge.network.s2c.ClosePlayerGuiS2CPacket;
import net.noyji.thequestforge.network.s2c.SyncEntityQuestDataS2CPacket;

import java.util.UUID;

public class NextQuestAction extends AbstractAction{
    @Override
    public boolean handler(ActionContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) return false;
        Entity npc = context.getEntity();

        if (npc == null) return false;

        PlayerQuestData playerData = CapabilityUtil.getPlayerQuestData(player);
        EntityQuestData npcData = CapabilityUtil.getEntityQuestData(npc);
        UUID npcId = npc.getUUID();

        Quest npcQuest = npcData.getQuest(playerData.getChainProgress(npcId));

        playerData.advanceChainProgress(npcId);
        TheQuestForgeNetworking.sendToPlayer(new ChainProgressUpdateS2CPacket(npcQuest.getId()), player);

        QuestTemplate template = QuestTemplateManager.INSTANCE.getQuestTemplate(npcQuest.getSourceTemplate());
        if (template == null) {
            TheQuestForge.LOGGER.debug("Quest template is null!");
            return false;
        }

        ResourceLocation nextQuest = template.getNextQuest();

        if (nextQuest != null) {
            Quest newQuest = QuestGenerator.generateQuest(npc, nextQuest);

            if (newQuest != null) {
                npcData.addQuest(newQuest);
                TheQuestForgeNetworking.sendToPlayer(new SyncEntityQuestDataS2CPacket(context.getEntity().getId(), npcData.serializeNBT(), true), player);
            }
        } else {
            playerData.lockNpc(npcId);

            player.sendSystemMessage(Component.literal("Цепочка заданий завершена!").withStyle(style -> style.withColor(0x00FFCC)));

            TheQuestForgeNetworking.sendToPlayer(new ClosePlayerGuiS2CPacket(), player);
        }
        return true;
    }

    @Override
    public ResourceLocation getLocation() {
        return TheQuestForge.id("next_quest");
    }
}
