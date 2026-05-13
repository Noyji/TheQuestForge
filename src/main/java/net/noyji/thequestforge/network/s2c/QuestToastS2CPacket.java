package net.noyji.thequestforge.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.network.ClientHandlerNetwork;

import java.util.UUID;
import java.util.function.Supplier;

public record QuestToastS2CPacket(UUID questid) {

    public static void encode(QuestToastS2CPacket packet, FriendlyByteBuf buf){
        buf.writeUUID(packet.questid);
    }

    public static QuestToastS2CPacket decode(FriendlyByteBuf buf){
        return new QuestToastS2CPacket(buf.readUUID());
    }

    public static void handle(QuestToastS2CPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> ClientHandlerNetwork.questToast(packet.questid));
        context.get().setPacketHandled(true);
    }
}
