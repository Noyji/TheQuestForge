package net.noyji.thequestforge.client.render;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ViewportEvent;
import net.noyji.thequestforge.config.ClientConfig;
import net.noyji.thequestforge.mixin.CameraInvoker;

public class DialogueCameraManager {

    private static boolean isActive = false;
    private static boolean isReturning = false;
    private static Entity targetNpc;
    private static float progress = 0.0f;

    private static Vec3 cinematicPos;
    private static float cinematicYaw;
    private static float cinematicPitch;

    private static float smoothedMouseX = 0.0f;
    private static float smoothedMouseY = 0.0f;

    public static void startFocus(Entity npc) {
        if (!ClientConfig.CAMERA_ZOOM.get()) return;
        targetNpc = npc;
        isReturning = false;
        isActive = true;

        smoothedMouseX = 0.0f;
        smoothedMouseY = 0.0f;

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 startPos = camera.getPosition();

        double npcEyeY = targetNpc.getY() + targetNpc.getEyeHeight();
        Vec3 npcFacePos = new Vec3(targetNpc.getX(), npcEyeY, targetNpc.getZ());

        Vec3 dirToPlayer = startPos.subtract(npcFacePos).normalize();
        cinematicPos = npcFacePos.add(dirToPlayer.scale(1.5));

        double dX = npcFacePos.x - cinematicPos.x;
        double dY = npcEyeY - cinematicPos.y;
        double dZ = npcFacePos.z - cinematicPos.z;
        double horizDist = Math.sqrt(dX * dX + dZ * dZ);

        cinematicYaw = (float) (Math.atan2(dZ, dX) * (180D / Math.PI)) - 90.0F;
        cinematicPitch = (float) -(Math.atan2(dY, horizDist) * (180D / Math.PI));
    }

    public static void stopFocus() {
        if (isActive) isReturning = true;
    }

    public static float getProgress() {
        return progress;
    }

    public static void onCameraUpdate(ViewportEvent.ComputeCameraAngles event) {
        if (!isActive || targetNpc == null || Minecraft.getInstance().player == null) return;

        float delta = Minecraft.getInstance().getDeltaFrameTime();
        float speed = 0.05f;

        if (isReturning) {
            progress -= delta * speed * 1.5f;
            if (progress <= 0.0f) {
                progress = 0.0f;
                isActive = false;
                isReturning = false;
                return;
            }
        } else {
            progress += delta * speed;
            if (progress > 1.0f) progress = 1.0f;
        }

        float ease = 1.0f - (float) Math.pow(1.0f - progress, 3.0f);
        Camera camera = event.getCamera();

        double natX = camera.getPosition().x;
        double natY = camera.getPosition().y;
        double natZ = camera.getPosition().z;
        float natYaw = event.getYaw();
        float natPitch = event.getPitch();

        float currentCinematicYaw = cinematicYaw;
        float currentCinematicPitch = cinematicPitch;
        double currentCinematicX = cinematicPos.x;
        double currentCinematicY = cinematicPos.y;
        double currentCinematicZ = cinematicPos.z;

        if (ClientConfig.CAMERA_PARALLAX.get()) {
            Minecraft mc = Minecraft.getInstance();

            double rawMouseX = mc.mouseHandler.xpos();
            double rawMouseY = mc.mouseHandler.ypos();
            double screenWidth = mc.getWindow().getScreenWidth();
            double screenHeight = mc.getWindow().getScreenHeight();

            float targetMouseX = (float) ((rawMouseX / screenWidth) * 2.0 - 1.0);
            float targetMouseY = (float) ((rawMouseY / screenHeight) * 2.0 - 1.0);

            float followSpeed = 0.002f;

            smoothedMouseX = Mth.lerp(followSpeed, smoothedMouseX, targetMouseX);
            smoothedMouseY = Mth.lerp(followSpeed, smoothedMouseY, targetMouseY);

            float maxSwayYaw = 1.5f;
            float maxSwayPitch = 0.5f;
            float parallaxOffset = 0.05f;

            float swayYaw = smoothedMouseX * maxSwayYaw;
            float swayPitch = smoothedMouseY * maxSwayPitch;

            currentCinematicYaw += (swayYaw * ease);
            currentCinematicPitch += (swayPitch * ease);

            double offsetX = -Math.cos(Math.toRadians(cinematicYaw)) * smoothedMouseX * parallaxOffset;
            double offsetZ = -Math.sin(Math.toRadians(cinematicYaw)) * smoothedMouseX * parallaxOffset;
            double offsetY = -smoothedMouseY * parallaxOffset;

            currentCinematicX += (offsetX * ease);
            currentCinematicY += (offsetY * ease);
            currentCinematicZ += (offsetZ * ease);
        }

        float lerpedYaw = Mth.rotLerp(ease, natYaw, currentCinematicYaw);
        float lerpedPitch = Mth.lerp(ease, natPitch, currentCinematicPitch);

        double lerpedX = Mth.lerp(ease, natX, currentCinematicX);
        double lerpedY = Mth.lerp(ease, natY, currentCinematicY);
        double lerpedZ = Mth.lerp(ease, natZ, currentCinematicZ);

        ((CameraInvoker) camera).invokeSetPosition(lerpedX, lerpedY, lerpedZ);
        event.setYaw(lerpedYaw);
        event.setPitch(lerpedPitch);
    }
}