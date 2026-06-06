package net.noyji.thequestforge.api.client.placeholder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;
import org.jetbrains.annotations.Nullable;

public class TimePlaceholder implements IPlaceholderResolver {
    @Override
    public String resolve(Player player, @Nullable Entity entity, PlayerQuest quest, String[] args) {
        if (quest.getTimeLimit() > -1){
            return Component.literal(quest.getTimeLimit() + " ").append(Component.translatable("dialog.thequestforge.direction_placeholder.days")).getString();
        } else {
            return "";
        }
    }
}
