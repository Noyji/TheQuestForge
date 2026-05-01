package net.noyji.thequestforge.client.gui.entity;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.client.gui.components.DialogManager;
import net.noyji.thequestforge.client.gui.components.DialogOptionSelector;
import net.noyji.thequestforge.client.gui.components.TypewriterTextWidget;
import net.noyji.thequestforge.client.render.DialogueCameraManager;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class QuestGiverGUI extends Screen {

    private static final ResourceLocation DIALOG_FRAME_TEXTURE =
            TheQuestForge.id("textures/gui/quest_giver/dialog_frame.png");

    private final Entity entity;

    private int mainPosX;
    private int mainPosY;

    private TypewriterTextWidget typewriterTextWidget;
    private DialogOptionSelector optionSelector;

    public QuestGiverGUI(Entity entity) {
        super(Component.literal("quest_giver_gui"));
        this.entity = entity;
    }

    @Override
    protected void init() {
        mainPosX = 30;
        mainPosY = this.height / 2 + 50;

        DialogueCameraManager.startFocus(entity);

        createWidgets();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        optionSelector.visible = typewriterTextWidget.isFinished();

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.blit(DIALOG_FRAME_TEXTURE, mainPosX, mainPosY, 0, 0, 330, 100, 330, 100);
        typewriterTextWidget.render(guiGraphics, mouseX, mouseY, partialTick);

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE){
            typewriterTextWidget.skipAnimation();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        super.removed();
        DialogueCameraManager.stopFocus();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void createWidgets(){
        typewriterTextWidget = new TypewriterTextWidget.Builder()
                .position(mainPosX + 7, mainPosY +7)
                .width(316)
                .speed(35.0f)
                .soundSettings(2, 0.8f)
                .onComplete(() -> {
                })
                .build();

        optionSelector = DialogOptionSelector.builder(mainPosX + 350, mainPosY, 150, 100)
                .align(DialogOptionSelector.Alignment.LEFT)
                .build();
        optionSelector.visible = false;

        DialogManager dialogManager = new DialogManager(this.entity, this.typewriterTextWidget, this.optionSelector, this::onClose);

        this.addRenderableWidget(this.typewriterTextWidget);
        this.addRenderableWidget(this.optionSelector);
    }
}
