package net.noyji.thequestforge.client.gui.book;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.client.gui.components.AnimatedButton;
import org.jetbrains.annotations.NotNull;

public class QuestBookGui extends Screen {

    private static final ResourceLocation QUEST_BOOK_TEXTURE =
            TheQuestForge.id("textures/gui/quest_book/quest_book.png");

    private final float bookScale = 1.25F;

    private AnimatedButton leftPageButton;
    private AnimatedButton rightPageButton;

    private final QuestBookManager manager = new QuestBookManager();

    public QuestBookGui(){
        super(Component.literal("Quest Book"));
    }

    @Override
    protected void init() {
        float finalScale = getFinalScale();
        manager.init();

        leftPageButton = new AnimatedButton.Builder()
                .position((int) (this.width / 2f - (160 * finalScale)), (int) (this.height / 2f + 30 * finalScale))
                .size(40, 25)
                .scale(finalScale)
                .texture(QUEST_BOOK_TEXTURE)
                .textureSize(512, 512)
                .uv(281, 89)
                .animation(AnimatedButton.AnimationDirection.LEFT, 10.0F, 2.55F)
                .easing(AnimatedButton.EasingType.EASE_OUT)
                .onPress(button -> {
                    if(manager.questIndexDown()){
                        Minecraft.getInstance().getSoundManager().play(
                                SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F)
                        );
                    }
                })
                .build();

        this.addRenderableWidget(leftPageButton);

        rightPageButton = new AnimatedButton.Builder()
                .position((int) (this.width / 2f + (120 * finalScale)), (int) (this.height / 2f + 30 * finalScale))
                .size(40, 25)
                .scale(finalScale)
                .texture(QUEST_BOOK_TEXTURE)
                .textureSize(512, 512)
                .uv(281, 63)
                .animation(AnimatedButton.AnimationDirection.RIGHT, 10.0F, 2.55F)
                .easing(AnimatedButton.EasingType.EASE_OUT)
                .onPress(button -> {
                    if(manager.questIndexUp()){
                        Minecraft.getInstance().getSoundManager().play(
                                SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F)
                        );
                    }
                })
                .build();

        this.addRenderableWidget(rightPageButton);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        drawBookBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        drawButtonIcons(guiGraphics);

        drawBookPages(guiGraphics);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawButtonIcons(GuiGraphics guiGraphics) {
        float scale = getFinalScale();
        RenderSystem.enableBlend();

        int leftX = (int) (this.width / 2f - (135 * scale));
        int leftY = (int) (this.height / 2f + 30 * scale);
        guiGraphics.blit(QUEST_BOOK_TEXTURE, leftX, leftY, (int) (20 * scale), (int) (25 * scale), 463, 109, 40, 20, 512, 512);

        int rightX = (int) (this.width / 2f + (126 * scale));
        int rightY = (int) (this.height / 2f + 30 * scale);
        guiGraphics.blit(QUEST_BOOK_TEXTURE, rightX, rightY, (int) (20 * scale), (int) (25 * scale), 463, 83, 40, 20, 512, 512);

        RenderSystem.disableBlend();
    }

    private void drawBookBackground(GuiGraphics guiGraphics){
        int textureWidth = 280;
        int textureHeight = 180;
        float scale = getFinalScale();

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.scale(scale, scale, 1.0f);

        int x = (int) (((this.width / 2F) / scale) - (textureWidth / 2F));
        int y = (int) (((this.height / 2F) / scale) - (textureHeight / 2F));

        guiGraphics.blit(QUEST_BOOK_TEXTURE, x, y, textureWidth, textureHeight, 0, 0, textureWidth, textureHeight, 512, 512);
        pose.popPose();
    }

    private void drawBookPages(GuiGraphics guiGraphics){
        int textureWidth = 280;
        int textureHeight = 180;

        float globalScale = getFinalScale();

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();

        pose.scale(globalScale, globalScale, 1.0f);

        int bookX = (int) (((this.width / 2F) / globalScale) - (textureWidth / 2F));
        int bookY = (int) (((this.height / 2F) / globalScale) - (textureHeight / 2F));

        guiGraphics.blit(QUEST_BOOK_TEXTURE, bookX, bookY, textureWidth, textureHeight, 0, 180, textureWidth, textureHeight, 512, 512);

        float textInverseScale = globalScale > 1.0f ? (1.0f / globalScale) : 1.0f;

        pose.pushPose();

        pose.scale(textInverseScale, textInverseScale, 1.0f);

        int textX = (int) (bookX / textInverseScale);
        int textY = (int) (bookY / textInverseScale);

        float scaledPageWidth = 110 / textInverseScale;

        drawQuestInfo(guiGraphics, textX, textY, (int) scaledPageWidth + 14);

        pose.popPose();
        pose.popPose();
    }

    private void drawQuestInfo(GuiGraphics guiGraphics, int x, int y, int pageWidth) {
        int textColor = 0x3F3F3F;
        int startX = x + 19;
        int currentY = drawQuestTitle(guiGraphics, startX, y, textColor, pageWidth);

        guiGraphics.drawString(this.font, manager.getQuestRarity(), startX, currentY, textColor, false);
        currentY += this.font.lineHeight + 2;

        currentY += drawQuestTime(guiGraphics, startX, currentY, textColor);

        guiGraphics.drawWordWrap(this.font, manager.getQuestDescription(), startX, currentY + 2, pageWidth, textColor);
    }

    private int drawQuestTitle(GuiGraphics guiGraphics, int x, int y, int textColor, int pageWidth) {
        int currentY = y + 16;

        Component questName = manager.getQuestName();
        var lines = this.font.split(questName, pageWidth);

        int linesToDraw = Math.min(lines.size(), 2);

        for (int i = 0; i < linesToDraw; i++) {
            if (i == 1 && lines.size() > 2) {
                var textLines = this.font.getSplitter().splitLines(questName, pageWidth, questName.getStyle());
                String line2Raw = textLines.get(1).getString();
                String cut = this.font.plainSubstrByWidth(line2Raw, pageWidth - this.font.width("..."));

                Component ellipsisLine = Component.literal(cut + "...").withStyle(questName.getStyle());
                guiGraphics.drawString(this.font, ellipsisLine, x, currentY, textColor, false);
            } else {
                guiGraphics.drawString(this.font, lines.get(i), x, currentY, textColor, false);
            }
            currentY += this.font.lineHeight;
        }
        return currentY + 5;
    }

    private int drawQuestTime(GuiGraphics guiGraphics, int x, int y, int textColor){
        if (manager.getTimeLimit() == null) return y;
        guiGraphics.drawString(this.font, manager.getTimeLimit(), x, y, textColor, false);
        return this.font.lineHeight + 2;
    }

    private float getFinalScale() {
        int textureWidth = 280;
        float logicalWidth = textureWidth * this.bookScale + 200;

        float maxAllowedWidth = this.width * 0.95F;
        float adaptiveScale = Math.min(1.0F, maxAllowedWidth / logicalWidth);

        return this.bookScale * adaptiveScale;
    }
}
