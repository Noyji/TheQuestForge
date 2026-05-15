package net.noyji.thequestforge.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.network.c2s.ActionHandlerC2SPacket;
import net.noyji.thequestforge.network.c2s.RunFunctionsC2SPacket;
import net.noyji.thequestforge.network.s2c.AddDialogStageS2CPacket;
import net.noyji.thequestforge.network.c2s.InteractNpcC2SPacket;
import net.noyji.thequestforge.network.c2s.RemovePlayerQuestC2SPacket;
import net.noyji.thequestforge.network.s2c.*;

import java.util.Optional;

public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;

    private static int id(){
        return packetId++;
    }

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            TheQuestForge.id("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register(){
        //S2C
        CHANNEL.registerMessage(
                id(),
                SyncEntityQuestDataS2CPacket.class,
                SyncEntityQuestDataS2CPacket::encode,
                SyncEntityQuestDataS2CPacket::decode,
                SyncEntityQuestDataS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                id(),
                OpenQuestGuiS2CPacket.class,
                OpenQuestGuiS2CPacket::encode,
                OpenQuestGuiS2CPacket::decode,
                OpenQuestGuiS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                id(),
                SyncQuestTemplateS2CPacket.class,
                SyncQuestTemplateS2CPacket::encode,
                SyncQuestTemplateS2CPacket::decode,
                SyncQuestTemplateS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                id(),
                SyncPlayerAllQuestS2CPacket.class,
                SyncPlayerAllQuestS2CPacket::encode,
                SyncPlayerAllQuestS2CPacket::decode,
                SyncPlayerAllQuestS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                id(),
                AddPlayerQuestS2CPacket.class,
                AddPlayerQuestS2CPacket::encode,
                AddPlayerQuestS2CPacket::decode,
                AddPlayerQuestS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                id(),
                SyncSpecificPlayerQuestS2CPacket.class,
                SyncSpecificPlayerQuestS2CPacket::encode,
                SyncSpecificPlayerQuestS2CPacket::decode,
                SyncSpecificPlayerQuestS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                id(),
                AddDialogStageS2CPacket.class,
                AddDialogStageS2CPacket::encode,
                AddDialogStageS2CPacket::decode,
                AddDialogStageS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                id(),
                RemovePlayerQuestS2CPacket.class,
                RemovePlayerQuestS2CPacket::encode,
                RemovePlayerQuestS2CPacket::decode,
                RemovePlayerQuestS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                id(),
                QuestToastS2CPacket.class,
                QuestToastS2CPacket::encode,
                QuestToastS2CPacket::decode,
                QuestToastS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                id(),
                ClosePlayerGuiS2CPacket.class,
                ClosePlayerGuiS2CPacket::encode,
                ClosePlayerGuiS2CPacket::decode,
                ClosePlayerGuiS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                id(),
                ChainProgressUpdateS2CPacket.class,
                ChainProgressUpdateS2CPacket::encode,
                ChainProgressUpdateS2CPacket::decode,
                ChainProgressUpdateS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                id(),
                UnlockNpcS2CPacket.class,
                UnlockNpcS2CPacket::encode,
                UnlockNpcS2CPacket::decode,
                UnlockNpcS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        //C2S
        CHANNEL.registerMessage(
                id(),
                ActionHandlerC2SPacket.class,
                ActionHandlerC2SPacket::encode,
                ActionHandlerC2SPacket::decode,
                ActionHandlerC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                id(),
                InteractNpcC2SPacket.class,
                InteractNpcC2SPacket::encode,
                InteractNpcC2SPacket::decode,
                InteractNpcC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                id(),
                RemovePlayerQuestC2SPacket.class,
                RemovePlayerQuestC2SPacket::encode,
                RemovePlayerQuestC2SPacket::decode,
                RemovePlayerQuestC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                id(),
                RunFunctionsC2SPacket.class,
                RunFunctionsC2SPacket::encode,
                RunFunctionsC2SPacket::decode,
                RunFunctionsC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    public static void debugInfo(String string){
        TheQuestForge.LOGGER.debug("Sync! {}", string);
    }

    public static <MSG> void sendToPlayer(MSG message, Player player){
        if (player instanceof ServerPlayer serverPlayer){
            sendToPlayer(message, serverPlayer);
        }
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer serverPlayer){
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), message);
    }

    public static <MSG> void sendToServer(MSG message){
        CHANNEL.sendToServer(message);
    }

    public static <MSG> void sendToAll(MSG message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }

    public static <MSG> void sendToTrackingEntity(MSG message, Entity entity){
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }
}
