package net.noyji.thequestforge.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.network.ClientHandlerNetwork;

import java.util.UUID;
import java.util.function.Supplier;

public record AddDialogStageS2CPacket(UUID uuid, String stage) {

    public static void encode(AddDialogStageS2CPacket packet, FriendlyByteBuf buf){
        buf.writeUUID(packet.uuid);
        buf.writeUtf(packet.stage);
    }

    public static AddDialogStageS2CPacket decode(FriendlyByteBuf buf){
        return new AddDialogStageS2CPacket(buf.readUUID(), buf.readUtf());
    }

    public static void handle(AddDialogStageS2CPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()-> ClientHandlerNetwork.addDialogStage(packet.uuid, packet.stage));
        context.get().setPacketHandled(true);
    }
}
