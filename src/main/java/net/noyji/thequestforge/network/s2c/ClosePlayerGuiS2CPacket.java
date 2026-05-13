package net.noyji.thequestforge.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.network.ClientHandlerNetwork;

import java.util.function.Supplier;

public record ClosePlayerGuiS2CPacket() {

    public static void encode(ClosePlayerGuiS2CPacket packet, FriendlyByteBuf buf){}

    public static ClosePlayerGuiS2CPacket decode(FriendlyByteBuf buf){
        return new ClosePlayerGuiS2CPacket();
    }

    public static void handle(ClosePlayerGuiS2CPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(ClientHandlerNetwork::closeGui);
        context.get().setPacketHandled(true);
    }
}
