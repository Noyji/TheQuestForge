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
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.common.items.TheQuestForgeItems;
import net.noyji.thequestforge.data.capability.CapabilityUtil;
import net.noyji.thequestforge.data.quest.player.PlayerQuest;
import net.noyji.thequestforge.data.quest.player.components.GiverData;

import java.util.UUID;

public class TheQuestForgeProperties {

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
            if (tag == null || !tag.hasUUID("giver_uuid"))
                return 0.0F;

            UUID questUUID = tag.getUUID("giver_uuid");

            if (!(currentEntity instanceof Player player)) return 0.0F;
            PlayerQuest playerQuest = CapabilityUtil.getPlayerQuestData(player).getQuest(questUUID);

            if (playerQuest == null) return 0.0F;

            GiverData giverData = playerQuest.getGiverData();

            ResourceKey<Level> targetDim = giverData.getDimension();
            if (!level.dimension().equals(targetDim)){
                return (float) Math.random();
            }

            double targetX = giverData.getGiverPos().getX();
            double targetZ = giverData.getGiverPos().getZ();

           Entity npc = level.getEntity(giverData.getEntityId());
            if (npc != null){
                targetX = npc.getX();
                targetZ = npc.getZ();
            }


            double d0 = targetX - currentEntity.getX();
            double d1 = targetZ - currentEntity.getZ();
            double angle = (Math.atan2(d1, d0) + (Math.PI / 2.0D)) / (Math.PI * 2D);
            double entityYaw = Mth.positiveModulo(currentEntity.getYRot() / 360.0F, 1.0D);

            return (float) Mth.positiveModulo(angle - entityYaw, 1.0D);
        });
    }
}
