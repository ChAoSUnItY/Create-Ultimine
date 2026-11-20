package io.github.chaosunity.createultimine.network

import dev.architectury.networking.NetworkManager
import dev.architectury.networking.simple.BaseS2CMessage
import dev.architectury.networking.simple.MessageType
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag
import dev.ftb.mods.ftblibrary.snbt.SNBTNet
import io.github.chaosunity.createultimine.config.CreateUltimineServerConfig
import net.minecraft.network.FriendlyByteBuf

class SyncConfigFromServerPacket(private val config: SNBTCompoundTag?): BaseS2CMessage() {
    constructor(buf: FriendlyByteBuf) : this(SNBTNet.readCompound(buf))

    override fun getType(): MessageType = CreateUltimineNetwork.SYNC_CONFIG_FROM_SERVER

    override fun write(buf: FriendlyByteBuf) {
        SNBTNet.writeCompound(buf, config)
    }

    override fun handle(context: NetworkManager.PacketContext) {
        CreateUltimineServerConfig.CONFIG.read(config)
    }
}
