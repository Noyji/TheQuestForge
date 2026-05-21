package net.noyji.thequestforge.common.commands.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.ForgeRegistries;
import net.noyji.thequestforge.api.quest.registry.ActionRegistry;
import net.noyji.thequestforge.api.quest.registry.RequirementRegistry;
import net.noyji.thequestforge.api.quest.registry.TaskHandlerRegistry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportRegistryCommand {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
               root.then(Commands.literal("registry")
                        .then(Commands.literal("export")
                                .executes(context -> exportRegistry(context.getSource()))));
    }

    private static int exportRegistry(CommandSourceStack source) {
        JsonObject root = new JsonObject();

        JsonArray itemsArray = new JsonArray();
        ForgeRegistries.ITEMS.getKeys().forEach(id -> itemsArray.add(id.toString()));
        root.add("items", itemsArray);

        JsonArray entitiesArray = new JsonArray();
        ForgeRegistries.ENTITY_TYPES.getKeys().forEach(id -> entitiesArray.add(id.toString()));
        root.add("entities", entitiesArray);

        JsonArray enchantmentsArray = new JsonArray();
        ForgeRegistries.ENCHANTMENTS.getKeys().forEach(id -> enchantmentsArray.add(id.toString()));
        root.add("enchantments", enchantmentsArray);

        JsonArray taskTypesArray = new JsonArray();
        TaskHandlerRegistry.REGISTRY.get().getKeys().forEach(id -> taskTypesArray.add(id.toString()));
        root.add("task_types", taskTypesArray);

        JsonArray actionsArray = new JsonArray();
        ActionRegistry.REGISTRY.get().getKeys().forEach(id -> actionsArray.add(id.toString()));
        actionsArray.add("thequestforge:close");
        root.add("actions", actionsArray);

        JsonArray requirementsArray = new JsonArray();
        RequirementRegistry.REGISTRY.get().getKeys().forEach(id -> requirementsArray.add(id.toString()));
        root.add("requirements", requirementsArray);

        File outputFile = new File("thequestforge_registry.json");
        try (FileWriter writer = new FileWriter(outputFile)) {
            GSON.toJson(root, writer);
            source.sendSuccess(() -> Component.literal("Реестр успешно экспортирован в файл: " + outputFile.getAbsolutePath()), true);
        } catch (IOException e) {
            source.sendFailure(Component.literal("Ошибка при записи файла реестра: " + e.getMessage()));
            return 0;
        }

        return 1;
    }
}