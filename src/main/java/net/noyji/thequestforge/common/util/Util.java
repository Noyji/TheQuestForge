package net.noyji.thequestforge.common.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.noyji.thequestforge.api.quest.IWeighable;
import net.noyji.thequestforge.data.group.components.EnchantmentContext;
import net.noyji.thequestforge.data.group.components.JsonReward;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Util {
    @Nullable
    public static <T extends IWeighable> T getWeightItem(List<T> source, RandomSource randomSource){
        if (source == null || source.isEmpty()) return null;
        List<T> copy = new ArrayList<>(source);

        int totalWeight = 0;
        for (T item : copy){
            totalWeight += item.getWeight();
        }

        int roll = randomSource.nextInt(totalWeight);
        for (T item : copy){
            roll -= item.getWeight();
            if (roll < 0){
                return item;
            }
        }

        return copy.get(0);
    }
    @Nullable
    public static <T extends IWeighable> List<T> getWeightList(List<T> source, int count, RandomSource randomSource){
        if (source == null || source.isEmpty()) return null;
        if (count <= 0 || count > source.size()) count = 1;
        List<T> copy = new ArrayList<>(source);
        List<T> result = new ArrayList<>();

        for (int i = 0; i < count; i++){
            if (copy.isEmpty()) break;

            T item = getWeightItem(copy, randomSource);
            result.add(item);
            copy.remove(item);
        }
        return result;
    }
    @Nullable
    public static EntityType<?> getEntityType(ResourceLocation resourceLocation){
        return ForgeRegistries.ENTITY_TYPES.getValue(resourceLocation);
    }
    @Nullable
    public static ResourceLocation getEntityResourceLocation(Entity entity){
        return getEntityResourceLocation(entity.getType());
    }
    @Nullable
    public static ResourceLocation getEntityResourceLocation(EntityType<?> entity){
        return ForgeRegistries.ENTITY_TYPES.getKey(entity);
    }
    @Nullable
    public static ResourceLocation getItemResourceLocation(ItemStack itemStack){
        return  getItemResourceLocation(itemStack.getItem());
    }
    @Nullable
    public static ResourceLocation getItemResourceLocation(Item item){
        return  ForgeRegistries.ITEMS.getKey(item);
    }
    @Nullable
    public static ItemStack parseItemStack(JsonReward jsonTask, int goal, RandomSource randomSource) {
        if (jsonTask == null) return null;

        Item item = ForgeRegistries.ITEMS.getValue(jsonTask.getTarget());
        if (item == null) return null;

        ItemStack itemStack = new ItemStack(item, goal);

        CompoundTag tag = jsonTask.getTag();
        if (tag != null && !tag.isEmpty()){
            itemStack.setTag(tag);
        }

        List<EnchantmentContext> enchantmentContexts = jsonTask.getEnchantments(randomSource);
        if (enchantmentContexts != null && !enchantmentContexts.isEmpty()){
            for (EnchantmentContext context : enchantmentContexts){

                if (context == null) continue;

                itemStack.enchant(context.getEnchantment(), context.getLevel());
            }
        }

        return itemStack;
    }

    public static Item getItem(ResourceLocation resourceLocation){
        return ForgeRegistries.ITEMS.getValue(resourceLocation);
    }

    public static String getTranslateTextFromMap(Map<String, List<String>> source, String languageKey, int textIndex){
        if (source == null || source.isEmpty()) return "source is null or empty!";

        if (languageKey == null || languageKey.isEmpty()) languageKey = "en_us";
        if (textIndex < 0) textIndex = 0;

        String result = source.get(languageKey).get(textIndex);

        if (result == null) result = source.get("en_us").get(textIndex);

        if (result == null) result = "???";

        return result;
    }

}
