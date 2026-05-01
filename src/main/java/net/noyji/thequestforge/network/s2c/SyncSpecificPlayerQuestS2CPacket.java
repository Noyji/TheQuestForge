package net.noyji.thequestforge.network.s2c;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.network.ClientHandlerNetwork;

import java.util.UUID;
import java.util.function.Supplier;

public record SyncSpecificPlayerQuestS2CPacket(UUID questId, CompoundTag data) {
    public static void encode(SyncSpecificPlayerQuestS2CPacket packet, FriendlyByteBuf buf){
        buf.writeUUID(packet.questId);
        buf.writeNbt(packet.data);
    }

    public static SyncSpecificPlayerQuestS2CPacket decode (FriendlyByteBuf buf){
        return new SyncSpecificPlayerQuestS2CPacket(buf.readUUID(), buf.readNbt());
    }

    public static void handle(SyncSpecificPlayerQuestS2CPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> ClientHandlerNetwork.syncSpecificPlayerQuest(packet.questId, packet.data));
        context.get().setPacketHandled(true);
    }
}
