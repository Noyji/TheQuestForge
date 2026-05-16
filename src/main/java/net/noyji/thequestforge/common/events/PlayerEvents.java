package net.noyji.thequestforge.common.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.config.ServerConfig;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.capability.player.PlayerQuestData;
import net.noyji.thequestforge.data.managers.QuestGiversManager;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;
import net.noyji.thequestforge.network.ModNetworking;
import net.noyji.thequestforge.network.s2c.SyncEntityQuestDataS2CPacket;
import net.noyji.thequestforge.network.s2c.SyncPlayerAllQuestS2CPacket;
import net.noyji.thequestforge.network.s2c.SyncQuestTemplateS2CPacket;


@Mod.EventBusSubscriber
public class PlayerEvents {

    @SubscribeEvent
    public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        Entity target = event.getTarget();
        if (target == null || !QuestGiversManager.INSTANCE.thisQuestGiverOrVillager(target)) return;

        CompoundTag data = CapabilityUtil.getEntityQuestData(target).serializeNBT();
        ModNetworking.sendToPlayer(new SyncEntityQuestDataS2CPacket(target.getId(), data, false), serverPlayer);

        ModNetworking.debugInfo("Quest info in Start tracing event");
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ModNetworking.sendToPlayer(new SyncQuestTemplateS2CPacket(QuestTemplateManager.INSTANCE.serializeNBT()), serverPlayer);
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (!(entity instanceof ServerPlayer serverPlayer)) return;

        PlayerQuestData playerQuestData = CapabilityUtil.getPlayerQuestData(serverPlayer);
        ModNetworking.sendToPlayer(new SyncPlayerAllQuestS2CPacket(playerQuestData.serializeNBT()), serverPlayer);
        playerQuestData.debugInfoCatalog();
    }

    @SubscribeEvent
    public static void onTickPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() || event.phase == TickEvent.Phase.START) return;

        Player player = event.player;

        CapabilityUtil.getPlayerQuestData(player).updateQuestDays(player);
        checkResetCycle(event);
    }

    private static void checkResetCycle(TickEvent.PlayerTickEvent event){
        if (ServerConfig.TIME_TO_RESET_NPCS.get() == 501) return;

        Player player = event.player;

        if ((player.tickCount + player.getId()) % 20 == 0){
            PlayerQuestData playerQuestData = CapabilityUtil.getPlayerQuestData(player);

            long currentCycle = player.level().getGameTime() / (ServerConfig.TIME_TO_RESET_NPCS.get() * 24_000);

            if (playerQuestData.getLastResetCycle() < currentCycle){
                playerQuestData.clearChain();
                playerQuestData.unlockAllNpc();
                playerQuestData.reset(false);
                playerQuestData.setLastResetCycle(currentCycle);

                TheQuestForge.LOGGER.debug("NPC reset!");
            }
        }
    }
}
