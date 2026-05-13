package net.noyji.thequestforge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.client.gui.entity.QuestGiverGUI;
import net.noyji.thequestforge.client.gui.toast.QuestToast;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.capability.entity.EntityQuestData;
import net.noyji.thequestforge.data.capability.player.PlayerQuestData;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;
import net.noyji.thequestforge.data.quest.entity.Quest;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;
import net.noyji.thequestforge.data.template.QuestTemplate;

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

    public static void removePlayerQuest(UUID questId){
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        CapabilityUtil.getPlayerQuestData(player).removeQuest(questId);
    }

    public static void addDialogStage(UUID uuid, String stage){
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        PlayerQuestData playerQuestData = CapabilityUtil.getPlayerQuestData(player);

        playerQuestData.putDialogProgress(uuid, stage);
        playerQuestData.setSpareDialogStage(uuid, stage);
    }

    public static void questToast(UUID questid){
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        PlayerQuestData playerQuestData = CapabilityUtil.getPlayerQuestData(player);
        PlayerQuest playerQuest = playerQuestData.getQuest(questid);
        if (playerQuest == null) return;

        QuestTemplate template = QuestTemplateManager.INSTANCE.getQuestTemplate(playerQuest.getSourceTemplate());
        if (template == null) return;

        String language = Minecraft.getInstance().options.languageCode;
        Component questName = template.getQuestName(language, playerQuest.getNameIndex());

        Minecraft.getInstance().getToasts().addToast(new QuestToast(questName));
    }

    public static void closeGui(){
        Minecraft.getInstance().setScreen(null);
    }
}
