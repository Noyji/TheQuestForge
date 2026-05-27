package net.noyji.thequestforge.api.client.registry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.client.placeholder.IPlaceholderResolver;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderRegistry {
    private static final Map<String, IPlaceholderResolver> REGISTRY = new ConcurrentHashMap<>();
    private static final Pattern PLACEHOLDERS_PATTERN = Pattern.compile("%([^%]+)%");

    public static void register(String key, IPlaceholderResolver resolver){
        if (REGISTRY.containsKey(key)){
            TheQuestForge.LOGGER.warn("Placeholder '%{}%' is already registered!", key);
            return;
        }
        REGISTRY.put(key, resolver);
    }

    public static String parse(String text, Player player, Entity entity, PlayerQuest quest){
        if (text == null || text.isEmpty()) return text;

        Matcher matcher = PLACEHOLDERS_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()){
            String fullMatch = matcher.group(1);
            String[] parts = fullMatch.split("-");
            String key = parts[0];

            String[] args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, args.length);

            IPlaceholderResolver resolver = REGISTRY.get(key);

            if (resolver != null){
                String replacement = resolver.resolve(player, entity, quest, args);
                matcher.appendReplacement(result, replacement != null ? Matcher.quoteReplacement(replacement) : "");
            } else {
                matcher.appendReplacement(result, "%" + fullMatch + "%");
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
