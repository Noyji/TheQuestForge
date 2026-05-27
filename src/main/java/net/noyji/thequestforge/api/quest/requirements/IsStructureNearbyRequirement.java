package net.noyji.thequestforge.api.quest.requirements;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.common.util.Util;

public class IsStructureNearbyRequirement extends AbstractRequirement {
    //thequestforge:is_structure_nearby:#minecraft:village-2000
    @Override
    public boolean check(RequirementContext context) {
        String rawValue = context.getStringValue();
        if (rawValue == null || !rawValue.contains("-")) return false;

        String[] value = rawValue.split("-", 2);
        String structureStr = value[0].trim();

        int radius;
        try {
            radius = Math.abs(Integer.parseInt(value[1].trim()));
        } catch (NumberFormatException e) {
            return false;
        }

        Level level = context.getEntity().level();
        BlockPos blockPos = context.getEntity().blockPosition();

        BlockPos blockPosTo = Util.findStructurePosLocate(level, blockPos, structureStr, radius);
        if (blockPosTo == null) return false;

        context.addCustomData("direction", Util.getDirectionText(blockPos, blockPosTo));

        return true;
    }

    @Override
    public ResourceLocation getLocation() {
        return TheQuestForge.id("is_structure_nearby");
    }
}