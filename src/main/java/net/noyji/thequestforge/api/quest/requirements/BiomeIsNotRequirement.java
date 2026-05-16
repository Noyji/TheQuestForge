package net.noyji.thequestforge.api.quest.requirements;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.registry.RequirementRegistry;

public class BiomeIsNotRequirement extends AbstractRequirement{
    //thequestforge:biome_is_not:minecraft:plains
    @Override
    public boolean check(RequirementContext context) {
        Level level = context.getEntity().level();
        BlockPos pos = context.getEntity().blockPosition();

        Holder<Biome> currentBiome = level.getBiome(pos);

        ResourceKey<Biome> targetKey = ResourceKey.create(Registries.BIOME, context.getResourceLocationValue());

        RequirementRegistry.requirementsLogger(getLocation(), !currentBiome.is(targetKey));

        return !currentBiome.is(targetKey);
    }

    @Override
    public ResourceLocation getLocation() {
        return TheQuestForge.id("biome_is_not");
    }
}
