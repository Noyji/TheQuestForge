package net.noyji.thequestforge.data.group.components;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import net.noyji.thequestforge.api.quest.IWeighable;
import net.noyji.thequestforge.data.template.components.Range;

public class EnchantmentJsonContext implements IWeighable {
    private String id;
    private Range level;
    private int weight;

    public int getLevel(RandomSource randomSource){
        return level.getRandomInRange(randomSource);
    }

    public Enchantment getEnchantment(){
       return ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.parse(id));
    }

    @Override
    public int getWeight() {
        return weight;
    }
}
