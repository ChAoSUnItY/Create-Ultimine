package io.github.chaosunity.createultimine.config

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig

object CreateUltimineServerConfig {
    @JvmStatic
    val CONFIG: SNBTConfig = SNBTConfig.create("createultimine-server").comment(
        "Server-specific configuration for Create Ultimine",
        "This file is meant for server administrators to control user behaviour."
    )

    @JvmStatic
    val FEATURES: SNBTConfig = CONFIG.addGroup("features")

    @JvmStatic
    val MANUAL_APPLICATIONS: StringBooleanMapValue = FEATURES
        .add(
            StringBooleanMapValue(
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
}