package net.noyji.thequestforge.client.gui.book.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.common.util.Util;
import net.noyji.thequestforge.config.ServerConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class QuestRewardGridWidget extends AbstractWidget {

    private static final ResourceLocation SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TheQuestForge.MODID, "textures/gui/quest_book/components/task_frame.png");

    private List<ItemStack> rewards;
    private int xp;
    private int currency;

    private float scale = 1.0F;
    private int logicalWidth;
    private int logicalHeight;

    private final int slotSize = 18;
    private final int spacing = 2;

    private final int xpColor;
    private final int currencyColor;

    private QuestRewardGridWidget(int x, int y, int width, int height, float scale, List<ItemStack> rewards, int xp, int currency, int xpColor, int currencyColor) {
        super(x, y, width, height, Component.empty());
        this.scale = scale;
        this.logicalWidth = width;
        this.logicalHeight = height;
        this.rewards = rewards != null ? rewards : new ArrayList<>();
        this.xp = xp;
        this.currency = currency;
        this.xpColor = xpColor;
        this.currencyColor = currencyColor;
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

    public void setRewards(List<ItemStack> newRewards, int newXp, int newCurrency) {
        this.rewards = newRewards != null ? newRewards : new ArrayList<>();
        this.xp = newXp;
        this.currency = newCurrency;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;

        Font font = Minecraft.getInstance().font;
        ItemStack tooltipStack = null;

        graphics.pose().pushPose();
        graphics.pose().translate(getX(), getY(), 0);
        graphics.pose().scale(scale, scale, 1.0F);

        double relativeMouseX = (mouseX - getX()) / scale;
        double relativeMouseY = (mouseY - getY()) / scale;

        int columns = logicalWidth / (slotSize + spacing);
        if (columns < 1) columns = 1;

        int lastY = 0;

        if (rewards != null && !rewards.isEmpty()) {
            int index = 0;
            for (ItemStack stack : rewards) {
                if (stack.isEmpty()) continue;

                int col = index % columns;
                int row = index / columns;

                int drawX = col * (slotSize + spacing);
                int drawY = row * (slotSize + spacing);

                graphics.blit(SLOT_TEXTURE, drawX, drawY, 0, 0, 18, 18, 18, 18);

                graphics.renderItem(stack, drawX + 1, drawY + 1);
                graphics.renderItemDecorations(font, stack, drawX + 1, drawY + 1);

                if (relativeMouseX >= drawX && relativeMouseX <= drawX + 18 && relativeMouseY >= drawY && relativeMouseY <= drawY + 18) {
                    tooltipStack = stack;
                }

                lastY = drawY + slotSize + spacing;
                index++;
            }
        }

        int statsY = (rewards == null || rewards.isEmpty() || lastY == 0) ? 0 : lastY + 5;
        int currentX = 0;

        if (xp > 0) {
            String xpText = "+" + xp + " XP";
            graphics.drawString(font, xpText, currentX, statsY + 5, this.xpColor, false);
            currentX += font.width(xpText) + 10;
        }

        if (currency > 0) {
            ResourceLocation currencyId = ResourceLocation.tryParse(ServerConfig.CURRENCY_ID.get());

            if (currencyId != null) {
                Item currencyItem = Util.getItem(currencyId);
                ItemStack currencyStack = new ItemStack(currencyItem);

                graphics.renderItem(currencyStack, currentX, statsY);

                String amountText = String.valueOf(currency);
                graphics.drawString(font, amountText, currentX + 18, statsY + 5, this.currencyColor, false);

                if (relativeMouseX >= currentX && relativeMouseX <= currentX + 18 && relativeMouseY >= statsY && relativeMouseY <= statsY + 18) {
                    tooltipStack = currencyStack;
                }
            }
        }

        graphics.pose().popPose();

        if (tooltipStack != null) {
            graphics.renderTooltip(font, tooltipStack, mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

    public static class Builder {
        private int x = 0, y = 0, width = 100, height = 100;
        private float scale = 1.0F;
        private List<ItemStack> rewards = new ArrayList<>();
        private int xp = 0;
        private int currency = 0;
        private int xpColor = 0x3F3F3F;
        private int currencyColor = 0x3F3F3F;

        public Builder position(int x, int y) { this.x = x; this.y = y; return this; }
        public Builder size(int width, int height) { this.width = width; this.height = height; return this; }
        public Builder scale(float scale) { this.scale = scale; return this; }

        public Builder rewards(List<ItemStack> rewards, int xp, int currency) {
            if (rewards != null) this.rewards = rewards;
            this.xp = xp;
            this.currency = currency;
            return this;
        }

        public Builder colors(int xpColor, int currencyColor) {
            this.xpColor = xpColor;
            this.currencyColor = currencyColor;
            return this;
        }

        public QuestRewardGridWidget build() {
            return new QuestRewardGridWidget(x, y, width, height, scale, rewards, xp, currency, xpColor, currencyColor);
        }
    }
}