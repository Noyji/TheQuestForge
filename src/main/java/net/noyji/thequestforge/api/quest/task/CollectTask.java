package net.noyji.thequestforge.api.quest.task;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.common.util.Util;
import net.noyji.thequestforge.data.group.components.JsonTask;
import net.noyji.thequestforge.data.quest.player.components.QuestRarity;

public class CollectTask extends AbstractTask<TickEvent.PlayerTickEvent>{
    private ItemStack itemStack;

    public CollectTask() {
    }

    public ItemStack getItemStack(){
        return itemStack;
    }

    @Override
    public ResourceLocation getTarget() {
        return Util.getItemResourceLocation(itemStack);
    }

    @Override
    public ResourceLocation getLocation() {
        return TheQuestForge.id("collect");
    }

    @Override
    public Class<TickEvent.PlayerTickEvent> getEventClass() {
        return TickEvent.PlayerTickEvent.class;
    }

    @Override
    public void parse(JsonTask jsonTask, RandomSource randomSource, QuestRarity rarity) {
        if (jsonTask == null) return;
        goal = jsonTask.getCount(randomSource, rarity);
        itemStack = Util.parseItemStack(jsonTask, goal, randomSource);
    }

    @Override
    public void inComplete(Player player) {
        Util.removeItemFromPlayer(player, itemStack, progress);
    }

    @Override
    public String getTargetName() {
        return itemStack.getCount() + " " +  itemStack.getDisplayName().getString().replace("[", "").replace("]", "");
    }

    @Override
    public void handle(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        progress = Util.countMatchingItems(player, itemStack);
    }

    @Override
    public void info() {
        TheQuestForge.LOGGER.debug("{}: Target: {} Goal: {}", getLocation(), getTarget(), goal);
    }

    @Override
    public void serializeNBT(CompoundTag nbt) {
        nbt.putInt("Goal", this.goal);
        nbt.putInt("Count", this.progress);

        CompoundTag itemTag = new CompoundTag();
        if (this.itemStack != null && !this.itemStack.isEmpty()) {
            this.itemStack.save(itemTag);
        }
        nbt.put("ItemStack", itemTag);
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.goal = nbt.getInt("Goal");
        this.progress = nbt.getInt("Count");

        if (nbt.contains("ItemStack")) {
            this.itemStack = ItemStack.of(nbt.getCompound("ItemStack"));
        } else {
            this.itemStack = ItemStack.EMPTY;
        }
    }
}
