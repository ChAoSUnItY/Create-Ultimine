package io.github.chaosunity.createultimine

import com.simibubi.create.AllRecipeTypes
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe
import dev.ftb.mods.ftbultimine.api.rightclick.RightClickHandler
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext
import io.github.chaosunity.createultimine.config.CreateUltimineServerConfig
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.items.wrapper.RecipeWrapper

object ManualApplication : RightClickHandler {
    fun isManualApplicable(level: Level, blockState: BlockState, heldItem: ItemStack): ManualApplicationRecipe? {
        val recipeType: RecipeType<Recipe<RecipeWrapper>> = AllRecipeTypes.ITEM_APPLICATION.getType()

        return level.recipeManager
            .getAllRecipesFor(recipeType)
            .firstOrNull {
                val mar = it.value as ManualApplicationRecipe
                mar.testBlock(blockState) && mar.ingredients[1].test(heldItem)
            }?.value as? ManualApplicationRecipe
    }

    override fun handleRightClickBlock(
        shapeContext: ShapeContext,
        hand: InteractionHand,
        positions: Collection<BlockPos>
    ): Int {
        if (!CreateUltimineServerConfig.MANUAL_APPLICATION.get())
            return 0

        val player = shapeContext.player
        val level = player.level()
        var didWork = 0
        val heldItem = player.getItemInHand(hand)

        for (pos in positions) {
            val recipe = isManualApplicable(level, level.getBlockState(pos), heldItem) ?: break
            val (applicationResult, _) = manualApplicationRecipesApplyInWorld(
                level,
                player,
                hand,
                heldItem,
                pos,
                recipe
            )

            if (applicationResult) didWork++
            else continue

            level.playSound(null, pos, SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 1f, 1f)

            if (heldItem.isEmpty)
                break
        }

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
            val recipeType: RecipeType<Recipe<RecipeWrapper>> = AllRecipeTypes.ITEM_APPLICATION.getType()

            recipe = level.recipeManager
                .getAllRecipesFor(recipeType)
                .firstOrNull { it ->
                    val mar = it.value as ManualApplicationRecipe
                    mar.testBlock(blockState) && mar.ingredients[1].test(heldItem)
                }?.value as? ManualApplicationRecipe ?: return false to null
        }

        val transformedBlock = recipe.transformBlock(blockState, level.random)
        level.setBlock(pos, transformedBlock, 3)
        recipe.rollResults(level.random)
            .forEach { Block.popResource(level, pos, it) }

        val unbreakable = heldItem.has(DataComponents.UNBREAKABLE)
        val keepHeld = recipe.shouldKeepHeldItem() || player.isCreative

        if (!unbreakable && !keepHeld) {
            if (heldItem.isDamageableItem)
                heldItem.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand))
            else
                heldItem.shrink(1)
        }

        return true to recipe
    }
}
