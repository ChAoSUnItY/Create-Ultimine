package io.github.chaosunity.createultimine

import com.simibubi.create.AllRecipeTypes
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe
import dev.ftb.mods.ftbultimine.FTBUltiminePlayerData
import dev.ftb.mods.ftbultimine.api.rightclick.RightClickHandler
import dev.ftb.mods.ftbultimine.shape.ShapeContext
import io.github.chaosunity.createultimine.config.CreateUltimineServerConfig
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimplePreparableReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraftforge.event.AddReloadListenerEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent

object ManualApplication : RightClickHandler {
    fun isManualApplicable(level: Level, blockState: BlockState, heldItem: ItemStack): ManualApplicationRecipe? {
        val recipeType: RecipeType<ManualApplicationRecipe> = AllRecipeTypes.ITEM_APPLICATION.getType()
        // This will respect overlapped recipes, though in practice, recipe design should not have this
        // at the beginning
        val recipe = level.recipeManager
            .getAllRecipesFor(recipeType)
            .firstNotNullOfOrNull {
                if (!(it.testBlock(blockState) && it.ingredients[1].test(heldItem)))
                    return@firstNotNullOfOrNull null

                if (CreateUltimineServerConfig.MANUAL_APPLICATIONS.get()[it.id.toString()] != true)
                    return@firstNotNullOfOrNull null

                return@firstNotNullOfOrNull it
            }

        return recipe
    }

    override fun handleRightClickBlock(
        shapeContext: ShapeContext,
        hand: InteractionHand,
        positions: Collection<BlockPos>
    ): Int {
        var didWork = 0
        val player = shapeContext.player
        val level = player.level()
        val blockHitResult = FTBUltiminePlayerData.rayTrace(player) as? BlockHitResult ?: return 0

        for (pos in positions) {
            if (isManualApplicable(level, level.getBlockState(pos), player.getItemInHand(hand)) == null)
                continue

            blockHitResult.withPosition(pos)
            val simulatedClickEvent = PlayerInteractEvent.RightClickBlock(player, hand, pos, blockHitResult)
            ManualApplicationRecipe.manualApplicationRecipesApplyInWorld(simulatedClickEvent)

            if (simulatedClickEvent.isCancelable && simulatedClickEvent.isCanceled) didWork++
            else break
        }

        return didWork
    }

    object ManualApplicationSettings {
        private var recipes: Map<String, Boolean> = mapOf()
        
        fun syncToConfig() {
            CreateUltimineServerConfig.MANUAL_APPLICATIONS.set(recipes)
        }

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
                    
                    recipes = updatedConfig
                    syncToConfig()
                }
            })
        }
    }
}
