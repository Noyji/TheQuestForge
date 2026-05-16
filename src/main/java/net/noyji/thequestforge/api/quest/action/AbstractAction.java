package net.noyji.thequestforge.api.quest.action;

import net.minecraft.resources.ResourceLocation;

public abstract class AbstractAction {

    public abstract boolean handler(ActionContext context);

    public abstract ResourceLocation getLocation();
}
