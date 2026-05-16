package net.noyji.thequestforge.common.commands.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.noyji.thequestforge.common.util.EntityQuestHandler;
import net.noyji.thequestforge.common.util.QuestGenerator;
import net.noyji.thequestforge.common.util.Util;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.capability.entity.EntityQuestData;
import net.noyji.thequestforge.data.managers.QuestGiversManager;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;

public class SpawnQuestGiverCommand {

    private static final SuggestionProvider<CommandSourceStack> GIVER_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(QuestGiversManager.INSTANCE.getAllGiverIds(), builder);
    private static final SuggestionProvider<CommandSourceStack> QUEST_TEMPLATE = (context, builder) ->
            SharedSuggestionProvider.suggest(QuestTemplateManager.INSTANCE.getTemplatesIds(), builder);


    public static void register(LiteralArgumentBuilder<CommandSourceStack> root){
        root.then(Commands.literal("spawn")
                .then(Commands.argument("giver_id", ResourceLocationArgument.id())
                        .suggests(GIVER_SUGGESTIONS)
                        .then(Commands.argument("quest_template_id", ResourceLocationArgument.id())
                                .suggests(QUEST_TEMPLATE)
                                .executes(commandContext -> {
                                    ResourceLocation giverId = ResourceLocationArgument.getId(commandContext, "giver_id");
                                    ResourceLocation questTemplateId = ResourceLocationArgument.getId(commandContext, "quest_template_id");
                                    return execute(commandContext.getSource(), giverId, questTemplateId);
                                })
                        )
                )
        );
    }

    private static int execute(CommandSourceStack source, ResourceLocation entityId, ResourceLocation questTemplateId){
        EntityType<?> entityType = Util.getEntityType(entityId);

        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        if (entityType == null || !ForgeRegistries.ENTITY_TYPES.containsKey(entityId)){
            source.sendFailure(Component.translatable("command.thequestforge.spawn.entity_type_is_null"));
            return 0;
        }

        Entity entity = entityType.create(level);

        if (!(entity instanceof Mob mob)){
            source.sendFailure(Component.translatable("command.thequestforge.spawn.entity_not_mob"));
            return 0;
        }

        if (QuestTemplateManager.INSTANCE.getQuestTemplate(questTemplateId) == null){
            source.sendFailure(Component.translatable("command.thequestforge.spawn.template_not_found"));
            return 0;
        }

        mob.moveTo(pos.x, pos.y, pos.z, source.getRotation().x, source.getRotation().y);

        EntityQuestHandler.tryCreateEmptyQuest(mob, false);

        EntityQuestData entityQuestData = CapabilityUtil.getEntityQuestData(entity);

        entityQuestData.addQuest(QuestGenerator.generateQuest(entity, questTemplateId));

        level.addFreshEntity(mob);

        source.sendSuccess(() -> Component.translatable("command.thequestforge.spawn.success"), true);

        return 1;
    }
}
