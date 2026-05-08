package net.noyji.thequestforge.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.noyji.thequestforge.TheQuestForge;

@Mod.EventBusSubscriber(modid = TheQuestForge.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static int uncommonQuestChance;
    public static int rareQuestChance;
    public static int epicQuestChance;
    public static int legendaryQuestChance;

    public static final ForgeConfigSpec.ConfigValue<Integer> SPAWN_QUEST_GIVER_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Integer> ATTEMPTS_TO_CREATE_QUEST;

    public static final ForgeConfigSpec.ConfigValue<Double> COMMON_TASK_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> UNCOMMON_TASK_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> RARE_TASK_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> EPIC_TASK_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> LEGENDARY_TASK_MULTIPLIER;

    public static final ForgeConfigSpec.ConfigValue<String> CURRENCY_ID;

    private static final ForgeConfigSpec.ConfigValue<Integer> UNCOMMON_QUEST_CHANCE;
    private static final ForgeConfigSpec.ConfigValue<Integer> RARE_QUEST_CHANCE;
    private static final ForgeConfigSpec.ConfigValue<Integer> EPIC_QUEST_CHANCE;
    private static final ForgeConfigSpec.ConfigValue<Integer> LEGENDARY_QUEST_CHANCE;


    static {
        BUILDER.comment("server");

        SPAWN_QUEST_GIVER_CHANCE = BUILDER.defineInRange("quest_giver_chance", 80, 30, 100);
        ATTEMPTS_TO_CREATE_QUEST = BUILDER.defineInRange("attempts_to_create", 50, 10, 300);

        UNCOMMON_QUEST_CHANCE = BUILDER.defineInRange("uncommon_quest_rarity", 30, 0, 100);
        RARE_QUEST_CHANCE = BUILDER.defineInRange("rare_quest_rarity", 25, 0, 100);
        EPIC_QUEST_CHANCE = BUILDER.defineInRange("epic_quest_rarity", 10, 0, 100);
        LEGENDARY_QUEST_CHANCE = BUILDER.defineInRange("legendary_quest_rarity", 5, 0, 100);

        COMMON_TASK_MULTIPLIER = BUILDER.defineInRange("common_item_multiplier", 1.0f, 0.1f, 10.f);
        UNCOMMON_TASK_MULTIPLIER = BUILDER.defineInRange("uncommon_item_multiplier", 1.13f, 0.1f, 10.f);
        RARE_TASK_MULTIPLIER = BUILDER.defineInRange("rare_item_multiplier", 1.2f, 0.1f, 10.f);
        EPIC_TASK_MULTIPLIER = BUILDER.defineInRange("epic_item_multiplier", 1.4f, 0.1f, 10.f);
        LEGENDARY_TASK_MULTIPLIER = BUILDER.defineInRange("legendary_item_multiplier", 1.5f, 0.1f, 10.f);

        CURRENCY_ID = BUILDER.define("currency_id", "minecraft:emerald");

        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    public static void onModConfig(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == ServerConfig.SPEC) {
            raritySet();
        }
    }

    private static void raritySet(){
        int sum = UNCOMMON_QUEST_CHANCE.get() + RARE_QUEST_CHANCE.get() + EPIC_QUEST_CHANCE.get() + LEGENDARY_QUEST_CHANCE.get();

        if (sum > 100){
            legendaryQuestChance = (int) Math.round((LEGENDARY_QUEST_CHANCE.get() / (double) sum) * 100);
            epicQuestChance = (int) Math.round((EPIC_QUEST_CHANCE.get() / (double) sum) * 100);
            rareQuestChance = (int) Math.round((RARE_QUEST_CHANCE.get() / (double) sum) * 100);
            uncommonQuestChance = (int) Math.round((UNCOMMON_QUEST_CHANCE.get() / (double) sum) * 100);
        } else {
            legendaryQuestChance = LEGENDARY_QUEST_CHANCE.get();
            epicQuestChance = EPIC_QUEST_CHANCE.get();
            rareQuestChance = RARE_QUEST_CHANCE.get();
            uncommonQuestChance = UNCOMMON_QUEST_CHANCE.get();
        }
    }

}
