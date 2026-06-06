package net.noyji.thequestforge.api.client.placeholder;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;
import org.jetbrains.annotations.Nullable;

public class PlayerPlaceholder implements IPlaceholderResolver{
    @Override
    public String resolve(Player player, @Nullable Entity entity, PlayerQuest quest, String[] args) {
        return player.getName().getString();
    }
}
