package net.noyji.thequestforge.data.capability;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.data.capability.entity.EntityQuestData;
import net.noyji.thequestforge.data.capability.entity.EntityQuestProvider;
import net.noyji.thequestforge.data.capability.player.PlayerQuestData;
import net.noyji.thequestforge.data.capability.player.PlayerQuestProvider;
import net.noyji.thequestforge.data.managers.QuestGiversManager;

@Mod.EventBusSubscriber
public class AttachCapabilities {

    public static void registerCaps(RegisterCapabilitiesEvent event){
        event.register(PlayerQuestData.class);
        event.register(EntityQuestData.class);
    }

    public static void attachCaps(AttachCapabilitiesEvent<Entity> event){
        onAttachPlayerCapability(event);
        onAttachEntityCapability(event);
    }

    public static void onAttachPlayerCapability(AttachCapabilitiesEvent<Entity> event){
        if (!(event.getObject() instanceof Player)) return;
        if (event.getObject().getCapability(PlayerQuestProvider.PLAYER_QUEST_DATA).isPresent()) return;

        event.addCapability(TheQuestForge.id("player_quest_data"), new PlayerQuestProvider());
    }

    public static void onAttachEntityCapability(AttachCapabilitiesEvent<Entity> event){
        if (!(event.getObject() instanceof LivingEntity)) return;
        if (!(QuestGiversManager.INSTANCE.thisQuestGiverOrVillager(event.getObject()))) return;
        if (event.getObject().getCapability(EntityQuestProvider.ENTITY_QUEST_DATA).isPresent()) return;

        event.addCapability(TheQuestForge.id("entity_quest_data"), new EntityQuestProvider());
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(PlayerQuestProvider.PLAYER_QUEST_DATA).ifPresent(oldCap -> {
            event.getEntity().getCapability(PlayerQuestProvider.PLAYER_QUEST_DATA).ifPresent(newCap -> {
                newCap.deserializeNBT(oldCap.serializeNBT());
            });
        });
        event.getOriginal().invalidateCaps();
    }

}
