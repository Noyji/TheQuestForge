package net.noyji.thequestforge.api.quest.action;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.task.AbstractTask;
import net.noyji.thequestforge.common.util.Util;
import net.noyji.thequestforge.config.ServerConfig;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.capability.player.PlayerQuestData;
import net.noyji.thequestforge.data.managers.QuestTemplateManager;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;
import net.noyji.thequestforge.data.template.QuestTemplate;
import net.noyji.thequestforge.network.ModNetworking;
import net.noyji.thequestforge.network.s2c.RemovePlayerQuestS2CPacket;

public class QuestCompleteAction extends AbstractAction{
    @Override
    public boolean handler(ActionContext context) {
        Player player = context.getPlayer();

        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        PlayerQuestData playerQuestData = CapabilityUtil.getPlayerQuestData(serverPlayer);

        PlayerQuest quest = playerQuestData.getQuest(context.getEntity().getUUID());
        if (quest == null) return false;

        if (quest.isComplete()) {
            for (AbstractTask<?> task : quest.getTasks()){
                task.inComplete(serverPlayer);
            }
            playerQuestData.removeQuest(quest.getId());

            ModNetworking.sendToPlayer(new RemovePlayerQuestS2CPacket(quest.getId()), serverPlayer);

            for (ItemStack targetStack : quest.getRewards()){
               ItemHandlerHelper.giveItemToPlayer(player, targetStack);
            }

            if (ServerConfig.GIVE_OUT_EXPERIENCE.get()){
                player.giveExperiencePoints(quest.getXp());
            }

            if (ServerConfig.GIVE_OUT_CURRENCY.get()){
                ResourceLocation location = ResourceLocation.parse(ServerConfig.CURRENCY_ID.get());
                Item currency = Util.getItem(location);
                ItemStack currencyStack = new ItemStack(currency, quest.getCurrency());
                ItemHandlerHelper.giveItemToPlayer(player, currencyStack);
            }
            return true;
        }
        return false;
    }

    @Override
    public ResourceLocation getLocation() {
        return TheQuestForge.id("complete");
    }

    private boolean hasNextQuest(PlayerQuest playerQuest){
        QuestTemplate template = QuestTemplateManager.INSTANCE.getQuestTemplate(playerQuest.getSourceTemplate());
        return template.getNextQuest() == null;
    }
}
