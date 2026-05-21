package net.noyji.thequestforge.client.render;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;

public class DialogueGuiManager {

    public static void onRenderHand(RenderHandEvent event) {
        float progress = DialogueCameraManager.getProgress();

        if (progress > 0.0f) {
            float ease = 1.0f - (float) Math.pow(1.0f - progress, 3.0f);
            event.getPoseStack().translate(0, -ease * 1.5f, 0);
        }
    }

    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        float progress = DialogueCameraManager.getProgress();

        if (progress > 0.0f) {
            float ease = 1.0f - (float) Math.pow(1.0f - progress, 3.0f);
            ResourceLocation overlayId = event.getOverlay().id();

            if (overlayId.equals(VanillaGuiOverlay.CROSSHAIR.id())) {
                event.setCanceled(true);
            } else if (isBottomHudOverlay(overlayId)) {
                event.getGuiGraphics().pose().pushPose();
                event.getGuiGraphics().pose().translate(0, ease * 60.0f, 0);
            }
        }
    }

    public static void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event) {
        float progress = DialogueCameraManager.getProgress();

        if (progress > 0.0f) {
            ResourceLocation overlayId = event.getOverlay().id();

            if (isBottomHudOverlay(overlayId)) {
                event.getGuiGraphics().pose().popPose();
            }
        }
    }

    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        float progress = DialogueCameraManager.getProgress();

        if (progress > 0.0f) {
            float ease = 1.0f - (float) Math.pow(1.0f - progress, 3.0f);
            float scale = 1.0f - ease;

            if (scale <= 0.05f) {
                event.setCanceled(true);
                return;
            }

            event.getPoseStack().pushPose();
            event.getPoseStack().scale(scale, scale, scale);
        }
    }

    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        float progress = DialogueCameraManager.getProgress();

        if (progress > 0.0f) {
            float ease = 1.0f - (float) Math.pow(1.0f - progress, 3.0f);
            float scale = 1.0f - ease;

            if (scale > 0.05f) {
                event.getPoseStack().popPose();
            }
        }
    }

    private static boolean isBottomHudOverlay(ResourceLocation id) {
        return id.equals(VanillaGuiOverlay.HOTBAR.id()) ||
                id.equals(VanillaGuiOverlay.PLAYER_HEALTH.id()) ||
                id.equals(VanillaGuiOverlay.FOOD_LEVEL.id()) ||
                id.equals(VanillaGuiOverlay.ARMOR_LEVEL.id()) ||
                id.equals(VanillaGuiOverlay.EXPERIENCE_BAR.id()) ||
                id.equals(VanillaGuiOverlay.JUMP_BAR.id()) ||
                id.equals(VanillaGuiOverlay.MOUNT_HEALTH.id()) ||
                id.equals(VanillaGuiOverlay.AIR_LEVEL.id());
    }
}