package net.noyji.thequestforge.api.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.action.AbstractAction;
import net.noyji.thequestforge.api.quest.action.AcceptQuestAction;

import java.util.function.Supplier;

public class ActionRegistry {
    public static final ResourceKey<Registry<AbstractAction>> ACTION_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(TheQuestForge.MODID, "actions"));
    private static final DeferredRegister<AbstractAction> ACTIONS =
            DeferredRegister.create(ACTION_REGISTRY_KEY, TheQuestForge.MODID);
    public static final Supplier<IForgeRegistry<AbstractAction>> REGISTRY =
            ACTIONS.makeRegistry(() -> new RegistryBuilder<AbstractAction>()
                    .disableSaving()
                    .disableOverrides()
            );

    public static void register(IEventBus eventBus){
        ACTIONS.register(eventBus);
    }

    public static RegistryObject<AbstractAction> registerAction(AbstractAction action){
        return ACTIONS.register(action.getLocation().getPath(), () -> action);
    }

    public static AbstractAction getAction(ResourceLocation resourceLocation){
        return REGISTRY.get().getValue(resourceLocation);
    }

    public static final RegistryObject<AbstractAction> QUEST_ACCEPT = registerAction(new AcceptQuestAction());
}
