package net.noyji.thequestforge.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.common.util.EntityQuestHandler;

import java.util.function.Supplier;

public record InteractNpcC2SPacket(int entityId) {
    public static void encode(InteractNpcC2SPacket packet, FriendlyByteBuf buf){
        buf.writeInt(packet.entityId);
    }

    public static InteractNpcC2SPacket decode(FriendlyByteBuf buf){
        return new InteractNpcC2SPacket(buf.readInt());
    }

    public static void handle(InteractNpcC2SPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            Player player = context.get().getSender();
            Entity entity = player.level().getEntity(packet.entityId);
            if (entity == null) return;

            EntityQuestHandler.onEntityInteract(player, entity);
        });
        context.get().setPacketHandled(true);
    }
}
