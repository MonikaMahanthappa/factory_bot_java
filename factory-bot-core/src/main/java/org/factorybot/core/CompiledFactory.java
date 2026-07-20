package org.factorybot.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

/** A factory flattened for one build: parent chain + base traits + call-time traits merged by name. */
final class CompiledFactory<T> {

    final Class<T> type;
    final Supplier<T> constructor;
    final LinkedHashMap<String, Attribute<T>> attributes;
    final List<Callback<T>> callbacks;

    CompiledFactory(Class<T> type, Supplier<T> constructor,
                    LinkedHashMap<String, Attribute<T>> attributes, List<Callback<T>> callbacks) {
        this.type = type;
        this.constructor = constructor;
        this.attributes = attributes;
        this.callbacks = callbacks;
    }

    Attribute<T> attribute(String name) {
        return attributes.get(name);
    }
}
