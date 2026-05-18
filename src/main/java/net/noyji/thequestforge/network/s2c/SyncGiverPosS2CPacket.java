package net.noyji.thequestforge.network.s2c;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.network.ClientHandlerNetwork;

import java.util.UUID;
import java.util.function.Supplier;

public record SyncGiverPosS2CPacket(UUID questId, CompoundTag data) {

    public static void encode(SyncGiverPosS2CPacket packet, FriendlyByteBuf buf){
        buf.writeUUID(packet.questId);
        buf.writeNbt(packet.data);
    }

    public static SyncGiverPosS2CPacket decode(FriendlyByteBuf buf){
        return new SyncGiverPosS2CPacket(buf.readUUID() ,buf.readNbt());
    }

    public static void handle(SyncGiverPosS2CPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> ClientHandlerNetwork.syncGiverPos(packet.questId, packet.data));
        context.get().setPacketHandled(true);
    }
}
