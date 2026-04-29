package net.noyji.thequestforge.data.template.components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateDialogButton {
    private Map<String, List<String>> text;
    private List<SimplyItemStack> give;
    private List<SimplyItemStack> remove;
    private String function;
    private String toGo;
    private String altToGo;
    private List<String> actions;

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        nbt.putString("function", this.function != null ? this.function : "");
        nbt.putString("toGo", this.toGo != null ? this.toGo : "");
        nbt.putString("altToGo", this.altToGo != null ? this.altToGo : "");

        if (this.actions != null && !this.actions.isEmpty()) {
            ListTag actionsTag = new ListTag();
            for (String action : this.actions) {
                actionsTag.add(StringTag.valueOf(action));
            }
            nbt.put("actions", actionsTag);
        }

        if (this.give != null && !this.give.isEmpty()) {
            ListTag giveTag = new ListTag();
            for (SimplyItemStack item : this.give) {
                giveTag.add(item.serializeNBT());
            }
            nbt.put("give", giveTag);
        }

        if (this.remove != null && !this.remove.isEmpty()) {
            ListTag removeTag = new ListTag();
            for (SimplyItemStack item : this.remove) {
                removeTag.add(item.serializeNBT());
            }
            nbt.put("remove", removeTag);
        }

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

        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {

        this.function = nbt.getString("function");
        this.toGo = nbt.getString("toGo");
        this.altToGo = nbt.getString("altToGo");

        this.actions = new ArrayList<>();
        if (nbt.contains("actions", Tag.TAG_LIST)) {
            ListTag actionsTag = nbt.getList("actions", Tag.TAG_STRING);
            for (int i = 0; i < actionsTag.size(); i++) {
                this.actions.add(actionsTag.getString(i));
            }
        }

        this.give = new ArrayList<>();
        if (nbt.contains("give", Tag.TAG_LIST)) {
            ListTag giveTag = nbt.getList("give", Tag.TAG_COMPOUND);
            for (int i = 0; i < giveTag.size(); i++) {
                SimplyItemStack item = new SimplyItemStack();
                item.deserializeNBT(giveTag.getCompound(i));
                this.give.add(item);
            }
        }

        this.remove = new ArrayList<>();
        if (nbt.contains("remove", Tag.TAG_LIST)) {
            ListTag removeTag = nbt.getList("remove", Tag.TAG_COMPOUND);
            for (int i = 0; i < removeTag.size(); i++) {
                SimplyItemStack item = new SimplyItemStack();
                item.deserializeNBT(removeTag.getCompound(i));
                this.remove.add(item);
            }
        }

        this.text = new HashMap<>();
        if (nbt.contains("text", Tag.TAG_COMPOUND)) {
            CompoundTag textMapTag = nbt.getCompound("text");
            // Проходим по всем ключам, которые есть в этом CompoundTag
            for (String key : textMapTag.getAllKeys()) {
                ListTag stringListTag = textMapTag.getList(key, Tag.TAG_STRING);
                List<String> strings = new ArrayList<>();
                for (int i = 0; i < stringListTag.size(); i++) {
                    strings.add(stringListTag.getString(i));
                }
                this.text.put(key, strings);
            }
        }
    }
}
