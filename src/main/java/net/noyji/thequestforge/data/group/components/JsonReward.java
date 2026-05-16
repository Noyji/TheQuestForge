package net.noyji.thequestforge.data.group.components;

import com.google.gson.annotations.SerializedName;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.Enchantment;
import net.noyji.thequestforge.api.quest.IWeighable;
import net.noyji.thequestforge.common.util.Util;
import net.noyji.thequestforge.data.quest.player.components.QuestRarity;
import net.noyji.thequestforge.data.template.components.Range;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class JsonReward implements IWeighable, IParseItems {
    private String target;
    private String tag;
    private List<EnchantmentJsonContext> enchantment;
    @SerializedName("enchantment_count")
    private Range enchantmentCount;
    private boolean multiplier;
    private Range count;
    private int price;
    private int weight;

    public int getPrice() {
        return price;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    public int getCount(RandomSource randomSource, QuestRarity rarity){
        if (isMultiplier()){
            double m = Util.getRarityMultiplier(rarity);
            return (int) (count.getRandomInRange(randomSource) * m);
        } else {
            return count.getRandomInRange(randomSource);
        }
    }

    public boolean targetIs(String id){
        return target.equals(id);
    }
    @Override
    public ResourceLocation getTarget() {
        return ResourceLocation.parse(target);
    }
    @Override
    @Nullable
    public List<EnchantmentContext> getEnchantments(RandomSource randomSource) {
        if (enchantment == null || enchantment.isEmpty()) return null;

        if (enchantmentCount == null || enchantmentCount.isEmpty()) enchantmentCount = Range.ofValue(1);

        List<EnchantmentContext> result = new ArrayList<>();

        List<EnchantmentJsonContext> selectEnchantments = Util.getWeightList(enchantment,
                enchantmentCount.getRandomInRange(randomSource), randomSource);

        if (selectEnchantments == null || selectEnchantments.isEmpty()) return null;

        for (EnchantmentJsonContext enchantmentJsonContext : selectEnchantments){

            Enchantment selectEnchantment = enchantmentJsonContext.getEnchantment();

            if (selectEnchantment != null) {
                EnchantmentContext parse = new EnchantmentContext(selectEnchantment, enchantmentJsonContext.getLevel(randomSource));
                result.add(parse);
            }
        }

        return result;
    }
    @Override
    @Nullable
    public CompoundTag getTag() {
        if (tag == null || tag.isEmpty()) return null;
        try {
            return TagParser.parseTag(tag);
        } catch (CommandSyntaxException e) {
            return null;
        }
    }

    public boolean isMultiplier() {
        return multiplier;
    }
}
