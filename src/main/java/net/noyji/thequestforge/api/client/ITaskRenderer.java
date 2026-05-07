package net.noyji.thequestforge.api.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.noyji.thequestforge.api.quest.task.AbstractTask;

public interface ITaskRenderer<T extends AbstractTask<?>> {
    Component getName(T task);

    void renderIcon(GuiGraphics guiGraphics, T task, int x, int y);
}
