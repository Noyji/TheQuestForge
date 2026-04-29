package net.noyji.thequestforge.data.group.components;

import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentContext {
    private final Enchantment enchantment;
    private final int level;

    public EnchantmentContext(Enchantment enchantment, int level) {
        this.level = level;
        this.enchantment = enchantment;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public int getLevel() {
        return level;
    }
}
