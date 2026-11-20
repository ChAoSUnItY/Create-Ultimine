package io.github.chaosunity.createultimine.network

import dev.architectury.networking.NetworkManager
import dev.architectury.networking.simple.BaseS2CMessage
import dev.architectury.networking.simple.MessageType
import io.github.chaosunity.createultimine.CreateUltimine
import net.minecraft.network.FriendlyByteBuf

class EditConfigPacket : BaseS2CMessage() {
    override fun getType(): MessageType = CreateUltimineNetwork.EDIT_CONFIG

    override fun write(buf: FriendlyByteBuf) {}

    override fun handle(context: NetworkManager.PacketContext) {
        CreateUltimine.editConfig()
    }
}
