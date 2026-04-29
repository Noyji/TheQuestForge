package net.noyji.thequestforge.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;
import net.noyji.thequestforge.data.quest.entity.Quest;
import net.noyji.thequestforge.data.quest.entity.components.QuestDialog;
import net.noyji.thequestforge.data.template.QuestTemplate;
import net.noyji.thequestforge.data.template.components.TemplateDialog;

public class DialogManager {
    private final Quest quest;
    private final TypewriterTextWidget textWidget;
    private final String languageKey = Minecraft.getInstance().options.languageCode;
    private final QuestTemplate template;
    private String dialogKey = "start";

    public DialogManager(Entity entity, TypewriterTextWidget textWidget) {
        this.quest = getQuest(entity);
        this.textWidget = textWidget;
        this.template = QuestTemplateManager.INSTANCE.getQuestTemplate(quest.getSourceTemplate());
        updateText(dialogKey);
    }

    public void updateText(String key){
        TemplateDialog templateDialog = template.getDialogByKey(key);
        if (templateDialog == null) return;

        QuestDialog questDialog = quest.getDialog(key);
        if (questDialog == null) return;

        String dialogText =  templateDialog.getTranslateText(languageKey, questDialog.getTextIndex());
        textWidget.setText(dialogText);
    }

    private Quest getQuest(Entity entity){
        return CapabilityUtil.getEntityQuestData(entity).getQuest();
    }
}
