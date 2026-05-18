package net.noyji.thequestforge.api.client.placeholder;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.noyji.thequestforge.data.quest.entity.Quest;

@FunctionalInterface
public interface IPlaceholderResolver {
    String resolve(Player player, Entity entity, Quest quest, String[] args);
}
