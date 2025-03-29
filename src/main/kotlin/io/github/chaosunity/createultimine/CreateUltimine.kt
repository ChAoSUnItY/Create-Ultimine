package io.github.chaosunity.createultimine

import com.simibubi.create.infrastructure.config.AllConfigs
import dev.architectury.event.events.common.LifecycleEvent
import io.github.chaosunity.createultimine.config.CreateUltimineServerConfig
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.config.ModConfigEvent.Loading
import net.neoforged.fml.event.config.ModConfigEvent.Reloading
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(CreateUltimine.ID)
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
class CreateUltimine(container: ModContainer) {
    companion object {
        const val ID = "createultimine"

        val LOGGER: Logger = LogManager.getLogger(ID)
    }

    init {
        LOGGER.log(Level.INFO, "Hello world!")

        container.registerConfig(ModConfig.Type.SERVER, CreateUltimineServerConfig.configSpec)
        CreateUltimineServerConfig.configSpec.s
    }

    @SubscribeEvent
    fun onLoad(event: Loading) {
        CreateUltimineServerConfig.configSpec.save()
    }

    @SubscribeEvent
    fun onReload(event: Reloading) {
        CreateUltimineServerConfig.configSpec.save()
    }

    @SubscribeEvent
    fun onCommonSetup(event: FMLCommonSetupEvent) {
        LOGGER.log(Level.INFO, "Hello! This is working!")
    }
}