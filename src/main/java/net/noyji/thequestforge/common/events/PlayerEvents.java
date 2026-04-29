package net.noyji.thequestforge.common.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.noyji.thequestforge.common.util.EntityQuestHandler;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.managers.QuestGiversManager;
import net.noyji.thequestforge.network.ModNetworking;
import net.noyji.thequestforge.network.s2c.SyncEntityQuestDataS2CPacket;


@Mod.EventBusSubscriber
public class PlayerEvents {
    @SubscribeEvent
    public static void onPlayerInteractEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getEntity();
        Entity target = event.getTarget();

        EntityQuestHandler.onEntityInteract(player, target);
    }

    @SubscribeEvent
    public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        Entity target = event.getTarget();
        if (target == null || !QuestGiversManager.INSTANCE.thisQuestGiverOrVillager(target)) return;

        CompoundTag data = CapabilityUtil.getEntityQuestData(target).serializeNBT();
        ModNetworking.sendToPlayer(new SyncEntityQuestDataS2CPacket(target.getId(), data, false), serverPlayer);

        ModNetworking.debugInfo("Quest info in Start tracing event");
    }
}
