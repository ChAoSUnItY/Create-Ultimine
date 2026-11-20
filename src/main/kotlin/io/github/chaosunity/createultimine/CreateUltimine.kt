package io.github.chaosunity.createultimine

import dev.architectury.event.EventResult
import dev.architectury.event.events.common.CommandRegistrationEvent
import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.events.common.PlayerEvent
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag
import dev.ftb.mods.ftblibrary.ui.CustomClickEvent
import dev.ftb.mods.ftbultimine.api.rightclick.RegisterRightClickHandlerEvent
import io.github.chaosunity.createultimine.config.CreateUltimineServerConfig
import io.github.chaosunity.createultimine.network.SyncConfigFromServerPacket
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.KotlinModLoadingContext
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

@Mod(CreateUltimine.ID)
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
object CreateUltimine {
    const val ID = "createultimine"

    private val OPEN_CONFIG: ResourceLocation = ResourceLocation.fromNamespaceAndPath(ID, "open_config")
    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        RegisterRightClickHandlerEvent.REGISTER.register(::registerBuiltinHandlers)

        KotlinModLoadingContext.get().getKEventBus().addListener(::onClientSetup)
        PlayerEvent.PLAYER_JOIN.register(::onPlayerJoin)
        LifecycleEvent.SERVER_BEFORE_START.register(::serverStarting)
        CommandRegistrationEvent.EVENT.register(CreateUltimineCommands::registerCommands)

        FORGE_BUS.addListener(ManualApplication.ManualApplicationSettings::registerListener)
    }

    private fun registerBuiltinHandlers(dispatcher: RegisterRightClickHandlerEvent.Dispatcher) {
        dispatcher.registerHandler(ManualApplication)
        dispatcher.registerHandler(WrenchUse)
        dispatcher.registerHandler(CopycatApplication)
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            CustomClickEvent.EVENT.register {
                if (it.id == OPEN_CONFIG) {
                    Minecraft.getInstance().player
                        ?.connection
                        ?.sendCommand("$ID config")
                    EventResult.interruptFalse()
                } else {
                    EventResult.pass()
                }
            }
        }
    }

    private fun onPlayerJoin(serverPlayer: ServerPlayer) {
        val config = SNBTCompoundTag()
        CreateUltimineServerConfig.CONFIG.write(config)
        SyncConfigFromServerPacket(config).sendTo(serverPlayer)
    }

    private fun serverStarting(server: MinecraftServer) {
        // This event happens later than recipe reload, which would override the original config mutated from
        // ManualApplication.ManualApplicationSettings, so we have to load it again here.
        CreateUltimineServerConfig.load(server)
        ManualApplication.ManualApplicationSettings.syncToConfig()
    }

    fun editConfig() {
        val configGroup = CreateUltimineServerConfig.getConfigGroup()
        EditConfigScreen(configGroup).setAutoclose(true).openGui()
    }
}
