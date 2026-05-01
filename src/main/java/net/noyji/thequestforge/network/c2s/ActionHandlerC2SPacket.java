package net.noyji.thequestforge.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;
import net.noyji.thequestforge.data.template.QuestTemplate;
import net.noyji.thequestforge.data.template.components.TemplateDialog;
import net.noyji.thequestforge.data.template.components.TemplateDialogButton;

import java.util.function.Supplier;

public record ActionHandlerC2SPacket(String questId, String dialogKey, int buttonIndex, int entityId) {

    public static void encode(ActionHandlerC2SPacket packet, FriendlyByteBuf buf){
        buf.writeUtf(packet.questId);
        buf.writeUtf(packet.dialogKey);
        buf.writeInt(packet.buttonIndex);
        buf.writeInt(packet.entityId);
    }

    public static ActionHandlerC2SPacket decode(FriendlyByteBuf buf){
        return new ActionHandlerC2SPacket(buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readInt());
    }

    public static void handle(ActionHandlerC2SPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            ResourceLocation id = ResourceLocation.parse(packet.questId);
            QuestTemplate questTemplate = QuestTemplateManager.INSTANCE.getQuestTemplate(id);
            if (questTemplate == null) return;

            TemplateDialog templateDialog = questTemplate.getDialogByKey(packet.dialogKey);
            if (templateDialog == null) return;

            TemplateDialogButton templateDialogButton = templateDialog.getButtons().get(packet.buttonIndex);
            if (templateDialogButton == null) return;

            Player player = context.get().getSender();
            Entity entity = player.level().getEntity(packet.entityId);

            templateDialogButton.runActions(player, entity);

        });
        context.get().setPacketHandled(true);
    }
}
