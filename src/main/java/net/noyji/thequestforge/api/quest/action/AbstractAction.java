package net.noyji.thequestforge.api.quest.action;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public abstract class AbstractAction {

    public abstract boolean handler(ServerPlayer serverPlayer);

    public abstract ResourceLocation getLocation();
}
