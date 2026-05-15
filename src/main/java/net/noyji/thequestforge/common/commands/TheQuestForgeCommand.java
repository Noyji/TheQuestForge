package net.noyji.thequestforge.common.commands;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.noyji.thequestforge.TheQuestForge;

@Mod.EventBusSubscriber(modid = TheQuestForge.MODID)
public class TheQuestForgeCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}
