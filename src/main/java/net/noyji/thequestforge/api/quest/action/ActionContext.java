package net.noyji.thequestforge.api.quest.action;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.noyji.thequestforge.data.template.components.TemplateDialogButton;

public class ActionContext {
    private Player player;
    private Entity entity;
    private TemplateDialogButton button;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public TemplateDialogButton getButton() {
        return button;
    }

    public void setButton(TemplateDialogButton button) {
        this.button = button;
    }
}
