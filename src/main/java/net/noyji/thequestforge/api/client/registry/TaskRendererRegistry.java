package net.noyji.thequestforge.api.client.registry;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.noyji.thequestforge.TheQuestForge;
import net.noyji.thequestforge.api.client.ITaskRenderer;
import net.noyji.thequestforge.api.client.render.task.CollectTaskRenderer;
import net.noyji.thequestforge.api.client.render.task.KillTaskRenderer;
import net.noyji.thequestforge.api.quest.task.AbstractTask;
import net.noyji.thequestforge.api.quest.task.CollectTask;
import net.noyji.thequestforge.api.quest.task.KillTask;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = TheQuestForge.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TaskRendererRegistry {

    private static final Map<Class<? extends AbstractTask<?>>, ITaskRenderer<?>> RENDERERS = new HashMap<>();

    public static <T extends AbstractTask<?>> void register(Class<T> taskClass, ITaskRenderer<T> renderer) {
        RENDERERS.put(taskClass, renderer);
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractTask<?>> ITaskRenderer<T> getRenderer(T task) {
        return (ITaskRenderer<T>) RENDERERS.get(task.getClass());
    }

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event){
        event.enqueueWork(() -> {
            TaskRendererRegistry.register(CollectTask.class, new CollectTaskRenderer());
            TaskRendererRegistry.register(KillTask.class, new KillTaskRenderer());
        });
    }
}
