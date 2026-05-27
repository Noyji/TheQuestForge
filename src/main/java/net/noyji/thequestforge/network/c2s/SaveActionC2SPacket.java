package net.noyji.thequestforge.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.action.ActionContext;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;
import net.noyji.thequestforge.data.template.QuestTemplate;
import net.noyji.thequestforge.data.template.components.TemplateDialog;
import net.noyji.thequestforge.data.template.components.TemplateDialogButton;

import java.util.function.Supplier;

public record SaveActionC2SPacket(String questId, String dialogKey, int buttonIndex, int entityId) {

    public static void encode(SaveActionC2SPacket packet, FriendlyByteBuf buf){
        buf.writeUtf(packet.questId);
        buf.writeUtf(packet.dialogKey);
        buf.writeInt(packet.buttonIndex);
        buf.writeInt(packet.entityId);
    }

    public static SaveActionC2SPacket decode(FriendlyByteBuf buf){
        return new SaveActionC2SPacket(buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readInt());
    }

    public static void handle(SaveActionC2SPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(() -> {
            ResourceLocation id = ResourceLocation.parse(packet.questId);
            QuestTemplate questTemplate = QuestTemplateManager.INSTANCE.getQuestTemplate(id);
            if (questTemplate == null) return;

            TemplateDialog templateDialog = questTemplate.getDialogByKey(packet.dialogKey);
            if (templateDialog == null) return;

            TemplateDialogButton templateDialogButton = templateDialog.getButtons().get(packet.buttonIndex);
            if (templateDialogButton == null) return;

            ActionContext actionContext = new ActionContext();

            Player player = context.get().getSender();
            Entity entity = player.level().getEntity(packet.entityId);

            actionContext.setButton(templateDialogButton);
            actionContext.setPlayer(player);
            actionContext.setEntity(entity);

            ResourceLocation saveLoc = TheQuestForge.parse("save");

            templateDialogButton.runAction(actionContext, saveLoc);
        });
        context.get().setPacketHandled(true);
    }
}
