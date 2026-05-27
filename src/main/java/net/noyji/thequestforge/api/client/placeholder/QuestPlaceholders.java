package net.noyji.thequestforge.api.client.placeholder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.client.registry.PlaceholderRegistry;
import net.noyji.thequestforge.api.quest.task.AbstractTask;

public class QuestPlaceholders{

    public static void register(){
        PlaceholderRegistry.register("player", (player, entity, quest, args)
                -> player.getName().getString());

        PlaceholderRegistry.register("npc_name", (player, entity, quest, args) -> {
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
        });

        PlaceholderRegistry.register("target", (player, entity, quest, args) -> {
            if (args.length == 0) return "Wrong target placeholder index";
            try {
                int targetIndex = Integer.parseInt(args[0]) - 1;

                AbstractTask<?> task = quest.getTask(targetIndex);
                if (task == null) return "";

                return " " + task.getTargetName() + ",";
            } catch (NumberFormatException e) {
                return "Wrong number";
            }
        });

        PlaceholderRegistry.register("reward", (player, entity, quest, args) -> {
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
        });

        PlaceholderRegistry.register("direction", (player, entity, quest, args) -> {
            String direction = quest.getCustomData("direction");
            return (direction == null) ? "Direction not found" : Component.translatable(direction).getString();
        });

        PlaceholderRegistry.register("time", (player, entity, quest, args) -> {
            if (quest.getTimeLimit() > -1){
                return Component.literal(quest.getTimeLimit() + " ").append(Component.translatable("dialog.thequestforge.direction_placeholder.days")).getString();
            } else {
                return "";
            }
        });
    }
}
