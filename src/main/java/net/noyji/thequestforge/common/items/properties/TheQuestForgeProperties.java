package net.noyji.thequestforge.common.items.properties;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.common.items.TheQuestForgeItems;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;
import net.noyji.thequestforge.data.quest.player.components.GiverData;

import java.util.UUID;
@Mod.EventBusSubscriber(modid = TheQuestForge.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TheQuestForgeProperties {

    private static final CompassWobble normalWobble = new CompassWobble();
    private static final CompassWobble randomWobble = new CompassWobble();
    @SubscribeEvent
    public static void register(FMLClientSetupEvent event){
        event.enqueueWork(() -> compassProperties(event));
    }

    private static void compassProperties(FMLClientSetupEvent event){
        ItemProperties.register(TheQuestForgeItems.QUEST_COMPASS.get(), ResourceLocation.parse("angle"),
                (ItemStack stack, ClientLevel level, LivingEntity entity, int seed) -> {

                    if (entity == null && !stack.isFramed()) return 0.0F;

                    Entity currentEntity = entity != null ? entity : stack.getFrame();

                    if (level == null && currentEntity.level() instanceof ClientLevel){
                        level = (ClientLevel) currentEntity.level();
                    }

                    if (level == null) return 0.0F;

                    CompoundTag tag = stack.getTag();
                    if (tag == null || !tag.hasUUID("giver_uuid")) return 0.0F;

                    UUID questUUID = tag.getUUID("giver_uuid");

                    if (!(currentEntity instanceof Player player)) return 0.0F;

                    PlayerQuest playerQuest = CapabilityUtil.getPlayerQuestData(player).getQuest(questUUID);
                    if (playerQuest == null) return 0.0F;

                    GiverData giverData = playerQuest.getGiverData();
                    if (giverData == null) return 0.0F;

                    ResourceKey<Level> targetDim = giverData.getDimension();
                    if (targetDim == null) return 0.0F;

                    if (!level.dimension().equals(targetDim)){
                        return randomWobble.update(level.getGameTime(), Math.random());
                    }

                    if (giverData.getGiverPos() == null) return 0.0F;

                    double targetX = giverData.getGiverPos().getX();
                    double targetZ = giverData.getGiverPos().getZ();

                    Entity npc = level.getEntity(giverData.getEntityId());
                    if (npc != null){
                        targetX = npc.getX();
                        targetZ = npc.getZ();
                    }

                    double d0 = targetX - currentEntity.getX();
                    double d1 = targetZ - currentEntity.getZ();
                    double targetAngle = (Math.atan2(d1, d0) + (Math.PI / 2.0D)) / (Math.PI * 2D);
                    double entityYaw = Mth.positiveModulo(currentEntity.getYRot() / 360.0F, 1.0D);

                    double finalAngle = Mth.positiveModulo(targetAngle - entityYaw, 1.0D);

                    return normalWobble.update(level.getGameTime(), finalAngle);
                });
    }

    private static class CompassWobble {
        double rotation;
        double deltaRotation;
        long lastUpdateTick;

        float update(long gameTime, double targetAngle) {
            if (this.lastUpdateTick != gameTime) {
                this.lastUpdateTick = gameTime;

                double distance = targetAngle - this.rotation;
                distance = Mth.positiveModulo(distance + 0.5D, 1.0D) - 0.5D;

                this.deltaRotation += distance * 0.1D;
                this.deltaRotation *= 0.8D;

                this.rotation = Mth.positiveModulo(this.rotation + this.deltaRotation, 1.0D);
            }
            return (float) this.rotation;
        }
    }
}