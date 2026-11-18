package io.github.chaosunity.createultimine

import com.simibubi.create.content.decoration.copycat.CopycatBlock
import dev.ftb.mods.ftbultimine.FTBUltimine
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData
import dev.ftb.mods.ftbultimine.api.rightclick.RightClickHandler
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext
import io.github.chaosunity.createultimine.config.CreateUltimineServerConfig
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.phys.BlockHitResult

object CopycatApplication : RightClickHandler {
    override fun handleRightClickBlock(
        shapeContext: ShapeContext,
        hand: InteractionHand,
        positions: Collection<BlockPos>
    ): Int {
        val player = shapeContext.player
        val level = player.level()
        val blockHitResult = FTBUltiminePlayerData.rayTrace(player) as? BlockHitResult ?: return 0
        var didWork = 0
        val heldItem = player.getItemInHand(hand)
        val playerData = FTBUltimine.getInstance().getOrCreatePlayerData(player)
        val isPressed = playerData.isPressed

        if (!CreateUltimineServerConfig.COPYCAT_APPLICATION.get()) {
            val blockState = level.getBlockState(blockHitResult.blockPos)

            if (blockState.block !is CopycatBlock)
                return 0

            playerData.isPressed = false

            val currentHitResult = blockHitResult.withPosition(blockHitResult.blockPos)
            val result = blockState.useItemOn(heldItem, level, player, hand, currentHitResult)

            playerData.isPressed = isPressed
            return if (result == ItemInteractionResult.SUCCESS) 1 else 0
        }

        for (pos in positions) {
            val blockState = level.getBlockState(pos)

            if (blockState.block !is CopycatBlock)
                continue

            val currentHitResult = blockHitResult.withPosition(pos)
            val result = blockState.useItemOn(heldItem, level, player, hand, currentHitResult)

            if (result != ItemInteractionResult.SUCCESS)
                continue

            didWork++
        }

        playerData.isPressed = isPressed

        return didWork
    }
}