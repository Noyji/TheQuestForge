package net.noyji.thequestforge.common.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.common.util.Util;
import net.noyji.thequestforge.data.capability.CapabilityUtil;

@Mod.EventBusSubscriber
public class QuestEvents {
    private static final ResourceLocation KILL_KEY = TheQuestForge.id("kill");
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity deathMob = event.getEntity();
        if (deathMob.level().isClientSide()) return;

        Entity killer = event.getSource().getEntity();

        if (killer == null){
            killer = deathMob.getLastAttacker();
        }

        if (killer instanceof Player player){
            ResourceLocation mob = Util.getEntityResourceLocation(deathMob);
            CapabilityUtil.getPlayerQuestData(player).progressUpdate(KILL_KEY, mob, event);
        }
    }

}
