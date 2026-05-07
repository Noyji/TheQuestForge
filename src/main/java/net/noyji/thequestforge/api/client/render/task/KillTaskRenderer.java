package net.noyji.thequestforge.api.client.render.task;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.client.ITaskRenderer;
import net.noyji.thequestforge.api.quest.task.KillTask;
import net.noyji.thequestforge.common.util.Util;

public class KillTaskRenderer implements ITaskRenderer<KillTask> {

    private static final ResourceLocation UNKNOWN_MOB =
            ResourceLocation.fromNamespaceAndPath(TheQuestForge.MODID, "textures/gui/quest_book/icons/unknown_mob.png");

    @Override
    public Component getName(KillTask task) {
        EntityType<?> entityType = task.getEntity();

        if (entityType != null) {

            return entityType.getDescription();
        }

        return Component.literal("Unknown Target");
    }

    @Override
    public void renderIcon(GuiGraphics guiGraphics, KillTask task, int x, int y) {
        EntityType<?> entityType = task.getEntity();
        ResourceLocation iconToRender = UNKNOWN_MOB;

        if (entityType != null) {

            ResourceLocation entityId = Util.getEntityResourceLocation(entityType);

            if (entityId == null) entityId = ResourceLocation.parse("minecraft:pig");

            String nameKey = entityId.getNamespace() + "_" + entityId.getPath();

            ResourceLocation specificIcon = ResourceLocation.fromNamespaceAndPath(
                    TheQuestForge.MODID,
                    "textures/gui/quest_book/icons/" + nameKey + ".png"
            );

            if (textureExists(specificIcon)) {
                iconToRender = specificIcon;
            }
        }

        RenderSystem.enableBlend();
        guiGraphics.blit(iconToRender, x, y, 0, 0, 16, 16, 16, 16);
        RenderSystem.disableBlend();
    }

    private boolean textureExists(ResourceLocation location) {
        return Minecraft.getInstance().getResourceManager().getResource(location).isPresent();
    }
}
