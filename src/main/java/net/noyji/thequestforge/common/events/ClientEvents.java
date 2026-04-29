package net.noyji.thequestforge.common.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.noyji.thequestforge.client.render.DialogueCameraManager;
import net.noyji.thequestforge.client.render.DialogueGuiManager;
import net.noyji.thequestforge.client.render.RenderQuestIcons;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<LivingEntity, ?> event) {
        RenderQuestIcons.renderIcon(event);
    }
    @SubscribeEvent
    public static void onCameraUpdate(ViewportEvent.ComputeCameraAngles event) {
        DialogueCameraManager.onCameraUpdate(event);
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        DialogueGuiManager.onRenderHand(event);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        DialogueGuiManager.onRenderGuiOverlayPre(event);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event) {
        DialogueGuiManager.onRenderGuiOverlayPost(event);
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(net.minecraftforge.client.event.RenderPlayerEvent.Pre event) {
        DialogueGuiManager.onRenderPlayerPre(event);
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(net.minecraftforge.client.event.RenderPlayerEvent.Post event) {
        DialogueGuiManager.onRenderPlayerPost(event);
    }
}
