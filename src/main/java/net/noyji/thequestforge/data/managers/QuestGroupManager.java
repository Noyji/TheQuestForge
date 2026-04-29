package net.noyji.thequestforge.data.managers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.data.group.Group;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestGroupManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    public static final QuestGroupManager INSTANCE = new QuestGroupManager();
    private final Map<ResourceLocation, Group> groupMap = new HashMap<>();

    private static final Marker QUEST_GROUP_MARKER = MarkerFactory.getMarker("QUEST_GROUP_MANAGER");

    private static final RandomSource RANDOM = RandomSource.create();

    public QuestGroupManager(){
        super(GSON, "groups");
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> jsonElementMap,
                         @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        groupMap.clear();

        jsonElementMap.forEach((key, jsonElement) -> {
            try {
                Group group = GSON.fromJson(jsonElement, Group.class);

                if (group != null) {
                    groupMap.put(key, group);
                }

            } catch (Exception e) {
                TheQuestForge.LOGGER.warn(QUEST_GROUP_MARKER, "Error reading file : {}", key, e);
            }
        });

        TheQuestForge.LOGGER.debug(QUEST_GROUP_MARKER, "Total group loaded: {}", groupMap.size());
    }

    public Group getGroup(ResourceLocation groupKey){
        return groupMap.get(groupKey);
    }

    public Group getRandomGroup(){
        if (groupMap.isEmpty()) return null;

        List<ResourceLocation> keys = groupMap.keySet().stream().toList();
        ResourceLocation key = keys.get(RANDOM.nextInt(keys.size()));
        return groupMap.get(key);
    }
}
