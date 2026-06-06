package net.noyji.thequestforge.api.client.placeholder;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.noyji.thequestforge.api.quest.task.AbstractTask;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;
import org.jetbrains.annotations.Nullable;

public class TargetPlaceholder implements IPlaceholderResolver{
    @Override
    public String resolve(Player player, @Nullable Entity entity, PlayerQuest quest, String[] args) {
        if (args.length == 0) return "Wrong target placeholder index";
        try {
            int targetIndex = Integer.parseInt(args[0]) - 1;

            AbstractTask<?> task = quest.getTask(targetIndex);
            if (task == null) return "";

            return " " + task.getTargetName() + ",";
        } catch (NumberFormatException e) {
            return "Wrong number";
        }
    }
}
