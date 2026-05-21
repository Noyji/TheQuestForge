package net.noyji.thequestforge.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.common.items.TheQuestForgeItems;
import net.noyji.thequestforge.common.items.custom.QuestCompassItem;
import net.noyji.thequestforge.common.util.Util;

import java.util.UUID;
import java.util.function.Supplier;

public record SetTargetCompassC2SPacket(UUID questId) {

    public static void encode(SetTargetCompassC2SPacket packet, FriendlyByteBuf buf){
        buf.writeUUID(packet.questId);
    }

    public static SetTargetCompassC2SPacket decode(FriendlyByteBuf buf){
        return new SetTargetCompassC2SPacket(buf.readUUID());
    }

    public static void handle(SetTargetCompassC2SPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            Player player = context.get().getSender();
            if (player == null) return;

            ItemStack compass = Util.findItemInInventory(player, TheQuestForgeItems.QUEST_COMPASS.get());
            if (compass != null){
                QuestCompassItem.setTarget(compass, packet.questId);
            }
        });
        context.get().setPacketHandled(true);
    }
}
