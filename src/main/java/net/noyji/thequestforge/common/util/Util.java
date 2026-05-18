package net.noyji.thequestforge.common.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.noyji.thequestforge.api.quest.IWeighable;
import net.noyji.thequestforge.config.ServerConfig;
import net.noyji.thequestforge.data.group.components.EnchantmentContext;
import net.noyji.thequestforge.data.group.components.IParseItems;
import net.noyji.thequestforge.data.quest.player.components.QuestRarity;
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
    public static <T extends IParseItems> ItemStack parseItemStack(T object, int goal, RandomSource randomSource) {
        if (object == null) return null;

        Item item = ForgeRegistries.ITEMS.getValue(object.getTarget());
        if (item == null) return null;

        ItemStack itemStack = new ItemStack(item, goal);

        CompoundTag tag = object.getTag();
        if (tag != null && !tag.isEmpty()){
            itemStack.setTag(tag);
        }

        List<EnchantmentContext> enchantmentContexts = object.getEnchantments(randomSource);
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

    public static double getRarityMultiplier(QuestRarity rarity){
        double m;
        switch (rarity) {
            case UNCOMMON -> m = ServerConfig.UNCOMMON_TASK_MULTIPLIER.get();
            case RARE -> m = ServerConfig.RARE_TASK_MULTIPLIER.get();
            case EPIC -> m = ServerConfig.EPIC_TASK_MULTIPLIER.get();
            case LEGENDARY -> m = ServerConfig.LEGENDARY_TASK_MULTIPLIER.get();
            default -> m = ServerConfig.COMMON_TASK_MULTIPLIER.get();
        }
        return m;
    }

    public static void removeItemFromPlayer(Player player, ItemStack targetItem, int countToRemove){
        if (countMatchingItems(player, targetItem) < countToRemove) {
            return;
        }

        int remainingToRemove = countToRemove;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack slotStack = player.getInventory().getItem(i);

            if (isSameItem(slotStack, targetItem)) {
                int takeFromSlot = Math.min(remainingToRemove, slotStack.getCount());

                slotStack.shrink(takeFromSlot);
                remainingToRemove -= takeFromSlot;

                if (remainingToRemove <= 0) {
                    break;
                }
            }
        }
    }

    public static int countMatchingItems(Player player, ItemStack targetItem) {
        int totalCount = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack slotStack = player.getInventory().getItem(i);
            if (isSameItem(slotStack, targetItem)) {
                totalCount += slotStack.getCount();
            }
        }
        return totalCount;
    }

    public static boolean hasItem(Player player, ItemStack targetStack) {
        if (targetStack == null || targetStack.isEmpty()) {
            return true;
        }

        int requiredCount = targetStack.getCount();
        int foundCount = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack slotStack = player.getInventory().getItem(i);

            if (!slotStack.isEmpty() && ItemStack.isSameItemSameTags(slotStack, targetStack)) {
                foundCount += slotStack.getCount();

                if (foundCount >= requiredCount) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    public static ItemStack findItemInInventory(Player player, Item targetItem) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack slotStack = player.getInventory().getItem(i);

            if (!slotStack.isEmpty() && slotStack.is(targetItem)) {
                return slotStack;
            }
        }
        return null;
    }

    private static boolean isSameItem(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty()) return false;
        return ItemStack.isSameItemSameTags(stack1, stack2);
    }
}
