package io.github.chaosunity.createultimine

import com.simibubi.create.AllTags.AllItemTags
import com.simibubi.create.content.equipment.wrench.WrenchItem
import dev.ftb.mods.ftbultimine.FTBUltimine
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData
import dev.ftb.mods.ftbultimine.api.rightclick.RightClickHandler
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext
import io.github.chaosunity.createultimine.config.CreateUltimineServerConfig
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.phys.BlockHitResult

object WrenchUse : RightClickHandler {
    override fun handleRightClickBlock(
        shapeContext: ShapeContext,
        hand: InteractionHand,
        positions: Collection<BlockPos>
    ): Int {
        val player = shapeContext.player
        val itemStack = player.getItemInHand(hand)

        if (!CreateUltimineServerConfig.RIGHT_CLICK_WRENCH.get() ||
            itemStack.item !is WrenchItem ||
            !AllItemTags.WRENCH.matches(itemStack.item)
        )
            return 0

        var didWork = 0
        val blockHitResult = FTBUltiminePlayerData.rayTrace(player) as? BlockHitResult ?: return 0
        val playerData = FTBUltimine.instance.getOrCreatePlayerData(player)
        val isPressed = playerData.isPressed
        playerData.isPressed = false

        for (pos in positions) {
            val currentHitResult = blockHitResult.withPosition(pos)
            val context = UseOnContext(player, hand, currentHitResult)
            val result = itemStack.useOn(context)

            if (result != InteractionResult.SUCCESS)
                continue

            didWork++
        }

        playerData.isPressed = isPressed

        return didWork
    }
}