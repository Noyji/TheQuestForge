package net.noyji.thequestforge.api.quest.requirements;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.registry.RequirementRegistry;

public class BiomeTagIsRequirement extends AbstractRequirement{
    //thequestforge:biome_tag_is:minecraft:is_desert
    @Override
    public boolean check(RequirementContext context) {
        Level level = context.getEntity().level();
        BlockPos pos = context.getEntity().blockPosition();

        Holder<Biome> currentBiome = level.getBiome(pos);
        TagKey<Biome> tagKey = TagKey.create(Registries.BIOME, context.getResourceLocationValue());

        RequirementRegistry.requirementsLogger(getLocation(), currentBiome.is(tagKey));

        return currentBiome.is(tagKey);
    }

    @Override
    public ResourceLocation getLocation() {
        return TheQuestForge.id("biome_tag_is");
    }
}
