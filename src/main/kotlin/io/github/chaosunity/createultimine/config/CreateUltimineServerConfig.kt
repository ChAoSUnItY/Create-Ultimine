package io.github.chaosunity.createultimine.config

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue
import dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig
import net.minecraft.server.MinecraftServer

object CreateUltimineServerConfig {
    @JvmStatic
    val CONFIG: SNBTConfig = SNBTConfig.create("createultimine-server").comment<SNBTConfig>(
        "Server-specific configuration for Create Ultimine",
        "This file is meant for server administrators to control user behaviour.",
        "Changes in this file currently require a server restart to take effect"
    )

    @JvmStatic
    val FEATURES: SNBTConfig = CONFIG.addGroup("features")

    @JvmStatic
    val RIGHT_CLICK_ALLOY: BooleanValue = FEATURES
        .addBoolean("right_click_alloy", true)
        .comment("Right-click with an alloy ingot (e.g. andesite alloy) with the Ultimine key held to apply on stripped logs or casings")

    @JvmStatic
    val RIGHT_CLICK_WRENCH: BooleanValue = FEATURES
        .addBoolean("right_click_wrench", true)
        .comment("Right-click with an wrench with the Ultimine key held to interact blocks (e.g. rotate, breaking)")

    fun load(server: MinecraftServer) {
        ConfigUtil.loadDefaulted(
            CONFIG,
            server.getWorldPath(ConfigUtil.SERVER_CONFIG_DIR),
            "createultimine"
        )
    }
}