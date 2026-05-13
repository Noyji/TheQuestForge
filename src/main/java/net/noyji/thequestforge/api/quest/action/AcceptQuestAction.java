package net.noyji.thequestforge.api.quest.action;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.capability.entity.EntityQuestData;
import net.noyji.thequestforge.data.capability.player.PlayerQuestData;
import net.noyji.thequestforge.data.quest.entity.Quest;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;
import net.noyji.thequestforge.network.ModNetworking;
import net.noyji.thequestforge.network.s2c.AddPlayerQuestS2CPacket;

public class AcceptQuestAction extends AbstractAction{
    @Override
    public boolean handler(ActionContext context) {
        PlayerQuestData playerQuestData = CapabilityUtil.getPlayerQuestData(context.getPlayer());
        EntityQuestData entityQuestData = CapabilityUtil.getEntityQuestData(context.getEntity());

        Quest quest = entityQuestData.getQuest();
        if (quest == null) return false;

        PlayerQuest playerQuest = quest.copyToPlayerQuest();
        playerQuestData.addQuest(playerQuest);
        ModNetworking.sendToPlayer(new AddPlayerQuestS2CPacket(playerQuest.serializeNBT()), (ServerPlayer) context.getPlayer());
        return true;
    }

    @Override
    public ResourceLocation getLocation() {
        return TheQuestForge.id("accept");
    }
}
