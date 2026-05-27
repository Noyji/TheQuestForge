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
import net.noyji.thequestforge.api.quest.requirements.*;

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

    public static void requirementsLogger(ResourceLocation resourceLocation, boolean returnValue){
        TheQuestForge.LOGGER.debug("{} : {}", resourceLocation, returnValue);
    }

    public static final RegistryObject<AbstractRequirement> ENTITY_IS = registerRequirement(new EntityIsRequirement());
    public static final RegistryObject<AbstractRequirement> ENTITY_IS_NOT = registerRequirement(new EntityIsNotRequirement());
    public static final RegistryObject<AbstractRequirement> ENTITY_TAG_IS = registerRequirement(new EntityTagIsRequirement());
    public static final RegistryObject<AbstractRequirement> ENTITY_TAG_IS_NOT = registerRequirement(new EntityTagIsNotRequirement());
    public static final RegistryObject<AbstractRequirement> IS_BABY = registerRequirement(new IsBabyRequirement());

    public static final RegistryObject<AbstractRequirement> BIOME_IS = registerRequirement(new BiomeIsRequirement());
    public static final RegistryObject<AbstractRequirement> BIOME_IS_NOT = registerRequirement(new BiomeIsNotRequirement());
    public static final RegistryObject<AbstractRequirement> BIOME_TAG_IS = registerRequirement(new BiomeTagIsRequirement());
    public static final RegistryObject<AbstractRequirement> BIOME_TAG_IS_NOT = registerRequirement(new BiomeTagIsNotRequirement());
    public static final RegistryObject<AbstractRequirement> IS_BIOME_NEARBY = registerRequirement(new IsBiomeNearbyRequirement());

    public static final RegistryObject<AbstractRequirement> DIMENSION_IS = registerRequirement(new DimensionIsRequirement());
    public static final RegistryObject<AbstractRequirement> DIMENSION_IS_NOT = registerRequirement(new DimensionIsNotRequirement());

    public static final RegistryObject<AbstractRequirement> IS_STRUCTURE_NEARBY = registerRequirement(new IsStructureNearbyRequirement());

}
