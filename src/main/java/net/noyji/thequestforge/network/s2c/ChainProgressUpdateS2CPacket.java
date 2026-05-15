package net.noyji.thequestforge.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.network.ClientHandlerNetwork;

import java.util.UUID;
import java.util.function.Supplier;

public record ChainProgressUpdateS2CPacket(UUID uuid) {

    public static void encode(ChainProgressUpdateS2CPacket packet, FriendlyByteBuf buf){
        buf.writeUUID(packet.uuid);
    }

    public static ChainProgressUpdateS2CPacket decode(FriendlyByteBuf buf){
        return new ChainProgressUpdateS2CPacket(buf.readUUID());
    }

    public static void handle(ChainProgressUpdateS2CPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> ClientHandlerNetwork.addChainProgress(packet.uuid));
        context.get().setPacketHandled(true);
    }
}
