package net.noyji.thequestforge.api.quest.task;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.eventbus.api.Event;
import net.noyji.thequestforge.data.group.components.JsonTask;
import net.noyji.thequestforge.data.quest.player.components.QuestRarity;

public abstract class AbstractTask<T extends Event> {

    public abstract ResourceLocation getTarget();

    public abstract ResourceLocation getLocation();

    public abstract boolean isComplete();

    public abstract Class<T> getEventClass();

    public abstract void parse(JsonTask jsonTask, RandomSource randomSource, QuestRarity rarity);

    public abstract void handle(T event);

    public abstract int getGoal();

    public abstract int getProgress();

    public abstract void serializeNBT(CompoundTag nbt);

    public abstract void deserializeNBT(CompoundTag nbt);
    //
    public abstract void info();

    public void tryHandle(Event genericEvent){
        if (getEventClass().isInstance(genericEvent)){
            handle(getEventClass().cast(genericEvent));
        }
    }

    public boolean taskIs(ResourceLocation key, ResourceLocation target){
        return key.equals(getLocation()) && target.equals(getTarget());
    }

}
