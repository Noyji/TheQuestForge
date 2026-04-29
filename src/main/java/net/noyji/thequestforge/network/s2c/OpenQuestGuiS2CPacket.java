package net.noyji.thequestforge.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.network.ClientHandlerNetwork;

import java.util.function.Supplier;

public record OpenQuestGuiS2CPacket(int entityId) {

    public static void encode(OpenQuestGuiS2CPacket packet, FriendlyByteBuf buf){
        buf.writeInt(packet.entityId);
    }

    public static OpenQuestGuiS2CPacket decode(FriendlyByteBuf buf){
        return new OpenQuestGuiS2CPacket(buf.readInt());
    }

    public static void handle(OpenQuestGuiS2CPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> ClientHandlerNetwork.openQuestGiverGUI(packet.entityId));
        context.get().setPacketHandled(true);
    }
}
