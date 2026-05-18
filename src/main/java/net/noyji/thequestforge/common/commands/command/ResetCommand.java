package net.noyji.thequestforge.common.commands.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.capability.entity.EntityQuestData;
import net.noyji.thequestforge.data.capability.player.PlayerQuestData;
import net.noyji.thequestforge.network.TheQuestForgeNetworking;
import net.noyji.thequestforge.network.s2c.RemovePlayerQuestS2CPacket;
import net.noyji.thequestforge.network.s2c.SyncEntityQuestDataS2CPacket;
import net.noyji.thequestforge.network.s2c.SyncPlayerAllQuestS2CPacket;
import net.noyji.thequestforge.network.s2c.UnlockNpcS2CPacket;

public class ResetCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> root){
        root.then(Commands.literal("reset").then(Commands.argument("target", EntityArgument.entity())
                .executes(commandContext -> {
                    Entity entity = EntityArgument.getEntity(commandContext, "target");
                    return execute(commandContext.getSource(), entity);
                })));
    }

    private static int execute(CommandSourceStack source, Entity entity){

        if (entity instanceof Player player) {
            PlayerQuestData playerQuestData = CapabilityUtil.getPlayerQuestData(player);
            playerQuestData.reset(true);
            TheQuestForgeNetworking.sendToPlayer(new SyncPlayerAllQuestS2CPacket(playerQuestData.serializeNBT()), player);
            source.sendSuccess(() -> Component.translatable("command.thequestforge.reset.success_player"), true);
            return 1;
        }

        EntityQuestData entityQuestData = CapabilityUtil.getEntityQuestData(entity);

        if (entityQuestData.isEmpty()) {
            source.sendFailure(Component.translatable("command.thequestforge.reset.is_not_quest_giver"));
            return 0;
        }

        entityQuestData.resetQuest();

        MinecraftServer server = entity.level().getServer();
        if (server == null) return 0;

        PlayerList playerList = server.getPlayerList();
        for (ServerPlayer serverPlayer : playerList.getPlayers()){

            PlayerQuestData playerQuestData = CapabilityUtil.getPlayerQuestData(serverPlayer);

            if (!playerQuestData.hasQuest(entity.getUUID())) continue;
            TheQuestForge.LOGGER.debug("Player {} update quest!", serverPlayer.getTabListDisplayName());

            playerQuestData.removeQuest(entity.getUUID());
            TheQuestForgeNetworking.sendToPlayer(new RemovePlayerQuestS2CPacket(entity.getUUID()), serverPlayer);

        }

        TheQuestForgeNetworking.sendToTrackingEntity(new SyncEntityQuestDataS2CPacket(entity.getId(), entityQuestData.serializeNBT(), false), entity);
        TheQuestForgeNetworking.sendToAll(new UnlockNpcS2CPacket(entity.getUUID()));

        source.sendSuccess(() -> Component.translatable("command.thequestforge.reset.success"), true);

        return 1;
    }
}
