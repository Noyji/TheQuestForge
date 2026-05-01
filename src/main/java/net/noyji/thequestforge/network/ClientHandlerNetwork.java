package net.noyji.thequestforge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.client.gui.entity.QuestGiverGUI;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.capability.entity.EntityQuestData;
import net.noyji.thequestforge.data.capability.player.PlayerQuestData;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;
import net.noyji.thequestforge.data.quest.entity.Quest;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientHandlerNetwork {
    public static void handleSyncFullQuest(int entityId, CompoundTag data, boolean openGUI){
        if (Minecraft.getInstance().level == null) return;
        Level level = Minecraft.getInstance().level;

        Entity entity = level.getEntity(entityId);
        if (entity == null) return;

        TheQuestForge.LOGGER.debug("Sync entity cap!");

        EntityQuestData entityData = CapabilityUtil.getEntityQuestData(entity);
        entityData.deserializeNBT(data);

        if (openGUI){
            openQuestGiverGUI(entityId);
        }
    }

    public static void openQuestGiverGUI(int entityId){
        if (Minecraft.getInstance().level == null) return;
        Level level = Minecraft.getInstance().level;

        Entity entity = level.getEntity(entityId);
        if (entity == null) return;

        Minecraft.getInstance().setScreen(new QuestGiverGUI(entity));
    }

    public static void handleSyncQuestTemplate(CompoundTag data){
        QuestTemplateManager.INSTANCE.deserializeNBT(data);
    }

    public static void syncAllPlayerQuest(CompoundTag data){
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        PlayerQuestData playerQuestData = CapabilityUtil.getPlayerQuestData(player);
        playerQuestData.deserializeNBT(data);
        TheQuestForge.LOGGER.debug("Sync player quest: {}", playerQuestData.sizeQuest());
        playerQuestData.debugInfo();
    }

    public static void syncSpecificPlayerQuest(UUID questId, CompoundTag data){
        TheQuestForge.LOGGER.debug("Sync specific player quest!");
        Minecraft minecraft = Minecraft.getInstance();

        Player player = minecraft.player;
        if (player == null) return;

        PlayerQuestData playerQuestData = CapabilityUtil.getPlayerQuestData(player);
        playerQuestData.updateQuest(questId, data);
    }

    public static void addPlayerQuest(CompoundTag data){
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        PlayerQuestData playerQuestData = CapabilityUtil.getPlayerQuestData(player);
        Quest quest = new Quest();
        quest.deserializeNBT(data);
        playerQuestData.addQuest(quest);
    }

}
