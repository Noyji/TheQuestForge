package net.noyji.thequestforge.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.noyji.thequestforge.api.client.registry.PlaceholderRegistry;
import net.noyji.thequestforge.common.util.Util;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.capability.player.PlayerQuestData;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;
import net.noyji.thequestforge.data.quest.entity.Quest;
import net.noyji.thequestforge.data.quest.entity.components.QuestDialog;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;
import net.noyji.thequestforge.data.template.QuestTemplate;
import net.noyji.thequestforge.data.template.components.TemplateDialog;
import net.noyji.thequestforge.data.template.components.TemplateDialogButton;
import net.noyji.thequestforge.network.TheQuestForgeNetworking;
import net.noyji.thequestforge.network.c2s.ActionHandlerC2SPacket;

import java.util.UUID;

public class DialogManager {
    private final Quest quest;
    private final QuestTemplate template;

    private final TypewriterTextWidget textWidget;
    private final DialogOptionSelector optionSelector;

    private final Runnable onCloseScreen;

    private final Player player;
    private final Entity entity;

    private final String languageKey;
    private String currentDialogKey;

    public DialogManager(Entity npcEntity, TypewriterTextWidget textWidget, DialogOptionSelector optionSelector, Runnable onCloseScreen) {
        this.player = Minecraft.getInstance().player;
        this.entity = npcEntity;
        this.quest = getQuest();
        this.template = getTemplate();
        this.currentDialogKey = getDialogKey(npcEntity);
        this.textWidget = textWidget;

        this.optionSelector = optionSelector;
        this.optionSelector.setEntity(entity);
        this.optionSelector.setPlayer(player);
        this.optionSelector.setQuest(quest);

        this.onCloseScreen = onCloseScreen;
        this.languageKey = Minecraft.getInstance().options.languageCode;

        setupOptionAction();

        updateDialog(this.currentDialogKey);
    }

    private void setupOptionAction() {
        this.optionSelector.setOnSelectAction((TemplateDialogButton selectedButton) -> {
            if (player == null) return;

            if (selectedButton.hasAction("thequestforge:close") || selectedButton.hasAction("close")) {
                if (this.onCloseScreen != null) {
                    this.onCloseScreen.run();
                    return;
                }
            }

            boolean transition = true;

            String toGo = selectedButton.getToGo();

            if (selectedButton.hasItemToRemove()){
                for (ItemStack stack : selectedButton.getRemoveItem()){
                    if (!Util.hasItem(player, stack)){
                        transition = false;
                        toGo = selectedButton.getAltToGo();
                        break;
                    }
                }
            }

            if (selectedButton.hasAction("thequestforge:complete") || selectedButton.hasAction("complete")){

                PlayerQuest playerQuest = CapabilityUtil.getPlayerQuestData(player).getQuest(quest.getId());
                if (playerQuest == null || !playerQuest.isComplete()) {
                    transition = false;
                    toGo = selectedButton.getAltToGo();
                }
            }

            if (transition){
                TheQuestForgeNetworking.sendToServer(new ActionHandlerC2SPacket(quest.getSourceTemplate().toString(), currentDialogKey, optionSelector.getSelectedIndex(), entity.getId()));
            }

            if (toGo != null && !toGo.isEmpty()) {
                updateDialog(toGo);
            }
        });
    }

    public void updateDialog(String key) {
        if (this.template == null || this.quest == null) return;

        this.currentDialogKey = key;

        TemplateDialog templateDialog = template.getDialogByKey(key);
        if (templateDialog == null) return;

        QuestDialog questDialog = quest.getDialog(key);
        if (questDialog == null) return;

        String dialogText = templateDialog.getTranslateText(languageKey, questDialog.getTextIndex());
        dialogText = dialogText.replace('&', '\u00A7');
        dialogText = PlaceholderRegistry.parse(dialogText, player, entity, quest);

        this.textWidget.setText(dialogText);

        this.optionSelector.setOptions(templateDialog.getButtons(), questDialog.getButtonIndices());
    }

    private Quest getQuest(){
        if (entity == null || player == null) return null;

        if (CapabilityUtil.getEntityQuestData(entity).getChainLength() == 1){
            return CapabilityUtil.getEntityQuestData(entity).getQuest();
        }

        int index = CapabilityUtil.getPlayerQuestData(player).getChainProgress(entity.getUUID());
        return CapabilityUtil.getEntityQuestData(entity).getQuest(index);
    }

    private String getDialogKey(Entity entity){
        if (player == null) return "start";
        UUID questId = entity.getUUID();

        PlayerQuestData playerQuestData = CapabilityUtil.getPlayerQuestData(player);

        String key = playerQuestData.getDialogProgress(questId);

        if (key == null) key = playerQuestData.getSpareDialogStage(questId);

        return key;
    }

    private QuestTemplate getTemplate(){
        if (quest == null) return null;
        return QuestTemplateManager.INSTANCE.getQuestTemplate(quest.getSourceTemplate());
    }
}