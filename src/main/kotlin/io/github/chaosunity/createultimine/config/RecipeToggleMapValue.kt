package io.github.chaosunity.createultimine.config

import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe
import dev.ftb.mods.ftblibrary.config.BooleanConfig
import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag
import dev.ftb.mods.ftblibrary.snbt.config.BaseValue
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig
import dev.ftb.mods.ftblibrary.util.TooltipList
import io.github.chaosunity.createultimine.asTranslationKey
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Ingredient
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.fml.ModList
import kotlin.jvm.optionals.getOrNull

class RecipeToggleMapValue(config: SNBTConfig?, name: String, def: Map<String, Boolean>) :
    BaseValue<Map<String, Boolean>>(config, name, def) {
    init {
        set(def)
    }

    override fun write(tag: SNBTCompoundTag?) {
        val map = get()
        val mapTag = SNBTCompoundTag()

        for ((key, value) in map) {
            mapTag.putBoolean(key, value)
        }

        tag?.put(key, mapTag)
    }

    override fun read(tag: SNBTCompoundTag?) {
        val map = mutableMapOf<String, Boolean>()
        val mapTag = tag?.getCompound(key) ?: return

        for (key in mapTag.allKeys) {
            map[key] = mapTag.getBoolean(key)
        }

        set(map)
    }

    @OnlyIn(Dist.CLIENT)
    override fun createClientConfig(group: ConfigGroup?) {
        val group = group?.getOrCreateSubgroup(key)

        for ((key, value) in get()) {
            val recipeResourceLocation = ResourceLocation.tryParse(key)
            val modGroup = recipeResourceLocation?.run {
                val group = group?.getOrCreateSubgroup(namespace)

                group?.nameKey = ModList
                    .get()
                    .getModContainerById(namespace)
                    .getOrNull()
                    ?.modInfo
                    ?.displayName
                group
            } ?: group

            val configEntry = modGroup?.add(key, RecipeToggleConfig(key), value, {
                val modified = get().toMutableMap()
                modified[key] = it
                set(modified)
            }, true)
            configEntry?.nameKey = key
            configEntry?.canEdit = enabled.asBoolean
        }
    }

    private class RecipeToggleConfig(private val recipeId: String) : BooleanConfig() {
        override fun addInfo(list: TooltipList?) {
            val recipeManager = Minecraft.getInstance().level?.recipeManager ?: return
            val recipe = ResourceLocation.tryParse(recipeId)
                ?.let(recipeManager::byKey)
                ?.getOrNull()
                ?.value() as? ManualApplicationRecipe ?: return
            var component: Component

            list?.add(
                Component
                    .translatable(asTranslationKey("config.apply.tooltip"))
                    .withStyle(ChatFormatting.YELLOW)
            )

            addIngredientsInfo(list, recipe.ingredients[1])

            list?.add(
                Component
                    .translatable(asTranslationKey("config.onto.tooltip"))
                    .withStyle(ChatFormatting.YELLOW)
            )

            addIngredientsInfo(list, recipe.ingredients[0])

            list?.add(
                Component
                    .translatable(asTranslationKey("config.produce.tooltip"))
                    .withStyle(ChatFormatting.YELLOW)
            )

            val rollableResults = recipe.rollableResults

            for (result in rollableResults) {
                component = Component
                    .literal("")
                    .append(result.stack.displayName)
                    .withStyle(ChatFormatting.GRAY)
                list?.add(component)
            }

            if (rollableResults.size > 5) {
                component = Component
                    .translatable(asTranslationKey("config.more.tooltip"))
                    .withStyle(ChatFormatting.DARK_GRAY)
                list?.add(component)
            }

            list?.add(Component.literal(""))

            super.addInfo(list)
        }

        private fun addIngredientsInfo(list: TooltipList?, ingredient: Ingredient) {
            val values = ingredient.values
            var component: Component

            for (value in values.take(5)) {
                component = Component
                    .literal("")
                    .append(getValueText(value))
                    .withStyle(ChatFormatting.GRAY)
                list?.add(component)
            }

            if (values.size > 5) {
                component = Component
                    .translatable(asTranslationKey("config.more.tooltip"))
                    .withStyle(ChatFormatting.DARK_GRAY)
                list?.add(component)
            }
        }

        private fun getValueText(value: Ingredient.Value): Component {
            return when (value) {
                is Ingredient.ItemValue -> {
                    value.item.displayName
                }

                is Ingredient.TagValue -> {
                    Component.literal(value.tag.location.toString())
                }

                else -> {
                    throw IllegalArgumentException("Unsupported value type: ${value.javaClass.name}")
                }
            }
        }
    }
}