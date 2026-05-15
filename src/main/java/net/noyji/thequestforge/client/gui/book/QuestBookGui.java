package net.noyji.thequestforge.client.gui.book;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.client.gui.book.components.QuestRewardGridWidget;
import net.noyji.thequestforge.client.gui.book.components.QuestTaskListWidget;
import net.noyji.thequestforge.client.gui.components.AnimatedButton;
import net.noyji.thequestforge.common.sounds.TheQuestForgeSounds;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class QuestBookGui extends Screen {

    private static final ResourceLocation QUEST_BOOK_TEXTURE =
            TheQuestForge.id("textures/gui/quest_book/quest_book.png");

    private float scale;

    private int mainPosX;
    private int mainPosY;
    private int textColor = 0x3F3F3F;
    private int pageWidth = 0;

    private AnimatedButton leftPageButton;
    private AnimatedButton rightPageButton;
    private AnimatedButton removeQuestButton;

    private QuestTaskListWidget taskListWidget;
    private QuestRewardGridWidget rewardGridWidget;

    private final QuestBookManager manager = new QuestBookManager();

    public QuestBookGui(){
        super(Component.literal("Quest Book"));
    }

    @Override
    protected void init() {
        scale = getFinaleScale();

        mainPosX = (int) (((float) this.width / 2) - ((280 * scale) / 2));
        mainPosY = (int) (((float) this.height / 2) -  ((180 * scale) / 2));

        manager.init();

        taskListWidget = new QuestTaskListWidget.Builder()
                .position((int) (this.width / 2F + (10 * scale)), (int) (this.height / 2F - 62 * scale))
                .size((int) (115 * scale), (int) (60 * scale))
                .scale(Math.min(1.0F, scale))
                .tasks(manager.getCurrentQuestTasks())
                .build();

        this.addRenderableWidget(taskListWidget);

        rewardGridWidget = new QuestRewardGridWidget.Builder()
                .position((int) (this.width / 2F + (10 * scale)), (int) (this.height / 2F + 14 * scale))
                .size((int) (115 * scale), (int) (60 * scale))
                .scale(Math.min(1.0F, scale))
                .rewards(manager.getCurrentQuestRewards(), manager.getQuestXp(), manager.getQuestCurrency())
                .build();

        this.addRenderableWidget(rewardGridWidget);

        removeQuestButton = new AnimatedButton.Builder()
                .position((int) (this.width / 2f - (30 * scale)), (int) (this.height / 2f - 105 * scale))
                .size(20, 40)
                .scale(scale)
                .texture(QUEST_BOOK_TEXTURE)
                .textureSize(512, 512)
                .uv(322, 0)
                .animation(AnimatedButton.AnimationDirection.UP, 10.0F, 2.55F)
                .easing(AnimatedButton.EasingType.EASE_OUT)
                .onPress(button -> {
                    if (manager.removeQuest()) {
                        taskListWidget.setTasks(manager.getCurrentQuestTasks());
                        rewardGridWidget.setRewards(manager.getCurrentQuestRewards(), manager.getQuestXp(), manager.getQuestCurrency());
                        Minecraft.getInstance().getSoundManager().play(
                                SimpleSoundInstance.forUI(TheQuestForgeSounds.REMOVE_QUEST.get(), 1.0F, 1.0F)
                        );
                    } else {
                        taskListWidget.setTasks(null);
                        rewardGridWidget.setRewards(null, -1, -1);
                    }
                })
                .build();

        this.addRenderableWidget(removeQuestButton);

        leftPageButton = new AnimatedButton.Builder()
                .position((int) (this.width / 2f - (160 * scale)), (int) (this.height / 2f + 30 * scale))
                .size(40, 25)
                .scale(scale)
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
                        taskListWidget.setTasks(manager.getCurrentQuestTasks());
                        rewardGridWidget.setRewards(manager.getCurrentQuestRewards(), manager.getQuestXp(), manager.getQuestCurrency());
                    }
                })
                .build();

        this.addRenderableWidget(leftPageButton);

        rightPageButton = new AnimatedButton.Builder()
                .position((int) (this.width / 2f + (120 * scale)), (int) (this.height / 2f + 30 * scale))
                .size(40, 25)
                .scale(scale)
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
                        taskListWidget.setTasks(manager.getCurrentQuestTasks());
                        rewardGridWidget.setRewards(manager.getCurrentQuestRewards(), manager.getQuestXp(), manager.getQuestCurrency());
                    }
                })
                .build();

        this.addRenderableWidget(rightPageButton);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        drawBookBackground(guiGraphics);
        drawButtons(guiGraphics, mouseX, mouseY, partialTick);
        drawBookPages(guiGraphics);

        drawLeftSidePage(guiGraphics);
        drawRightSidePage(guiGraphics, mouseX, mouseY, partialTick);
        drawNumbering(guiGraphics);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawBookBackground(GuiGraphics guiGraphics){
        guiGraphics.blit(QUEST_BOOK_TEXTURE, mainPosX, mainPosY, (int) (280 * scale), (int) (180 * scale), 0, 0, 280, 180, 512, 512);
    }

    private void drawButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick){
        leftPageButton.render(guiGraphics, mouseX, mouseY, partialTick);
        rightPageButton.render(guiGraphics, mouseX, mouseY, partialTick);
        removeQuestButton.render(guiGraphics, mouseX, mouseY, partialTick);

        RenderSystem.enableBlend();
        guiGraphics.blit(QUEST_BOOK_TEXTURE, (int) (this.width / 2F - (138 * scale)), (int) (this.height / 2f + 30 * scale),
                (int) (20 * scale), (int) (25 * scale), 463, 109, 20, 25, 512, 512);
        guiGraphics.blit(QUEST_BOOK_TEXTURE, (int) (this.width / 2F + (118 * scale)), (int) (this.height / 2f + 30 * scale),
                (int) (20 * scale), (int) (25 * scale), 463, 83, 20, 25, 512, 512);
        RenderSystem.disableBlend();
    }

    private void drawBookPages(GuiGraphics guiGraphics){
        guiGraphics.blit(QUEST_BOOK_TEXTURE, mainPosX, mainPosY, (int) (280 * scale), (int) (180 * scale), 0, 180, 280, 180, 512, 512);
    }

    private void drawLeftSidePage(GuiGraphics guiGraphics){
        int startX = (int) (mainPosX + (16 * scale));
        int startY = (int) (mainPosY + (15 * scale));
        int pageWidth = (int) ((280 * scale) / 2 - (20 * scale));
        this.pageWidth = pageWidth;
        int currentY = 0;

        float scaleText = Math.min(1.0F, scale);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(startX, startY, 0);
        guiGraphics.pose().scale(scaleText, scaleText, 1.0F);

        //Quest name
        Component questName = manager.getQuestName();
        List<FormattedCharSequence> lines = this.font.split(questName, pageWidth);

        int linesToDraw = Math.min(lines.size(), 2);

        for (int i = 0; i < linesToDraw; i++){
            if ( i == 1 && lines.size() > 2) {
                List<FormattedText> textLines = this.font.getSplitter().splitLines(questName, pageWidth, questName.getStyle());
                String lineToRaw = textLines.get(1).getString();

                String cut = this.font.plainSubstrByWidth(lineToRaw, pageWidth - this.font.width("..."));

                Component ellipsisLine = Component.literal(cut + "...").withStyle(questName.getStyle());
                guiGraphics.drawString(this.font, ellipsisLine, 0, currentY, textColor, false);
            } else {
                guiGraphics.drawString(this.font, lines.get(i), 0, currentY, textColor, false);
            }
            currentY += this.font.lineHeight;
        }
        currentY += 4;
        //Quest rarity
        guiGraphics.drawString(this.font, manager.getQuestRarity(), 0, currentY, textColor, false);
        currentY += this.font.lineHeight + 2;
        //Time limit
        if (manager.getTimeLimit() != null){
            guiGraphics.drawString(this.font, manager.getTimeLimit(), 0, currentY, textColor, false);
            currentY += this.font.lineHeight + 2;
        }
        //Quest description
        guiGraphics.drawWordWrap(this.font, manager.getQuestDescription(), 0, currentY + 3, pageWidth, textColor);
        guiGraphics.pose().popPose();
    }

    private void drawRightSidePage(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick){
        Component taskText = Component.translatable("gui.thequestforge.book.task");
        int taskTextX = (int) (((this.width / 2F + (10 * scale)) + ((float) pageWidth / 2 - (5 * scale)) - ((float) font.width(taskText) / 2)));
        guiGraphics.drawString(this.font, taskText, taskTextX, (int) (this.height / 2F - 78 * scale), textColor, false);

        guiGraphics.blit(QUEST_BOOK_TEXTURE, (int) (this.width / 2F + (10 * scale)), (int) (this.height / 2F - 68 * scale), (int) (115 * scale), (int) (3 * scale), 384, 39, 115, 3, 512, 512);

        taskListWidget.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.blit(QUEST_BOOK_TEXTURE, (int) (this.width / 2F + (10 * scale)), (int) (this.height / 2F + 1 * scale), (int) (115 * scale), (int) (3 * scale), 384, 44, 115, 3, 512, 512);

        Component rewardText = Component.translatable("gui.thequestforge.book.reward");
        int rewardTaskX = (int) (((this.width / 2F + (10 * scale)) + ((float) pageWidth / 2 - (5 * scale)) - ((float) font.width(rewardText) / 2)));
        guiGraphics.drawString(this.font, rewardText, rewardTaskX, (int) (this.height / 2F + 6 * scale), textColor, false);

        rewardGridWidget.render(guiGraphics, mouseX, mouseY, partialTick);

        if (manager.isComplete()) {
            RenderSystem.enableBlend();
            guiGraphics.blit(QUEST_BOOK_TEXTURE, (int) (this.width / 2F + (95 * scale)), (int) (this.height / 2F + 55 * scale), (int) (25 * (scale - 0.25F)), (int) (25 * (scale - 0.25F)), 410, 100, 25, 25, 512, 512);
            RenderSystem.disableBlend();
        }
    }

    private void drawNumbering(GuiGraphics guiGraphics){
        if (manager.getMaxQuestsCount() == 0) return;

        int x = (this.width / 2 - 3);
        int y = (int) ((float) this.height / 2 + (95 * scale));
        String maxQuest = "/ " + manager.getMaxQuestsCount();

        guiGraphics.drawString(this.font, maxQuest, x, y, 0xFFFFFF, false);

        String index = (manager.getQuestIndex() + 1) + " ";
        guiGraphics.drawString(this.font, index , x - this.font.width(index), y, 0xFFFFFF, false);
    }

    private float getFinaleScale(){
        int textureWidth = 280;
        float additionalScale = 1.25F;
        float logicalWidth = textureWidth * additionalScale + 200;

        float maxAllowedWidth = this.width * 0.95F;

        float adaptiveScale = Math.min(1.0F, maxAllowedWidth / logicalWidth);

        return additionalScale * adaptiveScale;
    }
}
