package net.noyji.thequestforge.data.quest.entity.components;

import net.minecraft.nbt.CompoundTag;
import net.noyji.thequestforge.TheQuestForge;

import java.util.ArrayList;
import java.util.List;

public class QuestDialog {
    private int textIndex = 0;
    private List<Integer> buttons = new ArrayList<>();

    public QuestDialog() {
    }

    public QuestDialog(int textIndex, List<Integer> buttons) {
        this.textIndex = textIndex;
        this.buttons = buttons;
    }

    public List<Integer> getButtonIndices() {
        return buttons;
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
        save.putInt("text_index", this.textIndex);

        if (this.buttons != null) {
            save.putIntArray("buttons", this.buttons);
        }

        return save;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.textIndex = nbt.getInt("text_index");

        this.buttons = new ArrayList<>();
        if (nbt.contains("buttons")) {
            int[] buttonsArray = nbt.getIntArray("buttons");
            for (int buttonIndex : buttonsArray) {
                this.buttons.add(buttonIndex);
            }
        }
    }
}
