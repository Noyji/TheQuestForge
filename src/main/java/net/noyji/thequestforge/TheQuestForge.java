package net.noyji.thequestforge;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.noyji.thequestforge.api.quest.registry.ActionRegistry;
import net.noyji.thequestforge.api.quest.registry.RequirementRegistry;
import net.noyji.thequestforge.api.quest.registry.TaskHandlerRegistry;
import net.noyji.thequestforge.common.items.ItemsRegistry;
import net.noyji.thequestforge.config.ClientConfig;
import net.noyji.thequestforge.config.ServerConfig;
import net.noyji.thequestforge.data.capability.AttachCapabilities;
import net.noyji.thequestforge.network.ModNetworking;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;


@Mod(TheQuestForge.MODID)
public class TheQuestForge {

    public static final String MODID = "thequestforge";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheQuestForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onRegisterCapabilities);

        TaskHandlerRegistry.register(modEventBus);
        ActionRegistry.register(modEventBus);
        RequirementRegistry.register(modEventBus);
        ItemsRegistry.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, String.format("%s-client.toml", MODID));
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, String.format("%s-server.toml", MODID));

        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        AttachCapabilities.registerCaps(event);
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        AttachCapabilities.attachCaps(event);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetworking::register);
    }

    public static ResourceLocation id (@NotNull String path){
        return ResourceLocation.fromNamespaceAndPath(TheQuestForge.MODID, path);
    }

}
