package net.noyji.thequestforge.data.managers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.common.util.Util;
import net.noyji.thequestforge.data.template.QuestTemplate;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.*;

public class QuestTemplateManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();

    public static final QuestTemplateManager INSTANCE = new QuestTemplateManager();
    public static final QuestTemplate EMPTY_TEMPLATE = new QuestTemplate();

    private final Map<String, List<QuestTemplate>> templatePoolMap = new HashMap<>();
    private final Map<ResourceLocation, QuestTemplate> templateMap = new HashMap<>();

    private static final Marker QUEST_TEMPLATES_MARKER = MarkerFactory.getMarker("QUEST_TEMPLATE_MANAGER");

    private static final RandomSource RANDOM = RandomSource.create();

    public QuestTemplateManager(){
        super(GSON, "quest_template");
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> jsonElementMap,
                         @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        templatePoolMap.clear();
        templateMap.clear();

        jsonElementMap.forEach((key, jsonElement) -> {
            try {
                QuestTemplate template = GSON.fromJson(jsonElement, QuestTemplate.class);

                if (template == null) return;
                template.setThisId(key.toString());
                if (template.hasPool()){
                    templatePoolMap.computeIfAbsent(template.getPool(), arr -> new ArrayList<>()).add(template);
                    templateMap.put(key, template);
                }

            } catch (Exception e) {
                TheQuestForge.LOGGER.warn(QUEST_TEMPLATES_MARKER, "Error reading file : {}", key, e);
            }
        });
        TheQuestForge.LOGGER.debug(QUEST_TEMPLATES_MARKER, "Total quest templates loaded: {}", templateMap.size());
    }

    public Set<String> getTemplatesIds(){
        Set<String> result = new HashSet<>();

        for (ResourceLocation id : templateMap.keySet()){
            result.add(id.toString());
        }
        return result;
    }

    public QuestTemplate getRandomQuestTemplate(String pool){
        if (pool == null || pool.isEmpty()) return null;
        if (!templatePoolMap.containsKey(pool)) return null;
        return Util.getWeightItem(templatePoolMap.get(pool), RANDOM);
    }

    public QuestTemplate getQuestTemplate(ResourceLocation templateKey){
        return templateMap.get(templateKey);
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        for (Map.Entry<ResourceLocation, QuestTemplate> entry : this.templateMap.entrySet()) {
            nbt.put(entry.getKey().toString(), entry.getValue().serializeNBT());
        }
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.templateMap.clear();
        for (String keyStr : nbt.getAllKeys()) {
            try {
                ResourceLocation resLoc = ResourceLocation.parse(keyStr);
                QuestTemplate template = new QuestTemplate();

                template.deserializeNBT(nbt.getCompound(keyStr));

                this.templateMap.put(resLoc, template);
            } catch (Exception e) {
                //Skip
            }
        }
    }
}
