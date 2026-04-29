package net.noyji.thequestforge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.client.gui.entity.QuestGiverGUI;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.capability.entity.EntityQuestData;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;

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
}
