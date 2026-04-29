package net.noyji.thequestforge.data.quest.player.components;

import net.noyji.thequestforge.data.quest.entity.components.QuestDialog;

public enum QuestRarity {
    COMMON(0, "common"),
    UNCOMMON(1, "uncommon"),
    RARE(2, "rare"),
    EPIC(3, "epic"),
    LEGENDARY(4, "legendary");

    private final int weight;
    private final String name;

    QuestRarity(int weight, String name) {
        this.weight = weight;
        this.name = name;
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
}
