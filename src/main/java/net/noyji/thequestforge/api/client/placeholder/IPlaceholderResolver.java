package net.noyji.thequestforge.api.client.placeholder;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IPlaceholderResolver {
    String resolve(Player player, @Nullable Entity entity, PlayerQuest quest, String[] args);
}
