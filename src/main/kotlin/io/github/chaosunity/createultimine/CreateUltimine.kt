package io.github.chaosunity.createultimine

import dev.ftb.mods.ftblibrary.config.manager.ConfigManager
import dev.ftb.mods.ftbultimine.api.rightclick.RegisterRightClickHandlerEvent
import io.github.chaosunity.createultimine.config.CreateUltimineServerConfig
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(CreateUltimine.ID)
class CreateUltimine {
    companion object {
        const val ID = "createultimine"

        val LOGGER: Logger = LogManager.getLogger(ID)
    }

    init {
        ConfigManager.getInstance().registerServerConfig(CreateUltimineServerConfig.CONFIG, "$ID.server_settings", true)
        RegisterRightClickHandlerEvent.REGISTER.register(::registerBuiltinHandlers)
    }

    private fun registerBuiltinHandlers(dispatcher: RegisterRightClickHandlerEvent.Dispatcher) {
        dispatcher.registerHandler(ManualApplication)
        dispatcher.registerHandler(WrenchUse)
    }
}