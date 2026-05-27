package net.noyji.thequestforge.client.gui.entity;

import com.mojang.blaze3d.systems.RenderSystem;
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
import net.noyji.thequestforge.common.sounds.TheQuestForgeSounds;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class QuestGiverGUI extends Screen {

    private static final ResourceLocation DIALOG_FRAME_TEXTURE =
            TheQuestForge.id("textures/gui/quest_giver/dialog_frame.png");
    private static final ResourceLocation BLUR_VIGNETTE_TEXTURE =
            TheQuestForge.id("textures/gui/quest_giver/blur_vignette.png");

    private final Entity entity;

    private TypewriterTextWidget typewriterTextWidget;
    private DialogOptionSelector optionSelector;

    private int frameX;
    private int frameY;
    private int frameWidth;
    private int frameHeight;

    public QuestGiverGUI(Entity entity) {
        super(Component.literal("quest_giver_gui"));
        this.entity = entity;
    }

    @Override
    protected void init() {
        super.init();

        DialogueCameraManager.startFocus(entity);

        int baseFrameWidth = 330;
        int baseSelectorWidth = 200;
        int gap = 10;
        int bottomPadding = 10;
        this.frameHeight = 100;

        int totalBaseWidth = baseFrameWidth + gap + baseSelectorWidth;

        this.frameWidth = baseFrameWidth;
        int selectorWidth = baseSelectorWidth;

        if (totalBaseWidth > this.width - 20) {
            float responsiveScale = (this.width - 20f) / totalBaseWidth;
            this.frameWidth = (int) (baseFrameWidth * responsiveScale);
            selectorWidth = (int) (baseSelectorWidth * responsiveScale);
        }

        int totalWidth = this.frameWidth + gap + selectorWidth;
        int startX = (this.width - totalWidth) / 2;

        this.frameX = startX;
        this.frameY = this.height - this.frameHeight - bottomPadding;

        int selectorX = this.frameX + this.frameWidth + gap;
        int selectorY = this.height - 80 - (bottomPadding + 10);

        int textPaddingX = 15;
        int textPaddingY = 15;
        int dialogX = this.frameX + textPaddingX;
        int dialogY = this.frameY + textPaddingY;
        int dialogWidth = this.frameWidth - (textPaddingX * 2);

        createTypewriterTextWidget(dialogX, dialogY, dialogWidth);
        createOptionSelector(selectorX, selectorY, selectorWidth, 80);

        DialogManager dialogManager = new DialogManager(
                this.entity, this.typewriterTextWidget, this.optionSelector, this::onClose
        );
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        optionSelector.setVisible(typewriterTextWidget.isFinished());

        drawDialogFrame(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        drawBlurVignette(guiGraphics);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
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

    private void createTypewriterTextWidget(int x, int y, int width) {
        if (typewriterTextWidget == null) {
            typewriterTextWidget = new TypewriterTextWidget.Builder()
                    .position(x, y)
                    .width(width)
                    .speed(35.0f)
                    .sound(TheQuestForgeSounds.NPC_VOICE.get())
                    .soundSettings(3, 0.9f)
                    .onComplete(() -> {})
                    .build();
            this.addRenderableWidget(this.typewriterTextWidget);
        } else {
            typewriterTextWidget.updateBounds(x, y, width);
            if (!this.renderables.contains(typewriterTextWidget)) {
                this.addRenderableWidget(typewriterTextWidget);
            }
        }
    }

    private void createOptionSelector(int x, int y, int width, int height) {
        if (this.optionSelector == null) {
            this.optionSelector = DialogOptionSelector.builder(x, y, width, height)
                    .align(DialogOptionSelector.Alignment.LEFT)
                    .build();
            this.addRenderableWidget(this.optionSelector);
        } else {
            this.optionSelector.updateBounds(x, y, width, height);
            if (!this.renderables.contains(optionSelector)) {
                this.addRenderableWidget(optionSelector);
            }
        }
    }

    private void drawDialogFrame(GuiGraphics guiGraphics) {
        RenderSystem.enableBlend();
        guiGraphics.blit(DIALOG_FRAME_TEXTURE, frameX, frameY, frameWidth, frameHeight, 0, 0, 330, 100, 330, 100);
        RenderSystem.disableBlend();
    }

    private void drawBlurVignette(GuiGraphics guiGraphics) {
        RenderSystem.enableBlend();
        guiGraphics.blit(BLUR_VIGNETTE_TEXTURE, 0, 0, this.width, this.height, 0, 0, 256, 256, 256, 256);
        RenderSystem.disableBlend();
    }
}