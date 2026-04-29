package net.noyji.thequestforge.network.s2c;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.network.ClientHandlerNetwork;

import java.util.function.Supplier;

public record SyncEntityQuestDataS2CPacket(int entityId, CompoundTag data, boolean openGUI) {

    public static void encode(SyncEntityQuestDataS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.entityId);
        buf.writeNbt(packet.data);
        buf.writeBoolean(packet.openGUI);
    }

    public static SyncEntityQuestDataS2CPacket decode(FriendlyByteBuf buf) {
        return new SyncEntityQuestDataS2CPacket(buf.readInt(), buf.readNbt(), buf.readBoolean());
    }

    public static void handle(SyncEntityQuestDataS2CPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientHandlerNetwork.handleSyncFullQuest(packet.entityId, packet.data, packet.openGUI));
        context.get().setPacketHandled(true);
    }
}
