package io.github.chaosunity.createultimine

import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.events.common.PlayerEvent
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag
import dev.ftb.mods.ftbultimine.net.SyncConfigFromServerPacket
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry
import io.github.chaosunity.createultimine.config.CreateUltimineServerConfig
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(CreateUltimine.ID)
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
class CreateUltimine {
    companion object {
        const val ID = "createultimine"

        val LOGGER: Logger = LogManager.getLogger(ID)
    }

    init {
        PlayerEvent.PLAYER_JOIN.register(::onPlayerJoin)
        LifecycleEvent.SERVER_BEFORE_START.register(::serverStarting)
    }

    private fun onPlayerJoin(serverPlayer: ServerPlayer) {
        val config = SNBTCompoundTag()
        CreateUltimineServerConfig.CONFIG.write(config)
        SyncConfigFromServerPacket(config).sendTo(serverPlayer)
    }

    private fun serverStarting(server: MinecraftServer) {
        ShapeRegistry.freeze();
        CreateUltimineServerConfig.load(server);
    }
}