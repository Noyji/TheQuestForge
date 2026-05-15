package net.noyji.thequestforge.common.events.custom;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;
import net.noyji.thequestforge.data.quest.entity.Quest;

public class QuestAcceptEvent extends Event {
    private final Player player;
    private final Quest quest;

    public QuestAcceptEvent(Player player, Quest quest) {
        this.player = player;
        this.quest = quest;
    }

    public Player getPlayer() {
        return player;
    }

    public Quest getQuest() {
        return quest;
    }
}
