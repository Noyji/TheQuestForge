package net.noyji.thequestforge.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.network.s2c.OpenQuestGuiS2CPacket;
import net.noyji.thequestforge.network.s2c.SyncEntityQuestDataS2CPacket;
import net.noyji.thequestforge.network.s2c.SyncQuestTemplateS2CPacket;

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
    }

    public static void debugInfo(String string){
        TheQuestForge.LOGGER.debug("Sync! {}", string);
    }

    public static void sendToPlayer(Object message, ServerPlayer serverPlayer){
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), message);
    }

    public static void sendToServer(Object message){
        CHANNEL.sendToServer(message);
    }

    public static void sendToTracking(Object message, Entity entity){
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }
}
