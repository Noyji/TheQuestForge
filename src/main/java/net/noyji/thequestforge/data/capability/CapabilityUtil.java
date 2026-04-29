package net.noyji.thequestforge.data.capability;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.noyji.thequestforge.data.capability.entity.EntityQuestData;
import net.noyji.thequestforge.data.capability.entity.EntityQuestProvider;
import net.noyji.thequestforge.data.capability.player.PlayerQuestData;
import net.noyji.thequestforge.data.capability.player.PlayerQuestProvider;

public class CapabilityUtil {
    public static PlayerQuestData getPlayerQuestData(Player player){
        return player.getCapability(PlayerQuestProvider.PLAYER_QUEST_DATA).orElse(new PlayerQuestData());
    }

    public static EntityQuestData getEntityQuestData(Entity entity){
        return entity.getCapability(EntityQuestProvider.ENTITY_QUEST_DATA).orElse(new EntityQuestData());
    }
}
