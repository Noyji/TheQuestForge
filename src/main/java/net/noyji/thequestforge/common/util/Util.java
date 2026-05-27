package net.noyji.thequestforge.common.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.structure.Structure;
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
import java.util.function.Predicate;

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

        if (source.containsKey(languageKey)) {
            return source.get(languageKey).get(textIndex);
        }

        if (source.containsKey("en_us")) {
            return source.get("en_us").get(textIndex);
        }

        return "???";
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
    @Nullable
    public static BlockPos findBiomePosLocate(Level level, BlockPos centerPos, Predicate<Holder<Biome>> predicate, int radius) {
        if (!(level instanceof ServerLevel serverLevel)) return null;

        BiomeSource biomeSource = serverLevel.getChunkSource().getGenerator().getBiomeSource();

        int quartX = QuartPos.fromBlock(centerPos.getX());
        int quartY = QuartPos.fromBlock(centerPos.getY());
        int quartZ = QuartPos.fromBlock(centerPos.getZ());
        int quartRadius = QuartPos.fromBlock(radius);

        int step = 8;

        for (int r = 0; r <= quartRadius; r += step) {
            for (int x = -r; x <= r; x += step) {
                boolean isEdgeX = Math.abs(x) == r;

                for (int z = -r; z <= r; z += step) {
                    boolean isEdgeZ = Math.abs(z) == r;

                    if (!isEdgeX && !isEdgeZ) continue;

                    int checkX = quartX + x;
                    int checkZ = quartZ + z;

                    Holder<Biome> biomeHolder = biomeSource.getNoiseBiome(checkX, quartY, checkZ, serverLevel.getChunkSource().randomState().sampler());

                    if (predicate.test(biomeHolder)) {
                        return new BlockPos(checkX * 4, centerPos.getY(), checkZ * 4);
                    }
                }
            }
        }
        return null;
    }

    private static final String[] DIRECTIONS = {
            "dialog.thequestforge.direction_placeholder.east",
            "dialog.thequestforge.direction_placeholder.southeast",
            "dialog.thequestforge.direction_placeholder.south",
            "dialog.thequestforge.direction_placeholder.southwest",
            "dialog.thequestforge.direction_placeholder.west",
            "dialog.thequestforge.direction_placeholder.northwest",
            "dialog.thequestforge.direction_placeholder.north",
            "dialog.thequestforge.direction_placeholder.northeast"
    };

    public static String getDirectionText(BlockPos fromPos, BlockPos toPos) {
        double dX = toPos.getX() - fromPos.getX();
        double dZ = toPos.getZ() - fromPos.getZ();

        double angle = Math.atan2(dZ, dX) * 180.0 / Math.PI;

        angle = (angle + 360.0) % 360.0;

        int index = (int) Math.round(angle / 45.0) % 8;

        return DIRECTIONS[index];
    }

    public static BlockPos findStructurePosLocate(Level level, BlockPos centerPos, String structureStr, int radiusInBlocks) {
        if (!(level instanceof ServerLevel serverLevel)) return null;

        Registry<Structure> registry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
        HolderSet<Structure> holderSet = null;

        if (structureStr.startsWith("#")) {
            TagKey<Structure> tagKey = TagKey.create(Registries.STRUCTURE, ResourceLocation.parse(structureStr.substring(1)));
            var optionalTag = registry.getTag(tagKey);
            if (optionalTag.isPresent()) {
                holderSet = optionalTag.get();
            }
        } else {
            ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse(structureStr));
            var optionalHolder = registry.getHolder(key);
            if (optionalHolder.isPresent()) {
                holderSet = HolderSet.direct(optionalHolder.get());
            }
        }

        if (holderSet == null) return null;

        int chunkRadius = Math.max(1, radiusInBlocks / 16);

        Pair<BlockPos, Holder<Structure>> result = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(
                serverLevel, holderSet, centerPos, chunkRadius, false
        );

        return result != null ? result.getFirst() : null;
    }

    private static boolean isSameItem(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty()) return false;
        return ItemStack.isSameItemSameTags(stack1, stack2);
    }
}
