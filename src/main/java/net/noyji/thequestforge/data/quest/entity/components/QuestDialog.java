package net.noyji.thequestforge.data.quest.entity.components;

import net.minecraft.nbt.CompoundTag;
import net.noyji.thequestforge.TheQuestForge;

public class QuestDialog {
    private int textIndex = 0;
    private int buttons = 0;

    public QuestDialog() {
    }

    public QuestDialog(int textIndex, int buttons) {
        this.textIndex = textIndex;
        this.buttons = buttons;
    }

    public int getTextIndex(){
        return textIndex;
    }

    public void info(){
        TheQuestForge.LOGGER.debug("text index: {}", textIndex);
        TheQuestForge.LOGGER.debug("buttons: {}", buttons);
    }

    public CompoundTag serializeNBT() {
        CompoundTag save = new CompoundTag();
        save.putInt("text_index", textIndex);
        save.putInt("buttons", buttons);
        return save;
    }

    public void deserializeNBT(CompoundTag nbt) {
        textIndex = nbt.getInt("text_index");
        buttons = nbt.getInt("buttons");
    }
}
