package net.noyji.thequestforge.network.s2c;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.network.ClientHandlerNetwork;

import java.util.function.Supplier;

public record AddPlayerQuestS2CPacket(CompoundTag data) {

    public static void encode(AddPlayerQuestS2CPacket packet, FriendlyByteBuf buf){
        buf.writeNbt(packet.data);
    }

    public static AddPlayerQuestS2CPacket decode(FriendlyByteBuf buf){
        return new AddPlayerQuestS2CPacket(buf.readNbt());
    }

    public static void handle(AddPlayerQuestS2CPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> ClientHandlerNetwork.addPlayerQuest(packet.data));
        context.get().setPacketHandled(true);
    }
}
