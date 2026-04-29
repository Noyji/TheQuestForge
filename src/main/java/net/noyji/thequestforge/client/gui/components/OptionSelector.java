package net.noyji.thequestforge.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.noyji.thequestforge.data.quest.entity.Quest;
import net.noyji.thequestforge.data.template.QuestTemplate;
import org.jetbrains.annotations.NotNull;

public class OptionSelector extends AbstractWidget {
    private final Quest quest;
    private final QuestTemplate questTemplate;

    private final Font font;
    //TODO: долелать кнопки диалога
    private final int indentation = 15;


    public OptionSelector(int x, int y, int height, int width, Component text, Quest quest, QuestTemplate questTemplate) {
        super(x, y, width, height, text);
        this.quest = quest;
        this.questTemplate = questTemplate;
        this.font = Minecraft.getInstance().font;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

}
