package io.github.chaosunity.createultimine

import com.simibubi.create.AllRecipeTypes
import com.simibubi.create.AllTags
import com.simibubi.create.content.equipment.wrench.WrenchItem
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraftforge.event.entity.player.PlayerInteractEvent

object RightClickHandlers {
    fun isManualApplicable(level: Level, blockState: BlockState, heldItem: ItemStack): ManualApplicationRecipe? =
        level.recipeManager
            .getAllRecipesFor(AllRecipeTypes.ITEM_APPLICATION.getType<RecipeType<ManualApplicationRecipe>>())
            .firstOrNull { it.testBlock(blockState) && it.ingredients[1].test(heldItem) }

    fun itemApplication(
        player: ServerPlayer,
        hand: InteractionHand,
        blockHitResult: BlockHitResult,
        data: FTBUltiminePlayerData,
    ): Int {
        var didWork = 0

        for (pos in data.cachedPositions()) {
            blockHitResult.withPosition(pos)
            val simulatedClickEvent = PlayerInteractEvent.RightClickBlock(player, hand, pos, blockHitResult)
            ManualApplicationRecipe.manualApplicationRecipesApplyInWorld(simulatedClickEvent)

            if (simulatedClickEvent.isCancelable && simulatedClickEvent.isCanceled) didWork++
            else break
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

        if (itemStack.item !is WrenchItem ||
            !AllTags.AllItemTags.WRENCH.matches(itemStack))
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
