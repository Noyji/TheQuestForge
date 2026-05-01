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
import net.noyji.thequestforge.network.ModNetworking;
import net.noyji.thequestforge.network.s2c.AddPlayerQuestS2CPacket;

public class AcceptQuestAction extends AbstractAction{
    @Override
    public boolean handler(Player player, Entity entity) {
        PlayerQuestData playerQuestData = CapabilityUtil.getPlayerQuestData(player);
        EntityQuestData entityQuestData = CapabilityUtil.getEntityQuestData(entity);

        Quest quest = entityQuestData.getQuest();
        if (quest == null) return false;

        playerQuestData.addQuest(quest.copyToPlayerQuest());
        ModNetworking.sendToPlayer(new AddPlayerQuestS2CPacket(quest.serializeNBT()), (ServerPlayer) player);
        return true;
    }

    @Override
    public ResourceLocation getLocation() {
        return TheQuestForge.id("accept");
    }
}
