package net.noyji.thequestforge.network.s2c;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.network.ClientHandlerNetwork;

import java.util.function.Supplier;

public record SyncQuestTemplateS2CPacket(CompoundTag templates) {

    public static void encode(SyncQuestTemplateS2CPacket packet, FriendlyByteBuf buf){
        buf.writeNbt(packet.templates);
    }

    public static SyncQuestTemplateS2CPacket decode(FriendlyByteBuf buf){
        return new SyncQuestTemplateS2CPacket(buf.readNbt());
    }

    public static void handle(SyncQuestTemplateS2CPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> ClientHandlerNetwork.handleSyncQuestTemplate(packet.templates));
        context.get().setPacketHandled(true);
    }
}
