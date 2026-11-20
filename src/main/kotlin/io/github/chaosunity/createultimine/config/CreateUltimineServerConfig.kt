package io.github.chaosunity.createultimine.config

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag
import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue
import dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig
import io.github.chaosunity.createultimine.CreateUltimine
import io.github.chaosunity.createultimine.network.SyncConfigToServerPacket
import net.minecraft.server.MinecraftServer

object CreateUltimineServerConfig {
    @JvmStatic
    val CONFIG: SNBTConfig = SNBTConfig.create("createultimine-server").comment(
        "Server-specific configuration for Create Ultimine",
        "This file is meant for server administrators to control user behaviour."
    )

    @JvmStatic
    val FEATURES: SNBTConfig = CONFIG.addGroup("features")

    @JvmStatic
    val MANUAL_APPLICATIONS: RecipeToggleMapValue = FEATURES
        .add(
            RecipeToggleMapValue(
                FEATURES,
                "manual_applications",
                mapOf()
            )
        )
        .comment(
            "Right-click with any applicable items held in hand with the Ultimine key held to apply onto any blocks",
            "Entries will be updated once addition / deletion of datapack / mod triggers a recipe reload"
        )

    @JvmStatic
    val RIGHT_CLICK_WRENCH: BooleanValue = FEATURES
        .addBoolean("right_click_wrench", true)
        .comment("Right-click with an wrench with the Ultimine key held to interact blocks (e.g. rotate, breaking)")

    @JvmStatic
    val COPYCAT_APPLICATION: BooleanValue = FEATURES
        .addBoolean("copycat_application", true)
        .comment("Right-click on the copycat blocks with the held material to apply onto")

    fun load(server: MinecraftServer) {
        ConfigUtil.loadDefaulted(
            CONFIG,
            server.getWorldPath(ConfigUtil.SERVER_CONFIG_DIR),
            CreateUltimine.ID
        )
    }

    fun save(server: MinecraftServer) {
        val path = server
            .getWorldPath(ConfigUtil.SERVER_CONFIG_DIR)
            .resolve("${CreateUltimine.ID}-server.snbt")

        CONFIG.save(path)
    }

    fun getConfigGroup(): ConfigGroup {
        val group = ConfigGroup("${CreateUltimine.ID}.server_settings") {
            if (it) {
                val config = SNBTCompoundTag()
                CONFIG.write(config)
                SyncConfigToServerPacket(config).sendToServer()
            }
        }
        CONFIG.createClientConfig(group)
        return group
    }
}
