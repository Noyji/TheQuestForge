package net.noyji.thequestforge.api.quest.task;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.common.util.Util;
import net.noyji.thequestforge.data.group.components.JsonTask;
import net.noyji.thequestforge.data.quest.player.components.QuestRarity;

public class KillTask extends AbstractTask<LivingDeathEvent> {
    private EntityType<?> target;
    private CompoundTag tag;
    private int progress = 0;

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

    public EntityType<?> getEntity(){
        return target;
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
    public void inComplete(Player player) {
    }

    @Override
    public void handle(LivingDeathEvent event) {
        if (progress >= goal) return;

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
    public int getProgress() {
        return progress;
    }

    @Override
    public void info() {
        TheQuestForge.LOGGER.debug("{}: Target: {} Tag: {} Goal: {}", getLocation(), getTarget(), tag, goal);
    }

    @Override
    public String getTargetName() {
        return goal + " " + target.getDescription().getString();
    }

    @Override
    public void serializeNBT(CompoundTag nbt) {
        nbt.putInt("Goal", this.goal);
        nbt.putInt("Progress", this.progress);

        if (this.target != null) {
            ResourceLocation entityId = Util.getEntityResourceLocation(target);
            if (entityId == null) entityId = ResourceLocation.parse("minecraft:pig");
            nbt.putString("Target", entityId.toString());
        }

        if (this.tag != null) {
            nbt.put("Tag", this.tag);
        }
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.goal = nbt.getInt("Goal");
        this.progress = nbt.getInt("Progress");

        if (nbt.contains("Target")) {
            ResourceLocation entityId = ResourceLocation.parse(nbt.getString("Target"));
            this.target = Util.getEntityType(entityId);
        }

        if (nbt.contains("Tag")) {
            this.tag = nbt.getCompound("Tag");
        }
    }
}
