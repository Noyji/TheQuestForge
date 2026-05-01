package net.noyji.thequestforge.network.s2c;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.network.ClientHandlerNetwork;

import java.util.function.Supplier;

public record SyncPlayerAllQuestS2CPacket(CompoundTag data) {

    public static void encode(SyncPlayerAllQuestS2CPacket packet, FriendlyByteBuf buf){
        buf.writeNbt(packet.data);
    }

    public static SyncPlayerAllQuestS2CPacket decode(FriendlyByteBuf buf){
        return new SyncPlayerAllQuestS2CPacket(buf.readNbt());
    }

    public static void handle(SyncPlayerAllQuestS2CPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> ClientHandlerNetwork.syncAllPlayerQuest(packet.data));
        context.get().setPacketHandled(true);
    }
}
