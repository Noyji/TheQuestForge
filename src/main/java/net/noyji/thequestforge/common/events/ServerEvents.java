package net.noyji.thequestforge.common.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.noyji.thequestforge.data.managers.QuestGiversManager;
import net.noyji.thequestforge.data.managers.QuestGroupManager;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;
import net.noyji.thequestforge.network.ModNetworking;
import net.noyji.thequestforge.network.s2c.SyncQuestTemplateS2CPacket;

@Mod.EventBusSubscriber
public class ServerEvents {

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(QuestGiversManager.INSTANCE);
        event.addListener(QuestTemplateManager.INSTANCE);
        event.addListener(QuestGroupManager.INSTANCE);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ModNetworking.sendToPlayer(new SyncQuestTemplateS2CPacket(QuestTemplateManager.INSTANCE.serializeNBT()), serverPlayer);
    }


}
