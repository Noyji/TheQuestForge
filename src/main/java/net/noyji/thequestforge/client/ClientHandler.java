package net.noyji.thequestforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.noyji.thequestforge.api.client.registry.TaskRendererRegistry;
import net.noyji.thequestforge.api.client.render.task.CollectTaskRenderer;
import net.noyji.thequestforge.api.client.render.task.KillTaskRenderer;
import net.noyji.thequestforge.api.quest.task.CollectTask;
import net.noyji.thequestforge.api.quest.task.KillTask;
import net.noyji.thequestforge.data.managers.QuestGiversManager;
import net.noyji.thequestforge.network.ModNetworking;
import net.noyji.thequestforge.network.c2s.InteractNpcC2SPacket;

public class ClientHandler {

    public static void interact(InputEvent.Key event){
        if (KeyBindings.INTERACT_KEY.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();

            if (minecraft.hitResult == null || minecraft.screen != null) return;

            Entity target = minecraft.crosshairPickEntity;
            if (target == null) return;

            if (!QuestGiversManager.INSTANCE.thisQuestGiverOrVillager(target)) return;

            ModNetworking.sendToServer(new InteractNpcC2SPacket(target.getId()));
        }
    }

}
