package io.github.chaosunity.createultimine.network

import dev.architectury.networking.simple.MessageType
import dev.architectury.networking.simple.SimpleNetworkManager
import io.github.chaosunity.createultimine.CreateUltimine

object CreateUltimineNetwork {
    val NET: SimpleNetworkManager = SimpleNetworkManager.create(CreateUltimine.ID)
    val SYNC_CONFIG_FROM_SERVER: MessageType = NET.registerS2C("sync_config_from_server", ::SyncConfigFromServerPacket)
    val SYNC_CONFIG_TO_SERVER: MessageType = NET.registerC2S("sync_config_to_server", ::SyncConfigToServerPacket)
    val EDIT_CONFIG: MessageType = NET.registerS2C("edit_config") { EditConfigPacket() }
}
