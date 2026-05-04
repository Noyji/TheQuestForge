package net.noyji.thequestforge.data.quest.player.components;

import net.minecraft.network.chat.Component;

public enum QuestRarity {
    COMMON(0, "gui.thequestforge.quest.rarity.common"),
    UNCOMMON(1, "gui.thequestforge.quest.rarity.uncommon"),
    RARE(2, "gui.thequestforge.quest.rarity.rare"),
    EPIC(3, "gui.thequestforge.quest.rarity.epic"),
    LEGENDARY(4, "gui.thequestforge.quest.rarity.legendary");

    private final int weight;
    private final String translateKey;

    QuestRarity(int weight, String translateKey) {
        this.weight = weight;
        this.translateKey = translateKey;
    }

    /**
     *
     * @param reference
     * @param weighted
     * @return true = if reference > weighted,
     *         false = if reference < weighted;
     */
    public static boolean weighValues(QuestRarity reference, QuestRarity weighted){
        return reference.weight > weighted.weight;
    }

    public static boolean rarityIs(QuestRarity reference, QuestRarity weighted){
        return reference.weight == weighted.weight;
    }

    public Component getTranslateName(){
        return Component.translatable(translateKey);
    }
}
