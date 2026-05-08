package net.noyji.thequestforge.client.gui.book.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.client.ITaskRenderer;
import net.noyji.thequestforge.api.client.registry.TaskRendererRegistry;
import net.noyji.thequestforge.api.quest.task.AbstractTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class QuestTaskListWidget extends AbstractWidget {

    private static final ResourceLocation TASK_FRAME =
            ResourceLocation.fromNamespaceAndPath(TheQuestForge.MODID, "textures/gui/quest_book/components/task_frame.png");
    private static final ResourceLocation PROGRESS_BAR =
            ResourceLocation.fromNamespaceAndPath(TheQuestForge.MODID, "textures/gui/quest_book/components/progress_bar.png");
    private static final ResourceLocation UNKNOWN_MOB =
            ResourceLocation.fromNamespaceAndPath(TheQuestForge.MODID, "textures/gui/quest_book/icons/unknown_mob.png");

    private List<AbstractTask<?>> tasks;
    private double scrollAmount = 0;
    private final int entryHeight = 22;

    private float scale = 1.0F;
    private int logicalWidth;
    private int logicalHeight;

    private final boolean showTotalCountOnly;
    private final int textColor;
    private final int countColor;

    private QuestTaskListWidget(int x, int y, int width, int height, float scale, List<AbstractTask<?>> tasks, boolean showTotalCountOnly, int textColor, int countColor) {
        super(x, y, width, height, Component.empty());
        this.scale = scale;
        this.logicalWidth = width;
        this.logicalHeight = height;
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.showTotalCountOnly = showTotalCountOnly;
        this.textColor = textColor;
        this.countColor = countColor;
    }

    public void updateLayout(int logicalX, int logicalY, int logicalWidth, int logicalHeight, float newScale) {
        this.scale = newScale;
        this.logicalWidth = logicalWidth;
        this.logicalHeight = logicalHeight;

        this.setX((int) (logicalX * scale));
        this.setY((int) (logicalY * scale));
        this.width = (int) (logicalWidth * scale);
        this.height = (int) (logicalHeight * scale);
    }

    public void setTasks(List<AbstractTask<?>> newTasks) {
        this.tasks = newTasks != null ? newTasks : new ArrayList<>();
        this.scrollAmount = 0;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (tasks == null || tasks.isEmpty() || !visible) return;

        Font font = Minecraft.getInstance().font;
        Component tooltipToRender = null;

        graphics.enableScissor(getX(), getY(), getX() + width, getY() + height);
        graphics.pose().pushPose();
        graphics.pose().translate(getX(), getY(), 0);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.pose().translate(0, -scrollAmount, 0);

        double relativeMouseX = (mouseX - getX()) / scale;
        double relativeMouseY = (mouseY - getY()) / scale;

        int currentY = 0;

        for (AbstractTask<?> task : tasks) {
            if (currentY + entryHeight > scrollAmount && currentY < scrollAmount + logicalHeight) {
                Component hoveredTooltip = renderEntry(graphics, font, task, 0, currentY, relativeMouseX, relativeMouseY);
                if (hoveredTooltip != null) {
                    tooltipToRender = hoveredTooltip;
                }
            }
            currentY += entryHeight;
        }

        graphics.pose().popPose();
        graphics.disableScissor();

        renderScrollbar(graphics);

        if (tooltipToRender != null) {
            graphics.renderTooltip(font, tooltipToRender, mouseX, mouseY);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Component renderEntry(GuiGraphics graphics, Font font, AbstractTask<?> task, int entryX, int entryY, double relativeMouseX, double relativeMouseY) {
        Component name = Component.literal("Unknown Task");
        int iconYOffset = 3;

        graphics.blit(TASK_FRAME, entryX, entryY + iconYOffset, 18, 18, 0, 0, 16, 16, 16, 16);

        ITaskRenderer renderer = TaskRendererRegistry.getRenderer(task);

        if (renderer != null) {
            name = renderer.getName(task);
            renderer.renderIcon(graphics, task, entryX + 1, entryY + iconYOffset + 1);
        } else {
            RenderSystem.enableBlend();
            graphics.blit(UNKNOWN_MOB, entryX + 1, entryY + iconYOffset + 1, 0, 0, 16, 16, 16, 16);
            RenderSystem.disableBlend();
        }

        String countTxt = showTotalCountOnly ? "x" + task.getGoal() : task.getProgress() + "/" + task.getGoal();
        int countWidth = font.width(countTxt);
        int maxTextWidth = this.logicalWidth - 25 - countWidth - 10;

        String textToDraw = name.getString();
        boolean isTruncated = false;

        if (font.width(textToDraw) > maxTextWidth) {
            textToDraw = font.plainSubstrByWidth(textToDraw, maxTextWidth - font.width("...")) + "...";
            isTruncated = true;
        }

        int textY = showTotalCountOnly ? (entryY + (entryHeight - font.lineHeight) / 2) : (entryY + 2);

        graphics.drawString(font, textToDraw, entryX + 22, textY, textColor, false);
        graphics.drawString(font, countTxt, this.logicalWidth - countWidth - 5, textY, countColor, false);

        if (!showTotalCountOnly) {
            int barX = entryX + 22;
            int barY = entryY + 14;
            int barWidth = this.logicalWidth - 25 - 5;

            float percent = task.getGoal() > 0 ? Math.min(1.0F, (float) task.getProgress() / task.getGoal()) : 0;
            int filledWidth = (int) (barWidth * percent);

            drawAdaptiveProgressBar(graphics, barX, barY, barWidth, barWidth, 0);

            if (filledWidth > 0) {
                drawAdaptiveProgressBar(graphics, barX, barY, barWidth, filledWidth, 5);
            }
        }

        double entryScreenY = entryY - scrollAmount;
        boolean isMouseOverEntry = relativeMouseX >= entryX && relativeMouseX <= entryX + logicalWidth &&
                relativeMouseY >= entryScreenY && relativeMouseY <= entryScreenY + entryHeight;

        if (isHovered() && isMouseOverEntry && isTruncated) {
            return name;
        }

        return null;
    }

    private void renderScrollbar(GuiGraphics graphics) {
        int contentHeight = tasks.size() * entryHeight;
        if (contentHeight <= logicalHeight) return;

        int scrollBarHeight = Math.max(10, (int) ((float) (height * height) / (contentHeight * scale)));
        int scrollBarX = getX() + width - 2;
        int scrollBarY = getY() + (int) ((height - scrollBarHeight) * (scrollAmount / (contentHeight - logicalHeight)));

        graphics.fill(scrollBarX, getY(), scrollBarX + 2, getY() + height, 0x44000000);
        graphics.fill(scrollBarX, scrollBarY, scrollBarX + 2, scrollBarY + scrollBarHeight, 0xFF8B4513);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmountArg) {
        if (isHovered()) {
            int contentHeight = tasks.size() * entryHeight;
            if (contentHeight <= logicalHeight) return false;

            double scrollSpeed = 15.0;
            this.scrollAmount = Mth.clamp(this.scrollAmount - scrollAmountArg * scrollSpeed, 0, contentHeight - logicalHeight);
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

    private void drawAdaptiveProgressBar(GuiGraphics graphics, int x, int y, int barWidth, int filledWidth, int vOffset) {
        if (filledWidth <= 0) return;

        int edgeSize = 2;
        int texWidth = 127;

        if (barWidth <= edgeSize * 2) {
            graphics.blit(PROGRESS_BAR, x, y, 0, vOffset, filledWidth, 5, 127, 10);
            return;
        }

        int leftWidth = Math.min(edgeSize, filledWidth);
        graphics.blit(PROGRESS_BAR, x, y, leftWidth, 5, 0, vOffset, leftWidth, 5, 127, 10);

        if (filledWidth > edgeSize) {
            int maxMiddle = barWidth - (edgeSize * 2);
            int currentMiddle = filledWidth - edgeSize;
            int drawMiddle = Math.min(currentMiddle, maxMiddle);

            if (drawMiddle > 0) {
                graphics.blit(PROGRESS_BAR, x + edgeSize, y, drawMiddle, 5, edgeSize, vOffset, drawMiddle, 5, 127, 10);
            }
        }

        if (filledWidth > barWidth - edgeSize) {
            int rightStart = barWidth - edgeSize;
            int drawRight = filledWidth - rightStart;

            graphics.blit(PROGRESS_BAR, x + rightStart, y, drawRight, 5, texWidth - edgeSize, vOffset, drawRight, 5, 127, 10);
        }
    }

    public static class Builder {
        private int x = 0, y = 0, width = 100, height = 100;
        private float scale = 1.0F;
        private List<AbstractTask<?>> tasks = new ArrayList<>();
        private boolean showTotalCountOnly = false;
        private int textColor = 0x3F3F3F;
        private int countColor = 0x3F3F3F;

        public Builder position(int x, int y) { this.x = x; this.y = y; return this; }
        public Builder size(int width, int height) { this.width = width; this.height = height; return this; }
        public Builder scale(float scale) { this.scale = scale; return this; }
        public Builder tasks(List<AbstractTask<?>> tasks) { if (tasks != null) this.tasks = tasks; return this; }
        public Builder showTotalOnly(boolean show) { this.showTotalCountOnly = show; return this; }
        public Builder colors(int textColor, int countColor) { this.textColor = textColor; this.countColor = countColor; return this; }

        public QuestTaskListWidget build() {
            return new QuestTaskListWidget(x, y, width, height, scale, tasks, showTotalCountOnly, textColor, countColor);
        }
    }
}