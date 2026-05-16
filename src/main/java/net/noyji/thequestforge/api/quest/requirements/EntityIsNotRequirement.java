package net.noyji.thequestforge.api.quest.requirements;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.registry.RequirementRegistry;
import net.noyji.thequestforge.common.util.Util;

public class EntityIsNotRequirement extends AbstractRequirement {
    //thequestforge:entity_is_not:minecraft:pig
    @Override
    public boolean check(RequirementContext context) {
        if (context.getEntity() == null) return false;

        EntityType<?> entity = context.getEntity().getType();
        EntityType<?> target = Util.getEntityType(context.getResourceLocationValue());

        RequirementRegistry.requirementsLogger(getLocation(), (entity != target));

        return  (entity != target);
    }

    @Override
    public ResourceLocation getLocation() {
        return TheQuestForge.id("entity_is_not");
    }
}
