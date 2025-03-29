package io.github.chaosunity.createultimine

import com.simibubi.create.AllBlocks
import com.simibubi.create.AllItems
import com.simibubi.create.content.equipment.wrench.WrenchItem
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.BlockHitResult
import net.neoforged.neoforge.common.Tags

object RightClickHandlers {
    val alloyApplications: Map<Item, Pair<(BlockState) -> Boolean, BlockState>> = mapOf(
        AllItems.ANDESITE_ALLOY.get() to (
                {
                    state: BlockState ->
                        state.tags.anyMatch { it == Tags.Blocks.STRIPPED_LOGS }
                } to AllBlocks.ANDESITE_CASING.defaultState
        ),
        AllItems.BRASS_INGOT.get() to (
                {
                    state: BlockState ->
                        state.tags.anyMatch { it == Tags.Blocks.STRIPPED_LOGS }
                } to AllBlocks.BRASS_CASING.defaultState
        ),
        Items.COPPER_INGOT to (
                {
                    state: BlockState ->
                        state.tags.anyMatch { it == Tags.Blocks.STRIPPED_LOGS }
                } to AllBlocks.COPPER_CASING.defaultState
        ),
        AllItems.STURDY_SHEET.get() to (
                {
                    state: BlockState ->
                        state.block == AllBlocks.BRASS_CASING.get()
                } to AllBlocks.RAILWAY_CASING.defaultState
        )
    )

    fun alloyApply(
        player: ServerPlayer,
        hand: InteractionHand,
        clickPos: BlockPos,
        data: FTBUltiminePlayerData,
    ): Int {
        var didWork = 0
        val itemStack = player.getItemInHand(hand)
        val (predicate, newState) = alloyApplications[itemStack.item] ?: return 0
        val level = player.level()

        for (pos in data.cachedPositions()) {
            val state = level.getBlockState(pos)

            if (!predicate(state))
                continue

            player.level().setBlock(pos, newState, 11)
            player.level().gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState))
            if (!player.isCreative)
                itemStack.shrink(1)
            didWork++

            if (itemStack.isEmpty)
                break
        }

        if (didWork > 0) {
            level.playSound(null, clickPos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1f, 1f)
            level.playSound(null, clickPos, SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 1f, 1f)
        }

        return didWork
    }

    fun onWrenchUse(player: ServerPlayer, hand: InteractionHand, blockHitResult: BlockHitResult, data: FTBUltiminePlayerData): Int {
        var didWork = 0
        val itemStack = player.getItemInHand(hand)

        if (itemStack.item !is WrenchItem)
            return 0

        val isPressed = data.isPressed
        data.isPressed = false

        for (pos in data.cachedPositions()) {
            val currentHitResult = blockHitResult.withPosition(pos)
            val context = UseOnContext(player, hand, currentHitResult)
            val result = itemStack.useOn(context)

            if (result != InteractionResult.SUCCESS)
                continue

            didWork++
        }

        data.isPressed = isPressed

        return didWork
    }
}