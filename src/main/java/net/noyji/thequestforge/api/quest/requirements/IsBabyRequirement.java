package net.noyji.thequestforge.api.quest.requirements;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.registry.RequirementRegistry;

public class IsBabyRequirement extends AbstractRequirement{
    //thequestforge:is_baby:(true/false)
    @Override
    public boolean check(RequirementContext context) {
        if (!(context.getEntity() instanceof LivingEntity livingEntity)) return false;

        RequirementRegistry.requirementsLogger(getLocation(), livingEntity.isBaby() == context.getBooleanValue());

        return livingEntity.isBaby() == context.getBooleanValue();
    }

    @Override
    public ResourceLocation getLocation() {
        return TheQuestForge.id("is_baby");
    }
}
