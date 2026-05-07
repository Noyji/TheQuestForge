package net.noyji.thequestforge.api.client.render.task;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.noyji.thequestforge.api.client.ITaskRenderer;
import net.noyji.thequestforge.api.quest.task.CollectTask;

public class CollectTaskRenderer implements ITaskRenderer<CollectTask> {
    @Override
    public Component getName(CollectTask task) {
        if (task.getItemStack() == null || task.getItemStack().isEmpty() ) {
            return Component.literal("Empty Item");
        }
        return task.getItemStack().getHoverName();
    }

    @Override
    public void renderIcon(GuiGraphics guiGraphics, CollectTask task, int x, int y) {
        if (task.getItemStack() != null  && !task.getItemStack().isEmpty()) {
            guiGraphics.renderItem(task.getItemStack(), x, y);
        }
    }
}
