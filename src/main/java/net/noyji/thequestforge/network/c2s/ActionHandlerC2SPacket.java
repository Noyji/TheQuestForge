package net.noyji.thequestforge.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkEvent;
import net.noyji.thequestforge.api.quest.action.ActionContext;
import net.noyji.thequestforge.common.util.Util;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;
import net.noyji.thequestforge.data.template.QuestTemplate;
import net.noyji.thequestforge.data.template.components.TemplateDialog;
import net.noyji.thequestforge.data.template.components.TemplateDialogButton;

import java.util.List;
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

            ServerPlayer player = context.get().getSender();

            if (templateDialogButton.hasActions()) {
                Entity entity = player.level().getEntity(packet.entityId);

                ActionContext actionContext = new ActionContext();

                actionContext.setPlayer(player);
                actionContext.setEntity(entity);
                actionContext.setButton(templateDialogButton);

                templateDialogButton.runActions(actionContext);
            }
            if (templateDialogButton.hasFunctions()){
                templateDialogButton.runFunctions(player);
            }
            if (templateDialogButton.hasItemToGive()){
                List<ItemStack> giveList = templateDialogButton.getGiveItems();
                for (ItemStack itemStack : giveList){
                    ItemHandlerHelper.giveItemToPlayer(player, itemStack);
                }
            }
            if (templateDialogButton.hasItemToRemove()){
                List<ItemStack> removeList = templateDialogButton.getRemoveItem();
                for (ItemStack itemStack : removeList){
                    Util.removeItemFromPlayer(player, itemStack, itemStack.getCount());
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
