package net.noyji.thequestforge.client.render;

import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
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

            if (event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) {
                event.setCanceled(true);
            }

            if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
                event.getGuiGraphics().pose().pushPose();
                event.getGuiGraphics().pose().translate(0, ease * 40.0f, 0);
            }
        }
    }

    public static void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event) {
        float progress = DialogueCameraManager.getProgress();

        if (progress > 0.0f) {
            if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
                event.getGuiGraphics().pose().popPose();
            }
        }
    }

    public static void onRenderPlayerPre(net.minecraftforge.client.event.RenderPlayerEvent.Pre event) {
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

    public static void onRenderPlayerPost(net.minecraftforge.client.event.RenderPlayerEvent.Post event) {
        float progress = DialogueCameraManager.getProgress();

        if (progress > 0.0f) {
            float ease = 1.0f - (float) Math.pow(1.0f - progress, 3.0f);
            float scale = 1.0f - ease;

            if (scale > 0.05f) {
                event.getPoseStack().popPose();
            }
        }
    }
}
