package net.noyji.thequestforge.client.gui.entity;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.client.gui.components.DialogManager;
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

    private TypewriterTextWidget dialogText;

    public QuestGiverGUI(Entity entity) {
        super(Component.literal("quest_giver_gui"));
        this.entity = entity;
    }

    @Override
    protected void init() {
        mainPosX = 30;
        mainPosY = this.height / 2 + 50;

        DialogueCameraManager.startFocus(entity);
        dialogText = new TypewriterTextWidget.Builder()
                .position(mainPosX + 7, mainPosY +7)
                .width(316)
                .text("Patchouli's systems allow any modder or modpack §0 maker to quickly create beautiful  §k books full of user experience enhancing features. The user-facing feature set of the mod is designed in function of research done on what features people liked from Botania's Lexica Botania.")
                .speed(35.0f)
                .soundSettings(2, 0.8f)
                .onComplete(() -> {
                })
                .build();

        DialogManager dialogManager = new DialogManager(entity, dialogText);

        this.addRenderableWidget(dialogText);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.blit(DIALOG_FRAME_TEXTURE, mainPosX, mainPosY, 0, 0, 330, 100, 330, 100);
        dialogText.render(guiGraphics, mouseX, mouseY, partialTick);

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE){
            dialogText.skipAnimation();
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
}
