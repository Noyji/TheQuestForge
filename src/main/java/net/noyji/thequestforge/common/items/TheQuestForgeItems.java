package net.noyji.thequestforge.common.items;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.common.items.custom.QuestBookItem;
import net.noyji.thequestforge.common.items.custom.QuestCompassItem;

public class TheQuestForgeItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TheQuestForge.MODID);

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }

    public static final RegistryObject<Item> QUEST_BOOK = ITEMS.register("quest_book",
            () -> new QuestBookItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> QUEST_COMPASS = ITEMS.register("quest_compass",
            () -> new QuestCompassItem(new Item.Properties().stacksTo(1)));
}
