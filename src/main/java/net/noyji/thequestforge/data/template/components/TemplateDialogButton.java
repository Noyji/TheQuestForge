package net.noyji.thequestforge.data.template.components;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.action.AbstractAction;
import net.noyji.thequestforge.api.quest.action.ActionContext;
import net.noyji.thequestforge.api.quest.registry.ActionRegistry;
import net.noyji.thequestforge.common.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateDialogButton {
    private Map<String, List<String>> text;
    private List<SimplyItemStack> give;
    private List<SimplyItemStack> remove;
    private List<String> functions;
    private String toGo;
    private String altToGo;
    private List<String> actions;

    public void runActions(ActionContext context){
        for (String action : actions){

            ResourceLocation loc = TheQuestForge.parse(action);
            AbstractAction abstractAction = ActionRegistry.getAction(loc);

            if (abstractAction == null) {
                TheQuestForge.LOGGER.debug("Action : {} not found", action);
                continue;
            }
            TheQuestForge.LOGGER.debug("Action : {} running!", action);
            if (!abstractAction.handler(context)){
                TheQuestForge.LOGGER.debug("Action failed.");
            }
        }
    }

    public void runFunctions(ServerPlayer serverPlayer){
        if (functions == null || functions.isEmpty()) {
            TheQuestForge.LOGGER.debug("function is null or empty");
            return;
        }

        MinecraftServer server = serverPlayer.getServer();
        if (server == null) return;

        ServerFunctionManager functionManager = server.getFunctions();
        CommandSourceStack sourceStack = serverPlayer.createCommandSourceStack().withSuppressedOutput();
        for (String function : functions){
            ResourceLocation functionId = ResourceLocation.parse(function);

            functionManager.get(functionId).ifPresentOrElse(
                    commandFunction -> {
                        functionManager.execute(commandFunction, sourceStack);
                        TheQuestForge.LOGGER.debug("Function {} executed successfully for {}", functionId, serverPlayer);
                    },
                    () -> TheQuestForge.LOGGER.warn("Функция {} не найдена на сервере!", functionId)
            );
        }
    }

    public boolean hasItemToGive(){
        return give != null && !give.isEmpty();
    }

    public List<ItemStack> getGiveItems(){
        List<ItemStack> result = new ArrayList<>();
        for (SimplyItemStack simplyItemStack : give){
            ItemStack stack = simplyItemStack.toItem();

            if (stack == null || stack.isEmpty()) continue;
            result.add(stack);
        }
        return result;
    }

    public boolean hasItemToRemove(){
        return remove != null && !remove.isEmpty();
    }

    public List<ItemStack> getRemoveItem(){
        List<ItemStack> result = new ArrayList<>();
        for (SimplyItemStack simplyItemStack : remove){
            ItemStack stack = simplyItemStack.toItem();

            if (stack == null || stack.isEmpty()) continue;
            result.add(stack);
        }
        return result;
    }

    public boolean hasActions(){
        List<String> result = new ArrayList<>(actions);
        result.remove("thequestforge:close");
        return !result.isEmpty();
    }

    public boolean hasFunctions(){
        return functions != null && !functions.isEmpty();
    }

    public boolean hasAction(String action){
        if (action == null || action.isEmpty()) return false;
        return actions.contains(action);
    }

    public String getTranslateText(String languageKey, int nameIndex){
        return Util.getTranslateTextFromMap(text, languageKey, nameIndex);
    }

    public List<ResourceLocation> getFunctions(){
        List<ResourceLocation> result = new ArrayList<>();
        for (String function : functions){
            result.add(TheQuestForge.parse(function));
        }
        return result;
    }

    public int getTextIndex(@NotNull RandomSource randomSource){
        int limit = text.get("en_us").size();
        return randomSource.nextInt(0, limit);
    }

    public String getToGo() {
        return toGo;
    }

    public String getAltToGo() {
        return altToGo;
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();


        nbt.putString("toGo", this.toGo != null ? this.toGo : "");
        nbt.putString("altToGo", this.altToGo != null ? this.altToGo : "");

        if (this.functions != null && !this.functions.isEmpty()){
            ListTag functionTag = new ListTag();
            for (String function : this.functions){
                functionTag.add(StringTag.valueOf(function));
            }
            nbt.put("functions", functionTag);
        }

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

    public void deserializeNBT(@NotNull CompoundTag nbt) {

        this.toGo = nbt.getString("toGo");
        this.altToGo = nbt.getString("altToGo");

        this.functions = new ArrayList<>();
        if (nbt.contains("functions", Tag.TAG_LIST)) {
            ListTag functionsTag = nbt.getList("functions", Tag.TAG_STRING);
            for (int i = 0; i < functionsTag.size(); i++) {
                this.functions.add(functionsTag.getString(i));
            }
        }

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
