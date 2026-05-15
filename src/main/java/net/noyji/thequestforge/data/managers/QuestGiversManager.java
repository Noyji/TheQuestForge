package net.noyji.thequestforge.data.managers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.registries.ForgeRegistries;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.common.util.Util;
import net.noyji.thequestforge.data.managers.components.QuestGiverJsonInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QuestGiversManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    public static final QuestGiversManager INSTANCE = new QuestGiversManager();
    private final Map<ResourceLocation, QuestGiverJsonInfo> entityPools = new HashMap<>();

    private static final Marker QUEST_GIVERS_MARKER = MarkerFactory.getMarker("QUEST_GIVERS_MANAGER");

    private static final RandomSource RANDOM = RandomSource.create();

    public QuestGiversManager() {
        super(GSON, "quest_givers");
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> jsonElementMap,
                         @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        entityPools.clear();

        jsonElementMap.forEach((key, jsonElement) -> {
            try {
                QuestGiverJsonInfo info = GSON.fromJson(jsonElement, QuestGiverJsonInfo.class);

                if (info.scan(QUEST_GIVERS_MARKER, key)){
                    entityPools.put(key, info);
                }

            } catch (Exception e) {
                TheQuestForge.LOGGER.warn(QUEST_GIVERS_MARKER, "Error reading file : {}", key, e);
            }
        });

        TheQuestForge.LOGGER.debug(QUEST_GIVERS_MARKER, "Total number of quest givers loaded: {}", entityPools.size());
    }

    public boolean hasEntity(ResourceLocation entityKey){
        return entityPools.containsKey(entityKey);
    }

    public boolean hasEntity(Entity entity){
        ResourceLocation entityKey = Util.getEntityResourceLocation(entity);
        return hasEntity(entityKey);
    }

    public Set<String> getAllGiverIds(){
        Set<String> result = new HashSet<>();

        result.add("minecraft:villager");

        for (ResourceLocation entityId : entityPools.keySet()){
            result.add(entityId.toString());
        }
        return result;
    }

    public boolean thisQuestGiverOrVillager(Entity entity){
        return hasEntity(entity) || entity instanceof Villager;
    }

    public boolean entityHasPool(ResourceLocation entityKey, String pool){
        return entityPools.get(entityKey).hasPool(pool);
    }

    public String getRandomPool(ResourceLocation entityKey){
        return entityPools.get(entityKey).getRandomPool(RANDOM);
    }

    public String getRandomPool(Entity entity){
        ResourceLocation entityKey = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return getRandomPool(entityKey);
    }


}
