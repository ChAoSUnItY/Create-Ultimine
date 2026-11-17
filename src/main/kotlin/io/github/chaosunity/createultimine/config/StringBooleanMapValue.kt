package io.github.chaosunity.createultimine.config

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag
import dev.ftb.mods.ftblibrary.snbt.config.BaseValue
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig

class StringBooleanMapValue(config: SNBTConfig?, name: String, def: Map<String, Boolean>) :
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

    override fun createClientConfig(group: ConfigGroup?) {
        val group = group?.getOrCreateSubgroup(key)

        for ((key, value) in get()) {
            val configEntry = group?.addBool(key, value, {
                val modified = get().toMutableMap()
                modified[key] = it
                set(modified)
            }, true)
            configEntry?.nameKey = key
            configEntry?.canEdit = enabled.asBoolean
        }
    }
}