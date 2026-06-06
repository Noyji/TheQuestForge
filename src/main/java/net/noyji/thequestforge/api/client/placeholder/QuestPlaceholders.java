package net.noyji.thequestforge.api.client.placeholder;

import net.noyji.thequestforge.api.client.registry.PlaceholderRegistry;

public class QuestPlaceholders{

    public static void register(){
        PlaceholderRegistry.register("player", new PlayerPlaceholder());
        PlaceholderRegistry.register("npc_name", new NpcNamePlaceholder());
        PlaceholderRegistry.register("target", new TargetPlaceholder());
        PlaceholderRegistry.register("reward", new RewardPlaceholder());
        PlaceholderRegistry.register("direction", new DirectionPlaceholder());
        PlaceholderRegistry.register("time", new TimePlaceholder());
    }
}
