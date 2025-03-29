package io.github.chaosunity.createultimine.config

import net.neoforged.neoforge.common.ModConfigSpec

class CreateUltimineServerConfig(builder: ModConfigSpec.Builder) {
    companion object {
        @JvmStatic
        private val pair = ModConfigSpec.Builder().configure(::CreateUltimineServerConfig)

        @JvmStatic
        val config: CreateUltimineServerConfig = pair.left

        @JvmStatic
        val configSpec: ModConfigSpec = pair.right
    }

    var RIGHT_CLICK_ALLOY: ModConfigSpec.BooleanValue
    var RIGHT_CLICK_WRENCH: ModConfigSpec.BooleanValue

    init {
        builder.comment("General Settings").push("general")

        RIGHT_CLICK_ALLOY = builder
            .comment("Right-click with an alloy ingot (e.g. andesite alloy) with the Ultimine key held to apply on stripped logs or casings")
            .define("right_click_alloy", true)
        RIGHT_CLICK_WRENCH = builder
            .comment("Right-click with an wrench with the Ultimine key held to interact blocks (e.g. rotate, breaking)")
            .define("right_click_wrench", true)

        builder.pop()
    }
}