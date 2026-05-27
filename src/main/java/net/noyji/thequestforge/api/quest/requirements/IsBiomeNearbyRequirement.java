package net.noyji.thequestforge.api.quest.requirements;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.common.util.Util;

import java.util.function.Predicate;

public class IsBiomeNearbyRequirement extends AbstractRequirement {
    // thequestforge:is_biome_nearby:minecraft:desert-200
    // thequestforge:is_biome_nearby:#minecraft:is_forest-200
    @Override
    public boolean check(RequirementContext context) {
        String rawValue = context.getStringValue();
        if (rawValue == null || !rawValue.contains("-")) return false;

        String[] value = rawValue.split("-", 2);
        String biomeStr = value[0].trim();

        Predicate<Holder<Biome>> biomePredicate;
        if (biomeStr.startsWith("#")) {
            TagKey<Biome> tagKey = TagKey.create(Registries.BIOME, ResourceLocation.parse(biomeStr.substring(1)));
            biomePredicate = holder -> holder.is(tagKey);
        } else {
            ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, ResourceLocation.parse(biomeStr));
            biomePredicate = holder -> holder.is(biomeKey);
        }

        int radius;
        try {
            radius = Math.abs(Integer.parseInt(value[1].trim()));
        } catch (NumberFormatException e) {
            return false;
        }

        Level level = context.getEntity().level();
        BlockPos blockPos = context.getEntity().blockPosition();

        BlockPos blockPosTo = Util.findBiomePosLocate(level, blockPos, biomePredicate, radius);
        if (blockPosTo == null) return false;

        context.addCustomData("direction", Util.getDirectionText(blockPos, blockPosTo));

        return true;
    }

    @Override
    public ResourceLocation getLocation() {
        return TheQuestForge.id("is_biome_nearby");
    }
}