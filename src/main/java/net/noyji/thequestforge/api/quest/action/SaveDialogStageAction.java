package net.noyji.thequestforge.api.quest.action;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.capability.player.PlayerQuestData;
import net.noyji.thequestforge.network.TheQuestForgeNetworking;
import net.noyji.thequestforge.network.s2c.AddDialogStageS2CPacket;

import java.util.UUID;

public class SaveDialogStageAction extends AbstractAction{
    @Override
    public boolean handler(ActionContext context) {
        UUID uuid = context.getEntity().getUUID();
        String key = context.getButton().getToGo();
        if (key == null || key.isEmpty()) return false;

        PlayerQuestData playerQuestData = CapabilityUtil.getPlayerQuestData(context.getPlayer());

        playerQuestData.putDialogProgress(uuid, key);
        playerQuestData.setSpareDialogStage(uuid, key);

        TheQuestForgeNetworking.sendToPlayer(new AddDialogStageS2CPacket(uuid, key), (ServerPlayer) context.getPlayer());
        return true;
    }

    @Override
    public ResourceLocation getLocation() {
        return TheQuestForge.id("save");
    }
}
