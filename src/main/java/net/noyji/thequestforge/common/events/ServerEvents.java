package net.noyji.thequestforge.common.events;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.noyji.thequestforge.data.managers.QuestGiversManager;
import net.noyji.thequestforge.data.managers.QuestGroupManager;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;

@Mod.EventBusSubscriber
public class ServerEvents {

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(QuestGiversManager.INSTANCE);
        event.addListener(QuestTemplateManager.INSTANCE);
        event.addListener(QuestGroupManager.INSTANCE);
    }
}
