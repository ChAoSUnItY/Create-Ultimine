package io.github.chaosunity.createultimine

import com.simibubi.create.AllRecipeTypes
import com.simibubi.create.content.equipment.wrench.WrenchItem
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

object RightClickHandlers {
    fun isManualApplicable(level: Level, blockState: BlockState, heldItem: ItemStack): ManualApplicationRecipe? =
        level.recipeManager
            .getAllRecipesFor(AllRecipeTypes.ITEM_APPLICATION.getType<RecipeType<ManualApplicationRecipe>>())
            .firstOrNull { it.testBlock(blockState) && it.ingredients[1].test(heldItem) }

    fun itemApplication(
        player: ServerPlayer,
        hand: InteractionHand,
        clickPos: BlockPos,
        recipe: ManualApplicationRecipe,
        data: FTBUltiminePlayerData,
    ): Int {
        var recipe = recipe
        var didWork = 0
        val heldItem = player.getItemInHand(hand)
        val level = player.level()

        for (pos in data.cachedPositions()) {
            val (applicationResult, updatedRecipe) = manualApplicationRecipesApplyInWorld(
                level,
                player,
                hand,
                heldItem,
                pos,
                recipe
            )

            recipe = updatedRecipe ?: break

            if (applicationResult) didWork++
            else continue

            if (heldItem.isEmpty)
                break
        }

        if (didWork > 0) {
            level.playSound(null, clickPos, SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 1f, 1f)
        }

        return didWork
    }

    fun onWrenchUse(
        player: ServerPlayer,
        hand: InteractionHand,
        blockHitResult: BlockHitResult,
        data: FTBUltiminePlayerData
    ): Int {
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

    private fun manualApplicationRecipesApplyInWorld(
        level: Level,
        player: ServerPlayer,
        hand: InteractionHand,
        heldItem: ItemStack,
        pos: BlockPos,
        cachedRecipe: ManualApplicationRecipe
    ): Pair<Boolean, ManualApplicationRecipe?> {
        var recipe = cachedRecipe
        val blockState = level.getBlockState(pos)

        if (heldItem.isEmpty)
            return false to recipe
        if (blockState.isAir)
            return false to recipe

        level.destroyBlock(pos, false)

        if (!(recipe.testBlock(blockState) && recipe.ingredients[1].test(heldItem))) {
            // Mismatched find other recipes or fail
            recipe = level.recipeManager
                .getAllRecipesFor(AllRecipeTypes.ITEM_APPLICATION.getType<RecipeType<ManualApplicationRecipe>>())
                .firstOrNull { it.testBlock(blockState) && it.ingredients[1].test(heldItem) } ?: return false to null
        }

        val transformedBlock = recipe.transformBlock(blockState)
        level.setBlock(pos, transformedBlock, 3)
        recipe.rollResults()
            .forEach { Block.popResource(level, pos, it) }

        val unbreakable = heldItem.tag?.getBoolean("Unbreakable") == true
        val keepHeld = recipe.shouldKeepHeldItem() || player.isCreative

        if (!unbreakable && !keepHeld) {
            if (heldItem.isDamageableItem)
                heldItem.hurtAndBreak(1, player) { it.broadcastBreakEvent(hand) }
            else
                heldItem.shrink(1)
        }

        return true to recipe
    }
}
