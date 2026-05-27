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

        String value = context.getStringValue();
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        ResourceLocation targetTag = ResourceLocation.parse(value);
        Level level = context.getEntity().level();
        BlockPos pos = context.getEntity().blockPosition();

        TheQuestForge.LOGGER.debug("Checking BiomeTag requirement. Target tag: #{}", targetTag);

        Holder<Biome> currentBiome = level.getBiome(pos);
        TagKey<Biome> tagKey = TagKey.create(Registries.BIOME, targetTag);

        boolean isMatch = currentBiome.is(tagKey);

        if (isMatch) {
            TheQuestForge.LOGGER.debug("Success: Entity at [X:{}, Z:{}] matches biome tag #{}", pos.getX(), pos.getZ(), targetTag);
        } else {
            String biomeName = currentBiome.unwrapKey().map(key -> key.location().toString()).orElse("unknown");
            TheQuestForge.LOGGER.debug("Fail: Current biome '{}' at [X:{}, Z:{}] does not have tag #{}", biomeName, pos.getX(), pos.getZ(), targetTag);
        }

        RequirementRegistry.requirementsLogger(getLocation(), isMatch);

        return isMatch;
    }

    @Override
    public ResourceLocation getLocation() {
        return TheQuestForge.id("biome_tag_is");
    }
}
