package net.noyji.thequestforge.client.gui.entity;

import com.mojang.blaze3d.vertex.PoseStack;
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

    private TypewriterTextWidget typewriterTextWidget;
    private DialogOptionSelector optionSelector;

    public QuestGiverGUI(Entity entity) {
        super(Component.literal("quest_giver_gui"));
        this.entity = entity;
    }

    @Override
    protected void init() {
        super.init();

        DialogueCameraManager.startFocus(entity);

        createTypewriterTextWidget();
        createOptionSelector();
        DialogManager dialogManager =
                new DialogManager(this.entity, this.typewriterTextWidget, this.optionSelector, this::onClose);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        optionSelector.setVisible(typewriterTextWidget.isFinished());

        super.render(guiGraphics, mouseX, mouseY, partialTick);
        drawDialogFrame(guiGraphics);
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

    private void createTypewriterTextWidget(){
        int dialogWidth = Math.min(this.width - 80, 318);
        int dialogX = (this.width ) / 2 - 200;
        int dialogY = this.height - 100;

        if (typewriterTextWidget == null) {
            typewriterTextWidget = new TypewriterTextWidget.Builder()
                    .position(dialogX, dialogY)
                    .width(dialogWidth)
                    .speed(35.0f)
                    .soundSettings(2, 0.8f)
                    .onComplete(() -> {
                    })
                    .build();
        } else {
            typewriterTextWidget.updateBounds(dialogX, dialogY, dialogWidth);
        }
        this.addRenderableWidget(this.typewriterTextWidget);
    }

    private void createOptionSelector(){
        int selectorWidth = 200;
        int selectorHeight = 80;

        int selectorX = this.width / 2 + 110;
        int selectorY = this.height - selectorHeight - 20;

        if (this.optionSelector == null) {
            this.optionSelector = DialogOptionSelector.builder(selectorX, selectorY, selectorWidth, selectorHeight)
                    .align(DialogOptionSelector.Alignment.LEFT)
                    .build();
        } else {
            this.optionSelector.updateBounds(selectorX, selectorY, selectorWidth, selectorHeight);
        }
        this.addRenderableWidget(this.optionSelector);
    }

    private void drawDialogFrame(GuiGraphics guiGraphics){
        float baseWidth = 330F;
        float baseHeight = 100F;

        float maxAllowedWidth = this.width * 0.95F;
        float scale = Math.min(1.0F, maxAllowedWidth / baseWidth);

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.scale(scale, scale, 1.0f);

        int x = (int) (((this.width / 2F - 50F) / scale) - (baseWidth / 2F));
        int y = (int) ((this.height - 10f) / scale - baseHeight);

        guiGraphics.blit(DIALOG_FRAME_TEXTURE, x, y, 0, 0, 330, 100, 330, 100);
        pose.popPose();
    }
}
