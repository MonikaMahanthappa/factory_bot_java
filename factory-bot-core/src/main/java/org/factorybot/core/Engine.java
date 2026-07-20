package org.factorybot.core;

import java.util.List;
import java.util.Map;

/**
 * Internal callback surface that an {@link Evaluator} uses to build associations and reach shared
 * services, without statically coupling to {@link FactoryBot}. Implemented by {@link FactoryBot}.
 */
interface Engine {

    <X> X run(Class<? extends Factory<X>> factoryClass, Strategy strategy,
              Map<String, Object> overrides, List<String> traits);

    net.datafaker.Faker faker();

    PersistenceHandler persistence();
}
