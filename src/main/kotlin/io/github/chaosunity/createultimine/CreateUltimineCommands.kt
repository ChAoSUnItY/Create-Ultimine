package io.github.chaosunity.createultimine

import com.mojang.brigadier.CommandDispatcher
import dev.architectury.networking.NetworkManager
import dev.ftb.mods.ftblibrary.net.EditConfigChoicePacket
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object CreateUltimineCommands {
    fun registerCommands(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        registry: CommandBuildContext,
        selection: Commands.CommandSelection
    ) {
        dispatcher.register(
            Commands.literal("createultimine")
                .then(Commands.literal("config").requires { it.isPlayer && it.hasPermission(2) }
                    .executes {
                        NetworkManager.sendToPlayer(
                            (it.getSource() as CommandSourceStack).playerOrException,
                            EditConfigChoicePacket.server("createultimine-server")
                        )
                        1
                    })
        )
    }
}