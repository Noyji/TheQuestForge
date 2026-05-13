package net.noyji.thequestforge.common.init;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.noyji.thequestforge.TheQuestForge;

public class TheQuestForgeSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(
            ForgeRegistries.SOUND_EVENTS, TheQuestForge.MODID
    );

    public static final RegistryObject<SoundEvent> NPC_VOICE = registerSoundEvent("npc_voice");
    public static final RegistryObject<SoundEvent> REMOVE_QUEST = registerSoundEvent("remove_quest");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name){
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(TheQuestForge.id(name)));
    }

    public static void register(IEventBus eventBus){
        SOUND_EVENTS.register(eventBus);
    }
}
