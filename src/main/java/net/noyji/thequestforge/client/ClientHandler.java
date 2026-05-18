package net.noyji.thequestforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.InputEvent;
import net.noyji.thequestforge.data.managers.QuestGiversManager;
import net.noyji.thequestforge.network.TheQuestForgeNetworking;
import net.noyji.thequestforge.network.c2s.InteractNpcC2SPacket;

public class ClientHandler {

    public static void interact(InputEvent.Key event){
        if (KeyBindings.INTERACT_KEY.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.hitResult == null || minecraft.screen != null) return;

            Entity target = minecraft.crosshairPickEntity;
            if (target == null) return;

            if (!QuestGiversManager.INSTANCE.thisQuestGiverOrVillager(target)) return;

            TheQuestForgeNetworking.sendToServer(new InteractNpcC2SPacket(target.getId()));
        }
    }

}
