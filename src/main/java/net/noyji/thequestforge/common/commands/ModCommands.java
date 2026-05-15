package net.noyji.thequestforge.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("questforge")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2));

        ResetCommand.register(root);
        SpawnQuestGiverCommand.register(root);

        dispatcher.register(root);
    }
}
