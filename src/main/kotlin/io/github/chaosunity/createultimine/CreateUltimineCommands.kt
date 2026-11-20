package io.github.chaosunity.createultimine

import com.mojang.brigadier.CommandDispatcher
import io.github.chaosunity.createultimine.network.EditConfigPacket
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.CommandSelection

object CreateUltimineCommands {
    fun registerCommands(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        registry: CommandBuildContext,
        selection: CommandSelection
    ) {
        dispatcher.register(
            Commands.literal(CreateUltimine.ID)
                .then(Commands.literal("config").requires { it.isPlayer && it.hasPermission(2) }
                    .executes {
                        EditConfigPacket().sendTo(it.source.playerOrException)
                        1
                    })
        )
    }
}
