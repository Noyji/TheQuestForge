package net.noyji.thequestforge.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.capability.entity.EntityQuestData;
import net.noyji.thequestforge.data.capability.player.PlayerQuestData;
import net.noyji.thequestforge.data.managers.QuestGiversManager;
import org.joml.Matrix4f;

import java.util.UUID;

public class RenderQuestIcons {
    private static final ResourceLocation QUEST_AVAILABLE_ICON_TEXTURE =
            TheQuestForge.id("textures/quest_icons/quest_available.png");
    private static final ResourceLocation QUEST_IN_PROGRESS_ICON_TEXTURE =
            TheQuestForge.id("textures/quest_icons/quest_in_progress.png");
    private static final ResourceLocation QUEST_READY_ICON_TEXTURE =
            TheQuestForge.id("textures/quest_icons/quest_ready.png");

    public static void renderIcon(RenderLivingEvent.Post<LivingEntity, ?> event){
        LivingEntity entity = event.getEntity();

        if (!QuestGiversManager.INSTANCE.thisQuestGiverOrVillager(entity)) return;

        EntityQuestData entityQuestData = CapabilityUtil.getEntityQuestData(entity);
        if (!entityQuestData.isQuestGiver()) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (player.distanceTo(entity) > 40.0f) return;

        PlayerQuestData playerQuestData = CapabilityUtil.getPlayerQuestData(player);

        if (playerQuestData.isNpcLocked(entity.getUUID())) return;

        UUID questId = entity.getUUID();
        ResourceLocation textureIcon = null;

        if (!playerQuestData.hasQuest(questId)){
            textureIcon = QUEST_AVAILABLE_ICON_TEXTURE;
        } else if ( playerQuestData.hasQuest(questId) && !playerQuestData.isQuestComplete(questId)) {
            textureIcon = QUEST_IN_PROGRESS_ICON_TEXTURE;
        } else if (playerQuestData.hasQuest(questId) && playerQuestData.isQuestComplete(questId)){
            textureIcon = QUEST_READY_ICON_TEXTURE;
        }

        if (textureIcon == null) return;
        render(event.getPoseStack(), event.getMultiBufferSource(), entity, textureIcon);
    }

    private static void render(PoseStack poseStack, MultiBufferSource bufferSource, LivingEntity entity, ResourceLocation texture){
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();

        poseStack.pushPose();

        double height = entity.getBbHeight() + 0.75D;
        poseStack.translate(0.0D, height, 0.0D);

        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));

        float scale = 0.025F;
        poseStack.scale(-scale, -scale, scale);

        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));

        int light = 15728880;

        vertexConsumer.vertex(matrix4f, -8.0F, 8.0F, 0.0F)
                .color(255, 255, 255, 255)
                .uv(0.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        vertexConsumer.vertex(matrix4f, 8.0F, 8.0F, 0.0F)
                .color(255, 255, 255, 255)
                .uv(1.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        vertexConsumer.vertex(matrix4f, 8.0F, -8.0F, 0.0F)
                .color(255, 255, 255, 255)
                .uv(1.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        vertexConsumer.vertex(matrix4f, -8.0F, -8.0F, 0.0F)
                .color(255, 255, 255, 255)
                .uv(0.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        poseStack.popPose();
    }
}
