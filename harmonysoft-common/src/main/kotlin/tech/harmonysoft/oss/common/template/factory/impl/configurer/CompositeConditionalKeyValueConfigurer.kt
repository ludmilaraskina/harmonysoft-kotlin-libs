package tech.harmonysoft.oss.common.template.factory.impl.configurer

import tech.harmonysoft.oss.common.collection.CollectionInitializer
import tech.harmonysoft.oss.common.data.DataModificationStrategy
import tech.harmonysoft.oss.common.template.service.KeyValueConfigurationContext
import tech.harmonysoft.oss.common.template.service.KeyValueConfigurer

class CompositeConditionalKeyValueConfigurer<K>(
    val configurers: Array<ConditionalConfigurer<K>>
) : KeyValueConfigurer<K> {

    override val keys = configurers.flatMap { it.condition.keys + it.configurer.keys }.toSet()

    override val staticConfiguration = configurers.fold(mutableMapOf<K, MutableSet<Any>>()) { acc, configurer ->
        acc.apply {
            for ((key, value) in configurer.configurer.staticConfiguration) {
                getOrPut(key, CollectionInitializer.mutableSet()).addAll(value)
            }
        }
    }

    override fun configure(toConfigure: DataModificationStrategy<K>, context: KeyValueConfigurationContext<K>) {
        for (configurer in configurers) {
            if (configurer.condition.match(context)) {
                configurer.configurer.configure(toConfigure, context)
                break
            }
        }
    }

    override fun hashCode(): Int {
        return configurers.contentHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is CompositeConditionalKeyValueConfigurer<*> && configurers.contentEquals(other.configurers)
    }

    override fun toString(): String {
        return configurers.joinToString()
    }
}