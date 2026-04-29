package net.noyji.thequestforge.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnimatedButton extends AbstractButton {
    private final ResourceLocation texture;
    private final int textureWidth;
    private final int textureHeight;
    private final int u;
    private final int v;

    private final float scale;
    private final int originalWidth;
    private final int originalHeight;

    private final AnimationDirection direction;
    private final float animationDistance;
    private final float animationSpeed;
    private final EasingType easingType;

    private final SoundEvent hoverSound;
    private final SoundEvent clickSound;

    private float animationProgress = 0.0f;
    private boolean isAnimatingIn = false;
    private boolean isAnimatingOut = false;

    private boolean wasHovered = false;
    private long unhoverTime = 0;
    private final long reverseDelayMs;

    private final OnPress onPress;

    public AnimatedButton(int x, int y, int width, int height,
                          ResourceLocation texture, int textureWidth, int textureHeight,
                          int u, int v, float scale,
                          AnimationDirection direction, float animationDistance,
                          float animationSpeed, EasingType easingType,
                          OnPress onPress, SoundEvent hoverSound, SoundEvent clickSound,
                          long reverseDelayMs) {
        super(x, y, (int)(width * scale), (int)(height * scale), Component.empty());

        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.u = u;
        this.v = v;

        this.scale = scale;
        this.originalWidth = width;
        this.originalHeight = height;

        this.direction = direction;
        this.animationDistance = animationDistance;
        this.animationSpeed = Mth.clamp(animationSpeed, 0.01f, 10.0f);
        this.easingType = easingType;

        this.onPress = onPress;
        this.hoverSound = hoverSound;
        this.clickSound = clickSound;
        this.reverseDelayMs = reverseDelayMs;
    }

    public void startAnimation() {
        isAnimatingIn = true;
        isAnimatingOut = false;
    }

    public void startReverseAnimation() {
        isAnimatingIn = false;
        isAnimatingOut = true;
    }

    public void resetAnimation() {
        isAnimatingIn = false;
        isAnimatingOut = false;
        animationProgress = 0.0f;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isAnimatingIn) {
            animationProgress += animationSpeed * partialTick * 0.05f;
            if (animationProgress >= 1.0f) {
                animationProgress = 1.0f;
                isAnimatingIn = false;
            }
        } else if (isAnimatingOut) {
            animationProgress -= animationSpeed * partialTick * 0.05f;
            if (animationProgress <= 0.0f) {
                animationProgress = 0.0f;
                isAnimatingOut = false;
            }
        }

        boolean currentHovered = this.isHovered();

        if (currentHovered) {
            unhoverTime = 0;
            if (!wasHovered) {
                playHoverSound();
                startAnimation();
            }
        } else {
            if (wasHovered) {
                unhoverTime = System.currentTimeMillis();
            } else if (unhoverTime > 0 && System.currentTimeMillis() - unhoverTime >= reverseDelayMs) {
                startReverseAnimation();
                unhoverTime = 0;
            }
        }

        this.wasHovered = currentHovered;

        float easedProgress = applyEasing(animationProgress);
        float offsetX = 0;
        float offsetY = 0;
        float scaledDistance = animationDistance * scale;

        switch (direction) {
            case RIGHT -> offsetX = easedProgress * scaledDistance;
            case LEFT -> offsetX = -easedProgress * scaledDistance;
            case DOWN -> offsetY = easedProgress * scaledDistance;
            case UP -> offsetY = -easedProgress * scaledDistance;
            case DIAGONAL_RIGHT_DOWN -> {
                offsetX = easedProgress * scaledDistance;
                offsetY = easedProgress * scaledDistance;
            }
            case DIAGONAL_LEFT_DOWN -> {
                offsetX = -easedProgress * scaledDistance;
                offsetY = easedProgress * scaledDistance;
            }
            case DIAGONAL_RIGHT_UP -> {
                offsetX = easedProgress * scaledDistance;
                offsetY = -easedProgress * scaledDistance;
            }
            case DIAGONAL_LEFT_UP -> {
                offsetX = -easedProgress * scaledDistance;
                offsetY = -easedProgress * scaledDistance;
            }
        }

        graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        graphics.blit(texture,
                (int) (getX() + offsetX),
                (int) (getY() + offsetY),
                (int) (originalWidth * scale),
                (int) (originalHeight * scale),
                u, v,
                originalWidth, originalHeight,
                textureWidth, textureHeight
        );
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void playHoverSound() {
        if (hoverSound != null) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(hoverSound, 1.0F, 1.0F)
            );
        }
    }

    @Override
    public void onPress() {
        if (clickSound != null) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(clickSound, 1.0F, 1.0F)
            );
        }
        if (onPress != null) {
            onPress.onPress(this);
        }
    }

    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    private float applyEasing(float progress) {
        return switch (easingType) {
            case LINEAR -> progress;
            case EASE_IN -> progress * progress;
            case EASE_OUT -> progress * (2.0f - progress);
            case EASE_IN_OUT -> progress < 0.5f ?
                    2.0f * progress * progress :
                    -1.0f + (4.0f - 2.0f * progress) * progress;
            case EASE_IN_CUBIC -> progress * progress * progress;
            case EASE_OUT_CUBIC -> {
                float t = progress - 1.0f;
                yield t * t * t + 1.0f;
            }
            case EASE_IN_OUT_CUBIC -> progress < 0.5f ?
                    4.0f * progress * progress * progress :
                    1.0f - (float) Math.pow(-2.0f * progress + 2.0f, 3.0f) / 2.0f;
            case EASE_IN_BACK -> {
                float c1 = 1.70158f;
                float c3 = c1 + 1.0f;
                yield c3 * progress * progress * progress - c1 * progress * progress;
            }
            case EASE_OUT_BACK -> {
                float c1 = 1.70158f;
                float c3 = c1 + 1.0f;
                float t = progress - 1.0f;
                yield 1.0f + c3 * t * t * t + c1 * t * t;
            }
            case BOUNCE_OUT -> {
                float n1 = 7.5625f;
                float d1 = 2.75f;
                if (progress < 1.0f / d1) {
                    yield n1 * progress * progress;
                } else if (progress < 2.0f / d1) {
                    float t = progress - 1.5f / d1;
                    yield n1 * t * t + 0.75f;
                } else if (progress < 2.5f / d1) {
                    float t = progress - 2.25f / d1;
                    yield n1 * t * t + 0.9375f;
                } else {
                    float t = progress - 2.625f / d1;
                    yield n1 * t * t + 0.984375f;
                }
            }
        };
    }

    public enum AnimationDirection {
        RIGHT, LEFT, UP, DOWN,
        DIAGONAL_RIGHT_DOWN, DIAGONAL_LEFT_DOWN,
        DIAGONAL_RIGHT_UP, DIAGONAL_LEFT_UP
    }

    public enum EasingType {
        LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT,
        EASE_IN_CUBIC, EASE_OUT_CUBIC, EASE_IN_OUT_CUBIC,
        EASE_IN_BACK, EASE_OUT_BACK, BOUNCE_OUT
    }

    @FunctionalInterface
    public interface OnPress {
        void onPress(AnimatedButton button);
    }

    public static class Builder {
        private int x, y, width, height;
        private ResourceLocation texture;
        private int textureWidth = 256;
        private int textureHeight = 256;
        private int u = 0, v = 0;
        private float scale = 1.0f;
        private AnimationDirection direction = AnimationDirection.RIGHT;
        private float distance = 10.0f;
        private float speed = 0.2f;
        private EasingType easing = EasingType.EASE_OUT;
        private OnPress onPress;
        private SoundEvent hoverSound = null;
        private SoundEvent clickSound = null;
        private long reverseDelayMs = 170L;

        @Nullable
        private Tooltip tooltip = null;

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public Builder texture(ResourceLocation texture) {
            this.texture = texture;
            return this;
        }

        public Builder textureSize(int width, int height) {
            this.textureWidth = width;
            this.textureHeight = height;
            return this;
        }

        public Builder uv(int u, int v) {
            this.u = u;
            this.v = v;
            return this;
        }

        public Builder animation(AnimationDirection direction, float distance, float speed) {
            this.direction = direction;
            this.distance = distance;
            this.speed = speed;
            return this;
        }

        public Builder easing(EasingType easing) {
            this.easing = easing;
            return this;
        }

        public Builder onPress(OnPress onPress) {
            this.onPress = onPress;
            return this;
        }

        public Builder hoverSound(SoundEvent sound) {
            this.hoverSound = sound;
            return this;
        }

        public Builder clickSound(SoundEvent sound) {
            this.clickSound = sound;
            return this;
        }

        public Builder reverseDelay(long ms) {
            this.reverseDelayMs = ms;
            return this;
        }

        public Builder tooltip(Component message) {
            this.tooltip = Tooltip.create(message);
            return this;
        }

        public AnimatedButton build() {
            AnimatedButton button = new AnimatedButton(x, y, width, height, texture,
                    textureWidth, textureHeight, u, v, scale,
                    direction, distance, speed, easing,
                    onPress, hoverSound, clickSound, reverseDelayMs);

            if (this.tooltip != null) {
                button.setTooltip(this.tooltip);
            }

            return button;
        }
    }
}