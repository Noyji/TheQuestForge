package net.noyji.thequestforge.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;
import net.noyji.thequestforge.data.quest.entity.Quest;
import net.noyji.thequestforge.data.quest.entity.components.QuestDialog;
import net.noyji.thequestforge.data.template.QuestTemplate;
import net.noyji.thequestforge.data.template.components.TemplateDialog;
import net.noyji.thequestforge.data.template.components.TemplateDialogButton;
import net.noyji.thequestforge.network.ModNetworking;
import net.noyji.thequestforge.network.c2s.ActionHandlerC2SPacket;

public class DialogManager {

    private final Quest quest;
    private final QuestTemplate template;

    private final TypewriterTextWidget textWidget;
    private final DialogOptionSelector optionSelector;

    private final Runnable onCloseScreen;

    private final String languageKey;
    private final int entityId;
    private String currentDialogKey = "start";

    public DialogManager(Entity npcEntity, TypewriterTextWidget textWidget, DialogOptionSelector optionSelector, Runnable onCloseScreen) {
        this.quest = getQuest(npcEntity);
        this.entityId = npcEntity.getId();
        this.template = QuestTemplateManager.INSTANCE.getQuestTemplate(quest.getSourceTemplate());

        this.textWidget = textWidget;
        this.optionSelector = optionSelector;
        this.onCloseScreen = onCloseScreen;
        this.languageKey = Minecraft.getInstance().options.languageCode;


        setupOptionAction();

        updateDialog(this.currentDialogKey);
    }

    private void setupOptionAction() {
        this.optionSelector.setOnSelectAction((TemplateDialogButton selectedButton) -> {

            //TODO:Доделать обработку действий(Actions)

            if (selectedButton.hasAction("thequestforge:close")) {
                if (this.onCloseScreen != null) {
                    this.onCloseScreen.run();
                    return;
                }
            }

            if (selectedButton.hasActions()){
                ModNetworking.sendToServer(new ActionHandlerC2SPacket(quest.getSourceTemplate().toString(),
                        currentDialogKey, optionSelector.getSelectedIndex(), entityId));
            }

            String toGo = selectedButton.getToGo();

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

        this.textWidget.setText(dialogText.replace('&', '\u00A7'));

        this.optionSelector.setOptions(templateDialog.getButtons(), questDialog.getButtonIndices());
    }

    private Quest getQuest(Entity entity){
        if (entity == null) return null;
        return CapabilityUtil.getEntityQuestData(entity).getQuest();
    }
}