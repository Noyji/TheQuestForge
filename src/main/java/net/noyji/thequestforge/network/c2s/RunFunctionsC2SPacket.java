package net.noyji.thequestforge.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;
import net.noyji.thequestforge.data.template.QuestTemplate;
import net.noyji.thequestforge.data.template.components.TemplateDialog;
import net.noyji.thequestforge.data.template.components.TemplateDialogButton;

import java.util.function.Supplier;

public record RunFunctionsC2SPacket(String questId, String dialogKey, int buttonIndex){
    //TODO: скорее всего не надо
    public static void encode(RunFunctionsC2SPacket packet, FriendlyByteBuf buf){
        buf.writeUtf(packet.questId);
        buf.writeUtf(packet.dialogKey);
        buf.writeInt(packet.buttonIndex);
    }
    
    public static RunFunctionsC2SPacket decode(FriendlyByteBuf buf){
        return new RunFunctionsC2SPacket(buf.readUtf(), buf.readUtf(), buf.readInt());
    }
    
    public static void handle(RunFunctionsC2SPacket packet, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork( () -> {
            ResourceLocation id = ResourceLocation.parse(packet.questId);
            QuestTemplate questTemplate = QuestTemplateManager.INSTANCE.getQuestTemplate(id);
            if (questTemplate == null) return;

            TemplateDialog templateDialog = questTemplate.getDialogByKey(packet.dialogKey);
            if (templateDialog == null) return;

            TemplateDialogButton templateDialogButton = templateDialog.getButtons().get(packet.buttonIndex);
            if (templateDialogButton == null) return;

            templateDialogButton.runFunctions(context.get().getSender());
        });
        context.get().setPacketHandled(true);
    }
}
