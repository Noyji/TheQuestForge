package net.noyji.thequestforge.client.gui.toast;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.noyji.thequestforge.common.items.TheQuestForgeItems;
import org.jetbrains.annotations.NotNull;

public class QuestToast implements Toast {

    private final Component title;
    private final Component subtitle;
    private final ItemStack icon;

    public QuestToast(String questName) {
        this.title = Component.translatable("toast.thequestforge.completed").withStyle(ChatFormatting.DARK_PURPLE);
        this.subtitle = Component.literal(questName).withStyle(ChatFormatting.DARK_GRAY);
        this.icon = new ItemStack(TheQuestForgeItems.QUEST_BOOK.get());
    }

    public QuestToast(Component questName) {
        this.title = Component.translatable("toast.thequestforge.completed").withStyle(ChatFormatting.DARK_PURPLE);
        this.subtitle = questName.copy().withStyle(ChatFormatting.DARK_GRAY);
        this.icon = new ItemStack(TheQuestForgeItems.QUEST_BOOK.get());
    }

    @Override
    public @NotNull Visibility render(@NotNull GuiGraphics guiGraphics, @NotNull ToastComponent toastComponent, long timeSinceLastVisible) {
        guiGraphics.blit(TEXTURE, 0, 0, 0, 32, this.width(), this.height());

        guiGraphics.drawString(toastComponent.getMinecraft().font, this.title, 30, 7, 0xFFFFFF, false);
        guiGraphics.drawString(toastComponent.getMinecraft().font, this.subtitle, 30, 18, 0xFFFFFF, false);

        guiGraphics.renderFakeItem(this.icon, 8, 8);

        return timeSinceLastVisible >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }
}
