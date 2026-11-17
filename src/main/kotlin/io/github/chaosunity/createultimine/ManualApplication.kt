package io.github.chaosunity.createultimine

import com.simibubi.create.AllRecipeTypes
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe
import dev.ftb.mods.ftbultimine.api.rightclick.RightClickHandler
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext
import io.github.chaosunity.createultimine.config.CreateUltimineServerConfig
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimplePreparableReloadListener
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.event.AddReloadListenerEvent

object ManualApplication : RightClickHandler {
    fun isManualApplicable(level: Level, blockState: BlockState, heldItem: ItemStack): ManualApplicationRecipe? {
        val recipeType: RecipeType<ManualApplicationRecipe> = AllRecipeTypes.ITEM_APPLICATION.getType()
        // This will respect overlapped recipes, though in practice, recipe design should not have this
        // at the beginning
        val recipe = level.recipeManager
            .getAllRecipesFor(recipeType)
            .firstNotNullOfOrNull {
                val mar = it.value

                if (!(mar.testBlock(blockState) && mar.ingredients[1].test(heldItem)))
                    return@firstNotNullOfOrNull null

                if (CreateUltimineServerConfig.MANUAL_APPLICATIONS.get()[it.id().toString()] != true)
                    return@firstNotNullOfOrNull null

                return@firstNotNullOfOrNull it
            } ?: return null

        return recipe.value
    }

    override fun handleRightClickBlock(
        shapeContext: ShapeContext,
        hand: InteractionHand,
        positions: Collection<BlockPos>
    ): Int {
        val player = shapeContext.player
        val level = player.level()
        var didWork = 0
        val heldItem = player.getItemInHand(hand)

        for (pos in positions) {
            val recipe = isManualApplicable(level, level.getBlockState(pos), heldItem) ?: continue
            val applicationResult = manualApplicationRecipesApplyInWorld(
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
        recipe: ManualApplicationRecipe
    ): Boolean {
        val blockState = level.getBlockState(pos)

        if (heldItem.isEmpty)
            return false
        if (blockState.isAir)
            return false

        level.destroyBlock(pos, false)
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

        return true
    }

    object ManualApplicationSettings {
        fun registerListener(event: AddReloadListenerEvent) {
            event.addListener(object : SimplePreparableReloadListener<Unit>() {
                override fun prepare(
                    manager: ResourceManager,
                    profiler: ProfilerFiller
                ) = Unit

                override fun apply(
                    data: Unit,
                    manager: ResourceManager,
                    profiler: ProfilerFiller
                ) {
                    val recipeType: RecipeType<ManualApplicationRecipe> = AllRecipeTypes.ITEM_APPLICATION.getType()
                    val cachedConfig = CreateUltimineServerConfig
                        .MANUAL_APPLICATIONS
                        .get()
                    val cachedIds = cachedConfig
                        .mapNotNull { ResourceLocation.tryParse(it.key) }
                        .toSet()
                    val loadedIds = event.serverResources.recipeManager
                        .getAllRecipesFor(recipeType)
                        .map { it.id }
                        .toSet()

                    val recipeIds = loadedIds - cachedIds
                    val updatedConfig = cachedConfig.filter {
                        ResourceLocation.tryParse(it.key)?.let(loadedIds::contains) == true
                    }.toMutableMap()

                    for (id in recipeIds) {
                        updatedConfig.putIfAbsent(id.toString(), true)
                    }

                    CreateUltimineServerConfig.MANUAL_APPLICATIONS.set(updatedConfig)
                }
            })
        }
    }
}
