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
import net.noyji.thequestforge.api.quest.task.AbstractTask;
import net.noyji.thequestforge.api.quest.task.CollectTask;
import net.noyji.thequestforge.api.quest.task.KillTask;
import net.noyji.thequestforge.api.quest.task.TaskType;

import java.util.function.Supplier;

public class TaskHandlerRegistry {
    public static final ResourceKey<Registry<TaskType<?>>> TASK_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(TheQuestForge.MODID, "task_type"));

    private static final DeferredRegister<TaskType<?>> TASK_TYPES =
            DeferredRegister.create(TASK_REGISTRY_KEY, TheQuestForge.MODID);

    public static final Supplier<IForgeRegistry<TaskType<?>>> REGISTRY =
            TASK_TYPES.makeRegistry(() -> new RegistryBuilder<TaskType<?>>()
                    .disableSaving()
                    .disableOverrides()
            );



    public static void register(IEventBus eventBus){
        TASK_TYPES.register(eventBus);
    }

    public static <T extends AbstractTask<?>> RegistryObject<TaskType<T>> registerTaskType(String name, Supplier<T> factory) {
        return TASK_TYPES.register(name, () -> new TaskType<>(factory));
    }

    public static final RegistryObject<TaskType<CollectTask>> COLLECT_TASK = registerTaskType("collect", CollectTask::new);
    public static final RegistryObject<TaskType<KillTask>> KILL_TASK = registerTaskType("kill", KillTask::new);
}