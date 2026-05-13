package net.noyji.thequestforge.api.quest.action;

import net.minecraft.resources.ResourceLocation;
import net.noyji.thequestforge.TheQuestForge;

import java.util.UUID;

public class NextQuestAction extends AbstractAction{
    @Override
    public boolean handler(ActionContext context) {
        UUID questId =
        ResourceLocation templateId = context.getEntity().
        return false;
    }

    @Override
    public ResourceLocation getLocation() {
        return TheQuestForge.id("next_quest");
    }
}
