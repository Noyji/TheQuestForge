package net.noyji.thequestforge.api.quest.task;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.common.util.Util;
import net.noyji.thequestforge.data.group.components.JsonTask;
import net.noyji.thequestforge.data.quest.player.components.QuestRarity;

public class KillTask extends AbstractTask<LivingDeathEvent> {
    private EntityType<?> target;
    private CompoundTag tag;
    private int progress = 0;
    private int goal;

    public KillTask() {
    }

    public KillTask(EntityType<?> target, CompoundTag tag, int goal) {
        this.target = target;
        this.tag = tag;
        this.goal = goal;
    }

    public KillTask(EntityType<?> target, int goal) {
        this.target = target;
        this.tag = null;
        this.goal = goal;
    }

    @Override
    public ResourceLocation getTarget() {
        return Util.getEntityResourceLocation(target);
    }

    @Override
    public ResourceLocation getLocation() {
        return TheQuestForge.id("kill");
    }

    @Override
    public boolean isComplete() {
        return progress >= goal;
    }

    @Override
    public Class<LivingDeathEvent> getEventClass() {
        return LivingDeathEvent.class;
    }

    @Override
    public void parse(JsonTask jsonTask, RandomSource randomSource, QuestRarity rarity) {
        target = Util.getEntityType(jsonTask.getTarget());

        tag = jsonTask.getTag();
        goal = jsonTask.getCount(randomSource, rarity);
    }

    @Override
    public void handle(LivingDeathEvent event) {
        if (tag == null || tag.isEmpty()){
            if (target == event.getEntity().getType()) {

                progress++;
            }
        } else {
            if (!(target == event.getEntity().getType())) return;
            CompoundTag killedTag = new CompoundTag();
            event.getEntity().saveWithoutId(killedTag);

            if (NbtUtils.compareNbt(tag, killedTag, true)){

                progress++;
            }
        }
    }

    @Override
    public void info() {
        TheQuestForge.LOGGER.debug("{}: Target: {} Tag: {} Goal: {}", getLocation(), getTarget(), tag, goal);
    }

    @Override
    public void serializeNBT(CompoundTag nbt) {

    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }
}
