package net.noyji.thequestforge.common.events;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.noyji.thequestforge.common.util.EntityQuestHandler;

@Mod.EventBusSubscriber
public class EntityEvents {
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        Entity entity = event.getEntity();

        EntityQuestHandler.tryCreateEmptyQuest(entity, true);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        EntityQuestHandler.immobilize(event);
        EntityQuestHandler.resetCycle(event);
    }


}
