package net.noyji.thequestforge.client.render;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.noyji.thequestforge.client.KeyBindings;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.managers.QuestGiversManager;

public class AdditionalRenders {
    public static void renderInteractMessage(RenderGuiOverlayEvent.Post event){
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) return;

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.ENTITY){
            EntityHitResult entityHitResult = (EntityHitResult) minecraft.hitResult;
            Entity target = entityHitResult.getEntity();

            if (!QuestGiversManager.INSTANCE.thisQuestGiverOrVillager(target)) return;

            if (!CapabilityUtil.getEntityQuestData(target).isQuestGiver()) return;

            Player player = minecraft.player;
            if (player == null) return;

            if (CapabilityUtil.getPlayerQuestData(player).isNpcLocked(target.getUUID())) return;

            if (target.distanceTo(minecraft.player) > 4.0f) return;

            Component keyName = KeyBindings.INTERACT_KEY.getTranslatedKeyMessage();
            Component interactMassage = Component.literal("[")
                    .append(keyName.copy().withStyle(ChatFormatting.YELLOW))
                    .append("] ")
                    .append(Component.translatable("overlay.thequestforge.interact_message"));

            int screenWidth = minecraft.getWindow().getGuiScaledWidth();
            int screenHeight = minecraft.getWindow().getGuiScaledHeight();


            int textWidth = minecraft.font.width(interactMassage);
            int x = (screenWidth - textWidth) / 2;

            int y = (screenHeight / 2) + 15;

            event.getGuiGraphics().drawString(minecraft.font, interactMassage, x, y, 0xFFFFFF, true);
        }
    }
}
