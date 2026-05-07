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
            ResourceLocation.fromNamespaceAndPath(TheQuestForge.MODID, "textures/gui/quest_book_book/icons/unknown_mob.png");

    private List<AbstractTask<?>> tasks;
    private double scrollAmount = 0;
    private final int entryHeight = 22;

    private final boolean showTotalCountOnly;
    private final int textColor;
    private final int countColor;

    private QuestTaskListWidget(int x, int y, int width, int height, List<AbstractTask<?>> tasks, boolean showTotalCountOnly, int textColor, int countColor) {
        super(x, y, width, height, Component.empty());
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.showTotalCountOnly = showTotalCountOnly;
        this.textColor = textColor;
        this.countColor = countColor;
    }

    public void updateBounds(int newX, int newY, int newWidth, int newHeight) {
        this.setX(newX);
        this.setY(newY);
        this.width = newWidth;
        this.height = newHeight;
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

        // Включаем ножницы для отсечения контента вне зоны виджета
        graphics.enableScissor(getX(), getY(), getX() + width, getY() + height);
        graphics.pose().pushPose();
        graphics.pose().translate(0, -scrollAmount, 0);

        int currentY = getY();
        for (AbstractTask<?> task : tasks) {
            // Оптимизация: рисуем только видимые элементы
            if (currentY + entryHeight > scrollAmount + getY() && currentY < scrollAmount + getY() + height) {
                Component hoveredTooltip = renderEntry(graphics, font, task, getX(), currentY, mouseX, mouseY);
                if (hoveredTooltip != null) {
                    tooltipToRender = hoveredTooltip;
                }
            }
            currentY += entryHeight;
        }

        graphics.pose().popPose();
        graphics.disableScissor();

        renderScrollbar(graphics);

        // Рисуем тултип поверх всего
        if (tooltipToRender != null) {
            graphics.renderTooltip(font, tooltipToRender, mouseX, mouseY);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Component renderEntry(GuiGraphics graphics, Font font, AbstractTask<?> task, int entryX, int entryY, int mouseX, int mouseY) {
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
        int maxTextWidth = this.width - 25 - countWidth - 10;

        String textToDraw = name.getString();
        boolean isTruncated = false;

        if (font.width(textToDraw) > maxTextWidth) {
            textToDraw = font.plainSubstrByWidth(textToDraw, maxTextWidth - font.width("...")) + "...";
            isTruncated = true;
        }

        // Вертикальное выравнивание текста
        int textY = showTotalCountOnly ? (entryY + (entryHeight - font.lineHeight) / 2) : (entryY + 2);

        graphics.drawString(font, textToDraw, entryX + 22, textY, textColor, false);
        graphics.drawString(font, countTxt, getX() + this.width - countWidth - 5, textY, countColor, false);

        // --- Прогресс Бар ---
        if (!showTotalCountOnly) {
            int barX = entryX + 22;
            int barY = entryY + 14;
            int barWidth = this.width - 25 - 5; // Динамическая ширина

            float percent = task.getGoal() > 0 ? Math.min(1.0F, (float) task.getProgress() / task.getGoal()) : 0;
            int filledWidth = (int) (barWidth * percent);

            graphics.blit(PROGRESS_BAR, barX, barY, 0, 0, barWidth, 5, 127, 10);

            if (filledWidth > 0) {
                graphics.blit(PROGRESS_BAR, barX, barY, 0, 5, filledWidth, 5, 127, 10);
            }
        }

        double entryScreenY = entryY - scrollAmount;
        boolean isMouseOverWidget = isMouseOver(mouseX, mouseY);
        boolean isMouseOverEntry = mouseX >= entryX && mouseX <= entryX + width &&
                mouseY >= entryScreenY && mouseY <= entryScreenY + entryHeight;

        if (isMouseOverWidget && isMouseOverEntry && isTruncated) {
            return name;
        }

        return null;
    }

    private void renderScrollbar(GuiGraphics graphics) {
        int contentHeight = tasks.size() * entryHeight;
        if (contentHeight <= height) return;

        int scrollBarHeight = Math.max(10, (int) ((float) (height * height) / contentHeight));
        int scrollBarX = getX() + width + 2;
        int scrollBarY = getY() + (int) ((height - scrollBarHeight) * (scrollAmount / (contentHeight - height)));

        graphics.fill(scrollBarX, getY(), scrollBarX + 2, getY() + height, 0x44000000);
        graphics.fill(scrollBarX, scrollBarY, scrollBarX + 2, scrollBarY + scrollBarHeight, 0xFF8B4513);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if (isMouseOver(mouseX, mouseY)) {
            int contentHeight = tasks.size() * entryHeight;
            if (contentHeight <= height) return false;

            double scrollSpeed = 15.0;
            this.scrollAmount = Mth.clamp(this.scrollAmount - scrollAmount * scrollSpeed, 0, contentHeight - height);
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
    }

    public static class Builder {
        private int x = 0, y = 0, width = 100, height = 100;
        private List<AbstractTask<?>> tasks = new ArrayList<>();
        private boolean showTotalCountOnly = false;
        private int textColor = 0x3F3F3F;
        private int countColor = 0x3F3F3F;

        public Builder position(int x, int y) { this.x = x; this.y = y; return this; }
        public Builder size(int width, int height) { this.width = width; this.height = height; return this; }
        public Builder tasks(List<AbstractTask<?>> tasks) { if (tasks != null) this.tasks = tasks; return this; }
        public Builder showTotalOnly(boolean show) { this.showTotalCountOnly = show; return this; }
        public Builder colors(int textColor, int countColor) { this.textColor = textColor; this.countColor = countColor; return this; }

        public QuestTaskListWidget build() {
            return new QuestTaskListWidget(x, y, width, height, tasks, showTotalCountOnly, textColor, countColor);
        }
    }
}