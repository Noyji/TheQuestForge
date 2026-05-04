package net.noyji.thequestforge.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TypewriterTextWidget extends AbstractWidget {

    private final Font font;
    private final List<FormattedCharSequence> lines = new ArrayList<>();
    private final List<Integer> lineLengths = new ArrayList<>();
    private String flatText = "";

    private final int textColor;
    private final boolean dropShadow;

    private float displayedChars = 0;
    private final float charsPerTick;
    private boolean isFinished = false;
    private int totalChars = 0;

    private float pauseTimer = 0.0f;
    private final SoundEvent typingSound;
    private final int soundInterval;
    private final float voicePitch;
    private final Runnable onComplete;

    private final RandomSource random = RandomSource.create();

    public TypewriterTextWidget(int x, int y, int width, Component text, Font font,
                                float charsPerSecond, int textColor, boolean dropShadow,
                                SoundEvent typingSound, int soundInterval, float voicePitch,
                                Runnable onComplete) {
        super(x, y, width, 0, text);
        this.font = font;
        this.textColor = textColor;
        this.dropShadow = dropShadow;
        this.charsPerTick = charsPerSecond / 20.0f;

        this.typingSound = typingSound;
        this.soundInterval = soundInterval;
        this.voicePitch = voicePitch;
        this.onComplete = onComplete;

        wrapText(text, width);
        this.height = lines.size() * font.lineHeight;
    }

    private void wrapText(Component text, int maxWidth) {
        List<FormattedCharSequence> splitLines = font.split(text, maxWidth);
        StringBuilder flatBuilder = new StringBuilder();

        for (FormattedCharSequence seq : splitLines) {
            lines.add(seq);
            int[] len = {0};

            seq.accept((index, style, codePoint) -> {
                len[0]++;
                flatBuilder.appendCodePoint(codePoint);
                return true;
            });

            lineLengths.add(len[0]);
        }

        this.flatText = flatBuilder.toString();
        this.totalChars = this.flatText.length();
    }

    public void skipAnimation() {
        if (!isFinished) {
            this.displayedChars = totalChars;
            this.isFinished = true;
            this.pauseTimer = 0;
            if (onComplete != null) onComplete.run();
        }
    }

    public void updateBounds(int newX, int newY, int newWidth) {
        this.setX(newX);
        this.setY(newY);

        if (this.width != newWidth) {
            this.setWidth(newWidth);

            this.lines.clear();
            this.lineLengths.clear();

            this.wrapText(this.getMessage(), newWidth);

            this.height = this.lines.size() * this.font.lineHeight;
        }
    }

    public boolean isFinished() {
        return isFinished;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float delta = Minecraft.getInstance().getDeltaFrameTime();

        if (!isFinished) {
            if (pauseTimer > 0) {
                pauseTimer -= delta;
            } else {
                int oldChars = (int) displayedChars;
                displayedChars += charsPerTick * delta;
                int newChars = (int) displayedChars;

                if (newChars > oldChars && newChars <= totalChars) {
                    char revealedChar = flatText.charAt(newChars - 1);

                    if (revealedChar == '.' || revealedChar == '!' || revealedChar == '?') {
                        pauseTimer = 8.0f;
                    } else if (revealedChar == ',') {
                        pauseTimer = 4.0f;
                    }

                    if (typingSound != null && revealedChar != ' ' && (newChars % soundInterval == 0)) {
                        float pitchVariance = voicePitch + (random.nextFloat() * 0.2f - 0.1f);
                        Minecraft.getInstance().getSoundManager().play(
                                SimpleSoundInstance.forUI(typingSound, pitchVariance, 0.5F)
                        );
                    }
                }

                if (displayedChars >= totalChars) {
                    displayedChars = totalChars;
                    isFinished = true;
                    if (onComplete != null) onComplete.run();
                }
            }
        }

        int charsLeftToDraw = (int) displayedChars;
        int currentY = this.getY();

        for (int i = 0; i < lines.size(); i++) {
            if (charsLeftToDraw <= 0) break;

            FormattedCharSequence line = lines.get(i);
            int lineLen = lineLengths.get(i);

            if (lineLen <= charsLeftToDraw) {
                graphics.drawString(font, line, this.getX(), currentY, textColor, dropShadow);
                charsLeftToDraw -= lineLen;
            } else {
                int finalCharsToDraw = charsLeftToDraw;
                FormattedCharSequence partialLine = sink -> {
                    int[] count = {0};
                    return line.accept((index, style, codePoint) -> {
                        if (count[0] >= finalCharsToDraw) return false;
                        count[0]++;
                        return sink.accept(index, style, codePoint);
                    });
                };

                graphics.drawString(font, partialLine, this.getX(), currentY, textColor, dropShadow);
                charsLeftToDraw = 0;
            }

            currentY += font.lineHeight;
        }
    }

    public void setText(Component newText) {
        this.setMessage(newText);

        this.lines.clear();
        this.lineLengths.clear();
        this.flatText = "";

        this.displayedChars = 0;
        this.isFinished = false;
        this.pauseTimer = 0.0f;
        this.totalChars = 0;

        this.wrapText(newText, this.width);

        this.height = this.lines.size() * this.font.lineHeight;
    }

    public void setText(String newText) {
        this.setText(Component.literal(newText.replace('&', '\u00A7')));
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {}

    public static class Builder {
        private int x = 0;
        private int y = 0;
        private int width = 200;
        private Component text = Component.empty();
        private Font font = Minecraft.getInstance().font;
        private float charsPerSecond = 40.0f;
        private int textColor = 0xFFFFFF;
        private boolean dropShadow = true;
        private SoundEvent typingSound = null;
        private int soundInterval = 2;
        private float voicePitch = 1.0f;
        private Runnable onComplete = null;

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder text(Component text) {
            this.text = text;
            return this;
        }

        public Builder text(String text){
            this.text = Component.literal(text.replace('&', '\u00A7'));
            return this;
        }

        public Builder font(Font font) {
            this.font = font;
            return this;
        }

        public Builder speed(float charsPerSecond) {
            this.charsPerSecond = charsPerSecond;
            return this;
        }

        public Builder color(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder dropShadow(boolean dropShadow) {
            this.dropShadow = dropShadow;
            return this;
        }

        public Builder sound(SoundEvent typingSound) {
            this.typingSound = typingSound;
            return this;
        }

        public Builder soundSettings(int interval, float pitch) {
            this.soundInterval = interval;
            this.voicePitch = pitch;
            return this;
        }

        public Builder onComplete(Runnable onComplete) {
            this.onComplete = onComplete;
            return this;
        }

        public TypewriterTextWidget build() {
            return new TypewriterTextWidget(
                    x, y, width, text, font, charsPerSecond,
                    textColor, dropShadow, typingSound,
                    soundInterval, voicePitch, onComplete
            );
        }
    }
}