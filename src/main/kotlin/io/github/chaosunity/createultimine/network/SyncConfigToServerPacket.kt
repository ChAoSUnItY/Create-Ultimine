package io.github.chaosunity.createultimine.network

import dev.architectury.networking.NetworkManager
import dev.architectury.networking.simple.BaseC2SMessage
import dev.architectury.networking.simple.MessageType
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag
import dev.ftb.mods.ftblibrary.snbt.SNBTNet
import dev.ftb.mods.ftbultimine.net.SyncConfigFromServerPacket
import io.github.chaosunity.createultimine.config.CreateUltimineServerConfig
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer

class SyncConfigToServerPacket(private val config: SNBTCompoundTag?) : BaseC2SMessage() {
    constructor(buf: FriendlyByteBuf) : this(SNBTNet.readCompound(buf))

    override fun getType(): MessageType = CreateUltimineNetwork.SYNC_CONFIG_TO_SERVER

    override fun write(buf: FriendlyByteBuf) {
        SNBTNet.writeCompound(buf, config)
    }

    override fun handle(context: NetworkManager.PacketContext) {
        val player = context.player
        if (player !is ServerPlayer || !player.hasPermissions(2))
            return

        val server = player.getServer() ?: return
        CreateUltimineServerConfig.CONFIG.read(config)
        CreateUltimineServerConfig.save(server)

        for (player in server.playerList.players) {
            if (player.getUUID() != player.getUUID()) {
                SyncConfigFromServerPacket(config).sendTo(player)
            }
        }
    }
}
