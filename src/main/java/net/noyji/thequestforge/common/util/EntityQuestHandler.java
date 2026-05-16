package net.noyji.thequestforge.common.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.config.ServerConfig;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.capability.entity.DialogSessionManager;
import net.noyji.thequestforge.data.capability.entity.EntityQuestData;
import net.noyji.thequestforge.data.capability.player.PlayerQuestData;
import net.noyji.thequestforge.data.managers.QuestGiversManager;
import net.noyji.thequestforge.data.quest.entity.Quest;
import net.noyji.thequestforge.data.quest.player.components.QuestType;
import net.noyji.thequestforge.network.ModNetworking;
import net.noyji.thequestforge.network.s2c.*;
import org.jetbrains.annotations.NotNull;

public class EntityQuestHandler {
    private static final RandomSource RANDOM = RandomSource.create();

    private static boolean tryCreateQuestGiver(Entity entity){
        if (entity == null) return false;

        if (!QuestGiversManager.INSTANCE.thisQuestGiverOrVillager(entity)) return  false;

        EntityQuestData entityQuestData = CapabilityUtil.getEntityQuestData(entity);
        if (entityQuestData.isCheck()) {
            return false;
        }
        entityQuestData.checked();

        return  (RANDOM.nextInt(100) < ServerConfig.SPAWN_QUEST_GIVER_CHANCE.get());
    }

    public static void tryCreateEmptyQuest(Entity entity, boolean useChance){
        if (useChance) {
            if (!tryCreateQuestGiver(entity)) return;
        }
        TheQuestForge.LOGGER.debug("Is quest giver! {}", Util.getEntityResourceLocation(entity));

        EntityQuestData entityQuestData = CapabilityUtil.getEntityQuestData(entity);

        long currentCycle = entity.level().getGameTime() / (ServerConfig.TIME_TO_RESET_NPCS.get() * 24_000L);
        entityQuestData.setLastResetCycle(currentCycle);
        entityQuestData.makeHimQuestGiver();
    }

    public static void immobilize(LivingEvent.@NotNull LivingTickEvent event){
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity.level().isClientSide) return;
        if (!(QuestGiversManager.INSTANCE.thisQuestGiverOrVillager(livingEntity))) return;
        if (!(livingEntity instanceof Mob mob)) return;

        Player talkingTo = DialogSessionManager.getTalkingPlayer(mob);
        if (talkingTo == null) return;

        mob.getNavigation().stop();
        mob.setDeltaMovement(0, mob.getDeltaMovement().y, 0);
        mob.getLookControl().setLookAt(talkingTo);

        if (mob.distanceTo(talkingTo) > 6.0f || !talkingTo.isAlive()) {
            DialogSessionManager.stopDialog(mob);
        }
    }

    public static void onEntityInteract(Player player, @NotNull Entity target){
        if (target.distanceTo(player) > 4.0f) return;

        if (CapabilityUtil.getPlayerQuestData(player).isNpcLocked(target.getUUID())) {
            player.sendSystemMessage(Component.literal("Я пока не нуждаюсь в твоей помощи. Приходи позже."));
            return;
        }

        EntityQuestData entityQuestData = CapabilityUtil.getEntityQuestData(target);
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        if (!(target.isAlive())) return;

        if (!entityQuestData.isQuestGiver()) {
            TheQuestForge.LOGGER.debug("is not quest giver");
            return;
        }

        DialogSessionManager.startDialog((Mob) target, player);

        if (entityQuestData.hasQuest()) {
            TheQuestForge.LOGGER.debug("Quest is already have!");
            ModNetworking.sendToPlayer(new OpenQuestGuiS2CPacket(target.getId()), serverPlayer);
            return;
        }

        String pool;
        if (target instanceof Villager villager){
            pool = villager.getVillagerData().getProfession().toString();
        } else {
            pool = QuestGiversManager.INSTANCE.getRandomPool(target);
        }

        if (pool == null || pool.isEmpty()){
            TheQuestForge.LOGGER.warn("Error: Pool not found for creature: {}", Util.getEntityResourceLocation(target));
            return;
        }

        Quest quest = QuestGenerator.generateQuest(player, target, pool);

        if (quest == null) {
            player.sendSystemMessage(Component.literal("У меня сейчас нет для тебя подходящих заданий..."));
            return;
        }

        entityQuestData.addQuest(quest);

        ModNetworking.sendToPlayer(new SyncEntityQuestDataS2CPacket(target.getId(), entityQuestData.serializeNBT(), true), serverPlayer);
        ModNetworking.debugInfo("Quest after generation!");
    }

    public static void resetCycle(LivingEvent.LivingTickEvent event){
        if (ServerConfig.TIME_TO_RESET_NPCS.get() == 501) return;

        Entity entity = event.getEntity();

        if (entity.tickCount % 20 != 0) return;

        if (entity.level().isClientSide) return;

        EntityQuestData entityQuestData = CapabilityUtil.getEntityQuestData(entity);
        if (!entityQuestData.isQuestGiver()) return;

        if (entityQuestData.getQuest() != null) {
            if (entityQuestData.getQuest().getType().equals(QuestType.STORY)) return;
        }

        long currentCycle = entity.level().getGameTime() / (ServerConfig.TIME_TO_RESET_NPCS.get() * 24_000);

        if (entityQuestData.getLastResetCycle() < currentCycle) {
            entityQuestData.setLastResetCycle(currentCycle);
            entityQuestData.resetQuest();

            MinecraftServer server = entity.level().getServer();
            if (server == null) return;

            PlayerList playerList = server.getPlayerList();
            for (ServerPlayer serverPlayer : playerList.getPlayers()){

                PlayerQuestData playerQuestData = CapabilityUtil.getPlayerQuestData(serverPlayer);

                if (!playerQuestData.hasQuest(entity.getUUID())) continue;
                TheQuestForge.LOGGER.debug("Player {} update quest!", serverPlayer.getTabListDisplayName());

                playerQuestData.removeQuest(entity.getUUID());
                ModNetworking.sendToPlayer(new RemovePlayerQuestS2CPacket(entity.getUUID()), serverPlayer);

            }

            Player talkingPlayer = DialogSessionManager.getTalkingPlayer((Mob) entity);
            if (talkingPlayer != null){
                DialogSessionManager.stopDialog((Mob) entity);
                ModNetworking.sendToPlayer(new ClosePlayerGuiS2CPacket(), talkingPlayer);
            }

            ModNetworking.sendToTrackingEntity(new SyncEntityQuestDataS2CPacket(entity.getId(), entityQuestData.serializeNBT(), false), entity);
            ModNetworking.sendToAll(new UnlockNpcS2CPacket(entity.getUUID()));

            TheQuestForge.LOGGER.debug("New cycle");
        }
    }
}