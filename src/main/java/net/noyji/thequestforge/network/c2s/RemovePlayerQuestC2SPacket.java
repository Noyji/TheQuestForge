package net.noyji.thequestforge.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.data.capability.CapabilityUtil;

import java.util.UUID;
import java.util.function.Supplier;

public record RemovePlayerQuestC2SPacket(UUID questId) {

    public static void encode(RemovePlayerQuestC2SPacket packet, FriendlyByteBuf buf){
        buf.writeUUID(packet.questId);
    }

    public static RemovePlayerQuestC2SPacket decode(FriendlyByteBuf buf){
        return new RemovePlayerQuestC2SPacket(buf.readUUID());
    }

    public static void handle(RemovePlayerQuestC2SPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            Player player = context.get().getSender();
            if (player == null) return;

            CapabilityUtil.getPlayerQuestData(player).removeQuest(packet.questId);
        });
        context.get().setPacketHandled(true);
    }
}
