package org.factorybot.core;

/** Thrown when dependent attributes reference each other in a cycle (e.g. a depends on b depends on a). */
public class CircularAttributeDependencyException extends FactoryBotException {

    public CircularAttributeDependencyException(String attributeName) {
        super("Circular attribute dependency detected while resolving '" + attributeName + "'.");
    }
}
