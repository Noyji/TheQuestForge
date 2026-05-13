package net.noyji.thequestforge.api.quest.action;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public abstract class AbstractAction {

    public abstract boolean handler(ActionContext context);

    public abstract ResourceLocation getLocation();
}
