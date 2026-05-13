package net.noyji.thequestforge.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.network.ClientHandlerNetwork;

import java.util.UUID;
import java.util.function.Supplier;

public record RemovePlayerQuestS2CPacket(UUID uuid){
    public static void encode(RemovePlayerQuestS2CPacket packet, FriendlyByteBuf buf){
        buf.writeUUID(packet.uuid);
    }

    public static RemovePlayerQuestS2CPacket decode(FriendlyByteBuf buf){
        return new RemovePlayerQuestS2CPacket(buf.readUUID());
    }

    public static void handle(RemovePlayerQuestS2CPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> ClientHandlerNetwork.removePlayerQuest(packet.uuid));
        context.get().setPacketHandled(true);
    }
}
