package net.noyji.thequestforge.api.quest.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.quest.requirements.AbstractRequirement;
import net.noyji.thequestforge.api.quest.requirements.EntityIsRequirement;

import java.util.function.Supplier;

public class RequirementRegistry {
    public static final ResourceKey<Registry<AbstractRequirement>> REQUIREMENT_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(TheQuestForge.MODID, "requirements"));
    private static final DeferredRegister<AbstractRequirement> REQUIREMENTS =
            DeferredRegister.create(REQUIREMENT_REGISTRY_KEY, TheQuestForge.MODID);
    public static final Supplier<IForgeRegistry<AbstractRequirement>> REGISTRY =
            REQUIREMENTS.makeRegistry(() -> new RegistryBuilder<AbstractRequirement>()
                    .disableSaving()
                    .disableOverrides()
            );

    public static void register(IEventBus eventBus){
        REQUIREMENTS.register(eventBus);
    }

    public static RegistryObject<AbstractRequirement> registerRequirement(AbstractRequirement requirement){
        return REQUIREMENTS.register(requirement.getLocation().getPath(), () -> requirement);
    }

    public static AbstractRequirement getRequirement(ResourceLocation resourceLocation){
        return REGISTRY.get().getValue(resourceLocation);
    }

    public static AbstractRequirement getRequirement(String name){
        ResourceLocation resourceLocation = ResourceLocation.parse(name);
        return getRequirement(resourceLocation);
    }

    public static final RegistryObject<AbstractRequirement> ENTITY_IS = registerRequirement(new EntityIsRequirement());
}
