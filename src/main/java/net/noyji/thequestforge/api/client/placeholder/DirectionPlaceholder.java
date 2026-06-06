package net.noyji.thequestforge.api.client.placeholder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;
import org.jetbrains.annotations.Nullable;

public class DirectionPlaceholder implements IPlaceholderResolver{
    @Override
    public String resolve(Player player, @Nullable Entity entity, PlayerQuest quest, String[] args) {
        String direction = quest.getCustomData("direction");
        return (direction == null) ? "Direction not found" : Component.translatable(direction).getString();
    }
}
