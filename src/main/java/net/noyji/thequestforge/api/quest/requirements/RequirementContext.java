package net.noyji.thequestforge.api.quest.requirements;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.data.quest.player.components.QuestRarity;

import java.util.Map;

public class RequirementContext {
    private final Player player;
    private final Entity entity;
    private final QuestRarity rarity;
    private final Map<String, String> customData;
    private String value;

    public RequirementContext(Player player, Entity entity, QuestRarity rarity, Map<String, String> customData) {
        this.player = player;
        this.entity = entity;
        this.rarity = rarity;
        this.customData = customData;
    }

    public Player getPlayer() {
        return player;
    }

    public Entity getEntity() {
        return entity;
    }

    public QuestRarity getRarity() {
        return rarity;
    }

    public String getStringValue() {
        return value;
    }

    public ResourceLocation getResourceLocationValue(){
        return ResourceLocation.parse(value);
    }

    public int getIntValue(){
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            TheQuestForge.LOGGER.info("Error while extracting number from context", e);
            return 1;
        }
    }

    public void addCustomData(String key, String data) {
        customData.put(key, data);
    }

    public String getCustomData(String key){
        return customData.get(key);
    }

    public boolean getBooleanValue(){
        return Boolean.parseBoolean(value);
    }

    public void setValue(String value) {
        this.value = value.toLowerCase().trim();
    }
}
