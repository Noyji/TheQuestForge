package net.noyji.thequestforge.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.network.ClientHandlerNetwork;

import java.util.UUID;
import java.util.function.Supplier;

public record UnlockNpcS2CPacket(UUID entityId) {

    public static void encode(UnlockNpcS2CPacket packet, FriendlyByteBuf buf){
        buf.writeUUID(packet.entityId);
    }

    public static UnlockNpcS2CPacket decode(FriendlyByteBuf buf){
        return new UnlockNpcS2CPacket(buf.readUUID());
    }

    public static void handle(UnlockNpcS2CPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> ClientHandlerNetwork.unlockNpc(packet.entityId));
        context.get().setPacketHandled(true);
    }
}
