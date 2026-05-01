package net.noyji.thequestforge.common.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.noyji.thequestforge.common.util.EntityQuestHandler;
import net.noyji.thequestforge.data.capability.entity.DialogSessionManager;
import net.noyji.thequestforge.data.managers.QuestGiversManager;

@Mod.EventBusSubscriber
public class EntityEvents {
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        Entity entity = event.getEntity();

        EntityQuestHandler.tryCreateEmptyQuest(entity);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        EntityQuestHandler.immobilize(event);
    }


}
