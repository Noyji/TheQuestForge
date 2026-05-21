package net.noyji.thequestforge.common.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.common.commands.command.ExportRegistryCommand;
import net.noyji.thequestforge.common.commands.command.ResetCommand;
import net.noyji.thequestforge.common.commands.command.SpawnQuestGiverCommand;

@Mod.EventBusSubscriber(modid = TheQuestForge.MODID)
public class TheQuestForgeCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("questforge")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2));

        ResetCommand.register(root);
        SpawnQuestGiverCommand.register(root);
        ExportRegistryCommand.register(root);

        event.getDispatcher().register(root);
    }
}
