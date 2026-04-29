package net.noyji.thequestforge.data.quest.player.components;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class TargetContext {
    private final ItemStack itemTarget;
    private final Entity entityTarget;
    private final ResourceLocation resourceLocation;
    private Map<ResourceLocation, Object> customData;

    public TargetContext(ItemStack itemTarget, Entity entityTarget, ResourceLocation resourceLocation) {
        this.itemTarget = itemTarget;
        this.entityTarget = entityTarget;
        this.resourceLocation = resourceLocation;
    }

    public ItemStack getItemTarget() {
        return itemTarget;
    }

    public Entity getEntityTarget() {
        return entityTarget;
    }

    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }
}
