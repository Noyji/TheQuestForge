package net.noyji.thequestforge.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.noyji.thequestforge.data.template.components.TemplateDialogButton;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class DialogOptionSelector extends AbstractWidget {

    public enum Alignment {
        LEFT, CENTER, RIGHT
    }

    private final Font font;

    private List<TemplateDialogButton> options = Collections.emptyList();
    private List<Integer> textIndices = Collections.emptyList();

    private final List<List<FormattedCharSequence>> splitLines = new ArrayList<>();
    private final List<Integer> optionHeights = new ArrayList<>();
    private final List<Integer> optionYOffsets = new ArrayList<>();

    private int selectedIndex = 0;
    private float scrollPosition = 0f;
    private final int spacingBetweenOptions = 10;

    private Alignment alignment = Alignment.CENTER;
    private int colorActive = 0xFFFFFFFF;
    private int colorInactive = 0x888888;
    private String languageKey;

    private Runnable onSelectSound = null;
    private Consumer<TemplateDialogButton> onSelectAction = null;

    public DialogOptionSelector(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.font = Minecraft.getInstance().font;
        this.languageKey = Minecraft.getInstance().options.languageCode;
    }

    public static Builder builder(int x, int y, int width, int height) {
        return new Builder(x, y, width, height);
    }

    public int getSelectedIndex(){
        return this.selectedIndex;
    }

    public void setOptions(List<TemplateDialogButton> options, List<Integer> textIndices) {
        this.options = options != null ? options : Collections.emptyList();
        this.textIndices = textIndices != null ? textIndices : Collections.emptyList();
        this.selectedIndex = 0;
        this.scrollPosition = 0f;
        recalculateLayout();
    }

    public void setLanguageKey(String key) {
        this.languageKey = key;
        recalculateLayout();
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    public void setColors(int active, int inactive) {
        this.colorActive = active;
        this.colorInactive = inactive;
    }

    public void setOnSelectAction(Consumer<TemplateDialogButton> action) {
        this.onSelectAction = action;
    }

    public void setOnSelectSound(Runnable sound) {
        this.onSelectSound = sound;
    }

    public void updateBounds(int newX, int newY, int newWidth, int newHeight) {
        this.setX(newX);
        this.setY(newY);
        this.setWidth(newWidth);
        this.height = newHeight;
        recalculateLayout();
    }

    private void recalculateLayout() {
        splitLines.clear();
        optionHeights.clear();
        optionYOffsets.clear();

        int currentY = 0;
        int maxWidth = Math.max(10, this.width - 20);

        for (int i = 0; i < options.size(); i++) {
            int currentTextIndex = (textIndices.size() > i) ? textIndices.get(i) : 0;
            String rawText = options.get(i).getTranslateText(languageKey, currentTextIndex);
            Component textComp = Component.literal(rawText.replace('&', '\u00A7'));

            List<FormattedCharSequence> lines = font.split(textComp, maxWidth);
            splitLines.add(lines);

            int blockHeight = lines.size() * font.lineHeight;
            optionHeights.add(blockHeight);
            optionYOffsets.add(currentY);

            currentY += blockHeight + spacingBetweenOptions;
        }
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible || options.isEmpty() || splitLines.isEmpty()) return;

        scrollPosition = Mth.lerp(0.2f * partialTick + 0.1f, scrollPosition, (float) selectedIndex);

        int centerY = getY() + height / 2;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float currentScrollY = 0;
        int floorIdx = Mth.floor(scrollPosition);
        if (floorIdx >= 0 && floorIdx < optionYOffsets.size()) {
            float frac = scrollPosition - floorIdx;
            float y1 = optionYOffsets.get(floorIdx);
            float y2 = (floorIdx + 1 < optionYOffsets.size()) ? optionYOffsets.get(floorIdx + 1) : y1;
            currentScrollY = Mth.lerp(frac, y1, y2);
        } else if (floorIdx < 0) {
            currentScrollY = optionYOffsets.get(0);
        } else {
            currentScrollY = optionYOffsets.get(optionYOffsets.size() - 1);
        }

        for (int i = 0; i < options.size(); i++) {
            float distance = Math.abs(i - scrollPosition);

            if (distance > 4.0f) continue;

            float scale = Math.max(0.8f, 1.1f - distance * 0.15f);
            float alpha = Math.max(0.1f, 1f - distance * 0.4f);

            int color = leapColor(colorInactive, colorActive, 1f - Math.min(1f, distance * 0.5f));
            int finalColor = applyAlpha(color, alpha);

            float offsetY = optionYOffsets.get(i) - currentScrollY;

            graphics.pose().pushPose();

            float pivotX = switch (alignment) {
                case CENTER -> getX() + getWidth() / 2f;
                case LEFT -> getX() + 10f;
                case RIGHT -> getX() + getWidth() - 10f;
            };

            graphics.pose().translate(pivotX, centerY + offsetY, 0);
            graphics.pose().scale(scale, scale, 1f);

            List<FormattedCharSequence> lines = splitLines.get(i);
            int blockHeight = optionHeights.get(i);

            int lineY = -blockHeight / 2;

            for (FormattedCharSequence line : lines) {
                int lineWidth = font.width(line);
                float drawX = switch (alignment) {
                    case CENTER -> -lineWidth / 2f;
                    case LEFT -> 0;
                    case RIGHT -> -lineWidth;
                };

                graphics.drawString(font, line, (int) drawX, lineY, finalColor, true);
                lineY += font.lineHeight;
            }

            graphics.pose().popPose();
        }

        RenderSystem.disableBlend();
    }

    public void moveUp() {
        if (options.isEmpty()) return;
        int prev = selectedIndex;
        selectedIndex = Math.max(0, selectedIndex - 1);
        if (prev != selectedIndex && onSelectSound != null) onSelectSound.run();
    }

    public void moveDown() {
        if (options.isEmpty()) return;
        int prev = selectedIndex;
        selectedIndex = Math.min(options.size() - 1, selectedIndex + 1);
        if (prev != selectedIndex && onSelectSound != null) onSelectSound.run();
    }

    public void confirm() {
        if (options.isEmpty()) return;
        if (onSelectAction != null) {
            onSelectAction.accept(options.get(selectedIndex));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (!visible) return false;
        if (scrollDelta > 0) moveUp();
        else if (scrollDelta < 0) moveDown();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!visible) return false;
        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_W) {
            moveUp();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_S) {
            moveDown();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            confirm();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (visible && isMouseOver(mouseX, mouseY) && button == 0) {
            confirm();
            return true;
        }
        return false;
    }

    public void setVisible(boolean value){
        this.visible = value;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

    private static int leapColor(int colorA, int colorB, float t) {
        t = Mth.clamp(t, 0f, 1f);
        int r1 = (colorA >> 16) & 0xFF;
        int g1 = (colorA >> 8) & 0xFF;
        int b1 = colorA & 0xFF;
        int r2 = (colorB >> 16) & 0xFF;
        int g2 = (colorB >> 8) & 0xFF;
        int b2 = colorB & 0xFF;
        return ((int) (r1 + (r2 - r1) * t) << 16) |
                ((int) (g1 + (g2 - g1) * t) << 8) |
                (int) (b1 + (b2 - b1) * t);
    }

    private static int applyAlpha(int color, float alpha) {
        int a = (int) (Mth.clamp(alpha, 0f, 1f) * 255);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    public static class Builder {
        private final int x, y, width, height;
        private List<TemplateDialogButton> options = Collections.emptyList();
        private List<Integer> textIndices = Collections.emptyList();

        private Alignment alignment = Alignment.CENTER;
        private Consumer<TemplateDialogButton> action;
        private Runnable sound;

        public Builder(int x, int y, int width, int height) {
            this.x = x; this.y = y; this.width = width; this.height = height;
        }

        public Builder options(List<TemplateDialogButton> options, List<Integer> textIndices) {
            this.options = options;
            this.textIndices = textIndices;
            return this;
        }

        public Builder align(Alignment align) { this.alignment = align; return this; }
        public Builder action(Consumer<TemplateDialogButton> action) { this.action = action; return this; }
        public Builder sound(Runnable sound) { this.sound = sound; return this; }

        public DialogOptionSelector build() {
            DialogOptionSelector s = new DialogOptionSelector(x, y, width, height);
            s.setOptions(options, textIndices);
            s.setAlignment(alignment);
            s.setOnSelectAction(action);
            s.setOnSelectSound(sound);
            return s;
        }
    }
}