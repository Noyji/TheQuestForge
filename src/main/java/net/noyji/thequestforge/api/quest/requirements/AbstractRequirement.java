package net.noyji.thequestforge.api.quest.requirements;

import net.minecraft.resources.ResourceLocation;

public abstract class AbstractRequirement {

    public abstract boolean check(RequirementContext context);

    public abstract ResourceLocation getLocation();
}
