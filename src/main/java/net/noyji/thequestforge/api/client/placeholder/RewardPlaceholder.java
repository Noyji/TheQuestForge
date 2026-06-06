package net.noyji.thequestforge.api.client.placeholder;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;
import org.jetbrains.annotations.Nullable;

public class RewardPlaceholder implements  IPlaceholderResolver{
    @Override
    public String resolve(Player player, @Nullable Entity entity, PlayerQuest quest, String[] args) {
        if (args.length == 0) return "Wrong target placeholder index";
        TheQuestForge.LOGGER.debug("reward");
        try {
            int rewardIndex = Integer.parseInt(args[0]) - 1;

            ItemStack stack = quest.getReward(rewardIndex);
            if (stack == null) return "";

            return " " + stack.getCount() + " " + stack.getDisplayName().getString()
                    .replace("[", "")
                    .replace("]", "")
                    + ",";
        } catch (Exception e) {
            return "Wrong number";
        }
    }
}
