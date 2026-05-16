package net.noyji.thequestforge.api.quest.requirements;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.registry.RequirementRegistry;

public class DimensionIsNotRequirement extends AbstractRequirement{
    //thequestforge:dimension_is_not:twilightforest:twilight_fores
    @Override
    public boolean check(RequirementContext context) {
        ResourceKey<Level> targetDimension = ResourceKey.create(Registries.DIMENSION, context.getResourceLocationValue());

        RequirementRegistry.requirementsLogger(getLocation(), context.getEntity().level().dimension() != targetDimension);

        return (context.getEntity().level().dimension() != targetDimension);
    }

    @Override
    public ResourceLocation getLocation() {
        return TheQuestForge.id("dimension_is_not");
    }
}
