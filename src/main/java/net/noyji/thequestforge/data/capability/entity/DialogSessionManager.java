package net.noyji.thequestforge.data.capability.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DialogSessionManager {
    private static final Map<UUID, UUID> activeDialog = new HashMap<>();

    public static void startDialog(Mob npc, Player player){
        if (!activeDialog.containsKey(npc.getUUID())) {
            activeDialog.put(npc.getUUID(), player.getUUID());
        }
    }

    public static void stopDialog(Mob npc){
        activeDialog.remove(npc.getUUID());
    }

    @Nullable
    public static Player getTalkingPlayer(Mob npc){
        UUID playerID = activeDialog.get(npc.getUUID());
        if (npc.level() instanceof ServerLevel serverLevel){
            return (Player) serverLevel.getEntity(playerID);
        }
        return null;
    }
}
