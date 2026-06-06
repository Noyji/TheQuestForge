package net.noyji.thequestforge.api.client.placeholder;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;
import org.jetbrains.annotations.Nullable;

public class NpcNamePlaceholder implements IPlaceholderResolver {
    @Override
    public String resolve(Player player, @Nullable Entity entity, PlayerQuest quest, String[] args) {
        String arg = "null";
        if (args.length != 0){
            arg = args[0];
        }
        if (entity != null) {
            if (arg.equals("no_hide")) {
                return entity.hasCustomName() ? entity.getCustomName().getString() : entity.getName().getString();
            } else {
                return entity.hasCustomName() ? entity.getCustomName().getString() : "";
            }
        } else {
            return quest.getGiverData().getName();
        }
    }
}
