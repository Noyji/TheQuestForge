package net.noyji.thequestforge.data.template.components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.noyji.thequestforge.common.util.Util;
import net.noyji.thequestforge.data.quest.entity.components.QuestDialog;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateDialog {
    private Map<String, List<String>> text = new HashMap<>();
    private List<TemplateDialogButton> buttons = new ArrayList<>();

    public String getTranslateText(String key, int textIndex){
        return Util.getTranslateTextFromMap(text, key, textIndex);
    }

    public List<TemplateDialogButton> getButtons() {
        return buttons;
    }

    @Nullable
    public QuestDialog toQuestDialog(RandomSource randomSource){
        if (text == null || text.isEmpty()) return null;

        int textIndex = randomSource.nextInt(0, text.get("en_us").size());
        List<Integer> result = new ArrayList<>();

        for (TemplateDialogButton templateDialogButton : buttons){
            result.add(templateDialogButton.getTextIndex(randomSource));
        }

        return new QuestDialog(textIndex, result);
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        if (this.text != null && !this.text.isEmpty()) {
            CompoundTag textMapTag = new CompoundTag();
            for (Map.Entry<String, List<String>> entry : this.text.entrySet()) {
                ListTag stringListTag = new ListTag();
                for (String str : entry.getValue()) {
                    stringListTag.add(StringTag.valueOf(str));
                }
                textMapTag.put(entry.getKey(), stringListTag);
            }
            nbt.put("text", textMapTag);
        }

        if (this.buttons != null && !this.buttons.isEmpty()) {
            ListTag buttonsTag = new ListTag();
            for (TemplateDialogButton button : this.buttons) {
                buttonsTag.add(button.serializeNBT());
            }
            nbt.put("buttons", buttonsTag);
        }

        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.text = new HashMap<>();
        if (nbt.contains("text", Tag.TAG_COMPOUND)) {
            CompoundTag textMapTag = nbt.getCompound("text");
            for (String key : textMapTag.getAllKeys()) {
                ListTag stringListTag = textMapTag.getList(key, Tag.TAG_STRING);
                List<String> strings = new ArrayList<>();
                for (int i = 0; i < stringListTag.size(); i++) {
                    strings.add(stringListTag.getString(i));
                }
                this.text.put(key, strings);
            }
        }

        this.buttons = new ArrayList<>();
        if (nbt.contains("buttons", Tag.TAG_LIST)) {
            ListTag buttonsTag = nbt.getList("buttons", Tag.TAG_COMPOUND);
            for (int i = 0; i < buttonsTag.size(); i++) {
                TemplateDialogButton button = new TemplateDialogButton();
                button.deserializeNBT(buttonsTag.getCompound(i));
                this.buttons.add(button);
            }
        }
    }
}
