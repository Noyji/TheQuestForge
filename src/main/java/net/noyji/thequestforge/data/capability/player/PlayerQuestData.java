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
import net.noyji.thequestforge.network.ModNetworking;
import net.noyji.thequestforge.network.s2c.SyncSpecificPlayerQuestS2CPacket;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerQuestData {
    private final Map<UUID, PlayerQuest> playerQuestMap = new HashMap<>();
    // --- ResourceLocation(thequestforge:collect) --- ResourceLocation(minecraft:pig) --- questId
    private final Map<ResourceLocation, Map<ResourceLocation, List<UUID>>> questCatalog = new HashMap<>();

    //TODO:
    public int sizeQuest(){
        return playerQuestMap.size();
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
            quest.updateTask(taskTypeKey, target, event);

            if (player instanceof ServerPlayer serverPlayer) {
                ModNetworking.sendToPlayer(new SyncSpecificPlayerQuestS2CPacket(uuid, quest.serializeNBT()), serverPlayer);
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

        return save;
    }

    public void deserializeNBT(@NotNull CompoundTag nbt) {
        this.playerQuestMap.clear();
        this.questCatalog.clear();

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
    }


    private void removeUUID (ResourceLocation taskKey, ResourceLocation targetKey, UUID uuidToRemove){
        Map<ResourceLocation, List<UUID>> innerMap = questCatalog.get(targetKey);
        if (innerMap == null){
            return;
        }

        List<UUID> uuids = innerMap.get(targetKey);
        if (uuids == null){
            return;
        }

        if (uuids.remove(uuidToRemove)){
            if (uuids.isEmpty()){
                innerMap.remove(targetKey);
            }
            if (innerMap.isEmpty()){
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
