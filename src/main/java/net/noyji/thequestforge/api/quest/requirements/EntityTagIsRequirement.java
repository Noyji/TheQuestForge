package net.noyji.thequestforge.api.quest.requirements;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.registry.RequirementRegistry;

public class EntityTagIsRequirement extends AbstractRequirement {
    //thequestforge:entity_tag_is:minecraft:zombies
    @Override
    public boolean check(RequirementContext context) {
        TagKey<EntityType<?>> tagKey = TagKey.create(Registries.ENTITY_TYPE, context.getResourceLocationValue());

        RequirementRegistry.requirementsLogger(getLocation(), context.getEntity().getType().is(tagKey));

        return context.getEntity().getType().is(tagKey);
    }

    @Override
    public ResourceLocation getLocation() {
        return TheQuestForge.id("entity_tag_is");
    }
}
