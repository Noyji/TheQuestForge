package net.noyji.thequestforge.data.capability.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;
import net.noyji.thequestforge.data.quest.player.components.QuestType;
import net.noyji.thequestforge.network.TheQuestForgeNetworking;
import net.noyji.thequestforge.network.s2c.RemovePlayerQuestS2CPacket;
import net.noyji.thequestforge.network.s2c.SyncSpecificPlayerQuestS2CPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerQuestData {
    private final Map<UUID, PlayerQuest> playerQuestMap = new HashMap<>();
    private final Map<UUID, Integer> npcChainProgress = new HashMap<>();
    // --- ResourceLocation(thequestforge:collect) --- ResourceLocation(minecraft:pig) --- questId
    private final Map<ResourceLocation, Map<ResourceLocation, List<UUID>>> questCatalog = new HashMap<>();
    private final Set<UUID> completedNpcQuests = new HashSet<>();

    private final DialogStage dialogStage = new DialogStage();
    private long lastResetCycle = 0;
    private long lastDayProcessed = -1;

    public void advanceChainProgress(UUID npcId) {
        int current = getChainProgress(npcId);
        npcChainProgress.put(npcId, current + 1);
    }

    public void updateQuestDays(Player player) {
        long currentTotalDays = player.level().getDayTime() / 24000L;

        if (currentTotalDays > lastDayProcessed) {
            lastDayProcessed = currentTotalDays;
            Set<UUID> questToRemove = new HashSet<>();

            TheQuestForge.LOGGER.info("Наступил новый день ({}). Обновляем лимиты времени у квестов!", currentTotalDays);

            for (PlayerQuest quest : playerQuestMap.values()) {
                int currentDays = quest.getTimeLimit();

                if (currentDays == -2) continue;

                if (currentDays > -1) {
                    quest.setTimeLimit(currentDays - 1);
                    TheQuestForgeNetworking.sendToPlayer(new SyncSpecificPlayerQuestS2CPacket(quest.getId(), quest.serializeNBT()), player);
                }

                if (quest.getTimeLimit() == -1) {
                   questToRemove.add(quest.getId());
                }
            }

            for (UUID uuid : questToRemove){
                removeQuest(uuid);
                lockNpc(uuid);
                TheQuestForgeNetworking.sendToPlayer(new RemovePlayerQuestS2CPacket(uuid), player);
            }
        }
    }

    public int getChainProgress(UUID uuid){
        return npcChainProgress.getOrDefault(uuid, 0);
    }

    public void reset(boolean all) {
        if (all){
            playerQuestMap.clear();
            questCatalog.clear();
            completedNpcQuests.clear();
            dialogStage.clear();
        } else {
            List<UUID> uuidsToRemove = new ArrayList<>();

            for (Map.Entry<UUID, PlayerQuest> entry : playerQuestMap.entrySet()) {
                if (entry.getValue().getType() != QuestType.STORY) {
                    uuidsToRemove.add(entry.getKey());
                }
            }

            if (uuidsToRemove.isEmpty()) return;

            for (UUID uuid : uuidsToRemove) {
                playerQuestMap.remove(uuid);
            }

            for (Map<ResourceLocation, List<UUID>> innerMap : questCatalog.values()) {

                for (List<UUID> uuidList : innerMap.values()) {
                    uuidList.removeAll(uuidsToRemove);
                }
                innerMap.values().removeIf(List::isEmpty);
            }
            questCatalog.values().removeIf(Map::isEmpty);
        }
    }

    public void clearChain(){
        npcChainProgress.clear();
    }

    public long getLastResetCycle() {
        return lastResetCycle;
    }

    public void setLastResetCycle(long lastResetCycle) {
        this.lastResetCycle = lastResetCycle;
    }

    public boolean isNpcLocked(UUID uuid){
        return completedNpcQuests.contains(uuid);
    }

    public void lockNpc(@NotNull UUID uuid){
        completedNpcQuests.add(uuid);
    }

    public void unlockAllNpc(){
        completedNpcQuests.clear();
    }

    public void unlockNpc(@NotNull UUID uuid){
        completedNpcQuests.remove(uuid);
    }

    //TODO:
    public int sizeQuest(){
        return playerQuestMap.size();
    }

    public void debugInfoCatalog() {
        TheQuestForge.LOGGER.info("=== QUEST CATALOG DEBUG ===");

        if (this.questCatalog.isEmpty()) {
            TheQuestForge.LOGGER.info("Каталог пуст.");
            TheQuestForge.LOGGER.info("===========================");
            return;
        }

        for (Map.Entry<ResourceLocation, Map<ResourceLocation, List<UUID>>> outerEntry : this.questCatalog.entrySet()) {
            ResourceLocation taskType = outerEntry.getKey(); // Например: thequestforge:kill
            Map<ResourceLocation, List<UUID>> innerMap = outerEntry.getValue();

            TheQuestForge.LOGGER.info("Тип задачи: [{}]", taskType);

            if (innerMap.isEmpty()) {
                TheQuestForge.LOGGER.info("  -> Нет зарегистрированных целей.");
                continue;
            }

            for (Map.Entry<ResourceLocation, List<UUID>> innerEntry : innerMap.entrySet()) {
                ResourceLocation target = innerEntry.getKey(); // Например: minecraft:zombie
                List<UUID> uuids = innerEntry.getValue();

                TheQuestForge.LOGGER.info("  Цель: [{}]", target);

                if (uuids.isEmpty()) {
                    TheQuestForge.LOGGER.info("    -> Пустой список (возможна утечка, список должен удаляться!)");
                } else {
                    TheQuestForge.LOGGER.info("    -> Активные квесты ({} шт.):", uuids.size());
                    for (UUID uuid : uuids) {
                        TheQuestForge.LOGGER.info("       - {}", uuid.toString());
                    }
                }
            }
        }
        TheQuestForge.LOGGER.info("===========================");
    }
    @Nullable
    public Map<ResourceLocation, List<UUID>> getTargetsCatalog(ResourceLocation taskKey){
        return questCatalog.get(taskKey);
    }

    public void checkCollectTasks(Player player, Event event) {
        ResourceLocation collectKey = TheQuestForge.id("collect");

        Map<ResourceLocation, List<UUID>> collectTargets = questCatalog.get(collectKey);

        if (collectTargets == null || collectTargets.isEmpty()) {
            return;
        }

        for (ResourceLocation targetItem : collectTargets.keySet()) {
            progressUpdate(collectKey, targetItem, event, player);
        }
    }

    @Nullable
    public PlayerQuest getQuest(UUID uuid){
        return playerQuestMap.get(uuid);
    }

    public void debugInfo(){
        for (Map.Entry<ResourceLocation, Map<ResourceLocation, List<UUID>>> entry : questCatalog.entrySet()){
            for (Map.Entry<ResourceLocation, List<UUID>> listEntry : entry.getValue().entrySet()){
                for (UUID uuids : listEntry.getValue()){
                    TheQuestForge.LOGGER.debug("Quest task: {}, target: {}, uuid: {}", entry.getKey(), listEntry.getKey(), uuids);
                }
            }
        }
    }

    public void putDialogProgress(UUID uuid, String stage){
        dialogStage.put(uuid, stage);
    }

    public String getDialogProgress(UUID uuid){
        return dialogStage.get(uuid);
    }

    public void setSpareDialogStage(UUID uuid, String stage){
        PlayerQuest quest = playerQuestMap.get(uuid);
        if (quest == null) return;

        quest.setSpareDialogKey(stage);
    }

    public String getSpareDialogStage(UUID uuid){
        PlayerQuest quest = playerQuestMap.get(uuid);
        if (quest == null) return "start";

        return quest.getSpareDialogKey();
    }

    public List<PlayerQuest> getQuests(){
        return playerQuestMap.values().stream().toList();
    }

    public boolean hasQuest(UUID uuid){
        return playerQuestMap.containsKey(uuid);
    }

    public boolean isQuestComplete(UUID id){
        PlayerQuest playerQuest = playerQuestMap.get(id);
        if (playerQuest == null) return false;
        return playerQuest.isComplete();
    }

    public void progressUpdate(ResourceLocation taskTypeKey, ResourceLocation target, Event event, Player player){
        if (questCatalog.isEmpty()) return;
        if (questCatalog.get(taskTypeKey) == null || questCatalog.get(taskTypeKey).isEmpty()) return;
        if (questCatalog.get(taskTypeKey).get(target) == null || questCatalog.get(taskTypeKey).get(target).isEmpty()) return;

        List<UUID> uuids = questCatalog.get(taskTypeKey).get(target);
        for (UUID uuid : uuids){
            PlayerQuest quest = playerQuestMap.get(uuid);
            quest.updateTask(taskTypeKey, target, event, player);

            if (player instanceof ServerPlayer serverPlayer) {
                TheQuestForgeNetworking.sendToPlayer(new SyncSpecificPlayerQuestS2CPacket(uuid, quest.serializeNBT()), serverPlayer);
            }
        }
    }

    public void updateQuest(UUID questId, CompoundTag data){
        PlayerQuest quest = playerQuestMap.get(questId);
        if (quest == null) return;

        quest.deserializeNBT(data);
    }

    public void removeQuest(UUID uuid){
        if (!hasQuest(uuid)) return;
        List<String> locationAndTarget = playerQuestMap.get(uuid).getLocationAndTarget();

        dialogStage.remove(uuid);

        for (String string : locationAndTarget){
            String[] values = string.split("-", 2);
            removeUUID(ResourceLocation.parse(values[0]), ResourceLocation.parse(values[1]), uuid);
        }
        playerQuestMap.remove(uuid);
    }

    public void addQuest(PlayerQuest playerQuest){
        if (playerQuest == null || playerQuestMap.containsKey(playerQuest.getId())) return;

        playerQuestMap.put(playerQuest.getId(), playerQuest);
        List<String> locationAndTarget = playerQuest.getLocationAndTarget();

        for (String string : locationAndTarget){
            String[] values = string.split("-", 2);

            addUUID(ResourceLocation.parse(values[0]), ResourceLocation.parse(values[1]), playerQuest.getId());
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag save = new CompoundTag();

        save.putLong("LastResetCycle", lastResetCycle);
        save.putLong("LastDayProcessed", lastDayProcessed);

        CompoundTag playerQuestMapTag = new CompoundTag();
        for (Map.Entry<UUID, PlayerQuest> entry : this.playerQuestMap.entrySet()) {
            playerQuestMapTag.put(entry.getKey().toString(), entry.getValue().serializeNBT());
        }
        save.put("player_quest_map", playerQuestMapTag);

        CompoundTag catalogTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Map<ResourceLocation, List<UUID>>> outerEntry : this.questCatalog.entrySet()) {
            CompoundTag innerMapTag = new CompoundTag();
            for (Map.Entry<ResourceLocation, List<UUID>> innerEntry : outerEntry.getValue().entrySet()) {
                ListTag uuidListTag = new ListTag();
                for (UUID uuid : innerEntry.getValue()) {
                    uuidListTag.add(StringTag.valueOf(uuid.toString()));
                }
                innerMapTag.put(innerEntry.getKey().toString(), uuidListTag);
            }
            catalogTag.put(outerEntry.getKey().toString(), innerMapTag);
        }
        save.put("quest_catalog", catalogTag);

        ListTag lockList = new ListTag();
        for (UUID uuid : completedNpcQuests) {
            lockList.add(StringTag.valueOf(uuid.toString()));
        }
        save.put("LockedNpcs", lockList);

        CompoundTag chainProgressTag = new CompoundTag();
        for (Map.Entry<UUID, Integer> entry : this.npcChainProgress.entrySet()) {
            chainProgressTag.putInt(entry.getKey().toString(), entry.getValue());
        }
        save.put("npc_chain_progress", chainProgressTag);

        save.put("dialog_stage", dialogStage.serializeNBT());

        return save;
    }

    public void deserializeNBT(@NotNull CompoundTag nbt) {
        this.playerQuestMap.clear();
        this.questCatalog.clear();
        this.completedNpcQuests.clear();
        this.npcChainProgress.clear();

        this.lastResetCycle = nbt.getLong("LastResetCycle");
        this.lastDayProcessed = nbt.getLong("LastDayProcessed");

        if (nbt.contains("player_quest_map", Tag.TAG_COMPOUND)) {
            CompoundTag playerQuestMapTag = nbt.getCompound("player_quest_map");
            for (String key : playerQuestMapTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    PlayerQuest quest = new PlayerQuest();
                    quest.deserializeNBT(playerQuestMapTag.getCompound(key));
                    this.playerQuestMap.put(uuid, quest);
                } catch (IllegalArgumentException e) {
                    //Skip
                }
            }
        }

        if (nbt.contains("quest_catalog", Tag.TAG_COMPOUND)) {
            CompoundTag catalogTag = nbt.getCompound("quest_catalog");
            for (String outerKey : catalogTag.getAllKeys()) {
                ResourceLocation outerLocation = ResourceLocation.parse(outerKey);
                CompoundTag innerMapTag = catalogTag.getCompound(outerKey);

                Map<ResourceLocation, List<UUID>> innerMap = new HashMap<>();

                for (String innerKey : innerMapTag.getAllKeys()) {
                    ResourceLocation innerLocation = ResourceLocation.parse(innerKey);
                    ListTag uuidListTag = innerMapTag.getList(innerKey, Tag.TAG_STRING);

                    List<UUID> uuidList = new ArrayList<>();
                    for (int i = 0; i < uuidListTag.size(); i++) {
                        try {
                            uuidList.add(UUID.fromString(uuidListTag.getString(i)));
                        } catch (IllegalArgumentException e) {
                            //Skip
                        }
                    }
                    innerMap.put(innerLocation, uuidList);
                }
                this.questCatalog.put(outerLocation, innerMap);
            }
        }

        if (nbt.contains("LockedNpcs", Tag.TAG_LIST)) {
            ListTag lockList = nbt.getList("LockedNpcs", Tag.TAG_STRING);
            for (int i = 0; i < lockList.size(); i++) {
                completedNpcQuests.add(UUID.fromString(lockList.getString(i)));
            }
        }

        if (nbt.contains("npc_chain_progress", Tag.TAG_COMPOUND)) {
            CompoundTag chainProgressTag = nbt.getCompound("npc_chain_progress");
            for (String key : chainProgressTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    int progress = chainProgressTag.getInt(key);
                    this.npcChainProgress.put(uuid, progress);
                } catch (IllegalArgumentException e) {
                    //Skip
                }
            }
        }

        if (nbt.contains("dialog_stage", Tag.TAG_COMPOUND)) {
            dialogStage.deserializeNBT(nbt.getCompound("dialog_stage"));
        }
    }


    private void removeUUID(ResourceLocation taskKey, ResourceLocation targetKey, UUID uuidToRemove) {
        Map<ResourceLocation, List<UUID>> innerMap = questCatalog.get(taskKey);
        if (innerMap == null) {
            return;
        }

        List<UUID> uuids = innerMap.get(targetKey);
        if (uuids == null) {
            return;
        }

        if (uuids.remove(uuidToRemove)) {
            if (uuids.isEmpty()) {
                innerMap.remove(targetKey);
            }
            if (innerMap.isEmpty()) {
                questCatalog.remove(taskKey);
            }
        }
    }

    private void addUUID(ResourceLocation taskKey, ResourceLocation targetKey, UUID uuidToAdd){
        Map<ResourceLocation, List<UUID>> innreMap = questCatalog.computeIfAbsent(taskKey, k -> new HashMap<>());

        List<UUID> uuids = innreMap.computeIfAbsent(targetKey, k -> new ArrayList<>());

        uuids.add(uuidToAdd);
    }
}
