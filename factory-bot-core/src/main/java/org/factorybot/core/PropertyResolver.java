package org.factorybot.core;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Recovers the JavaBean property name that a serializable method reference targets.
 *
 * <p>{@code User::setFirstName} → {@code "firstName"}, {@code User::getFirstName} → {@code "firstName"},
 * {@code User::isActive} → {@code "active"}, and a record accessor {@code User::firstName} → {@code "firstName"}.
 *
 * <p>Resolution serializes the lambda once to obtain its {@link SerializedLambda#getImplMethodName()},
 * then caches the result keyed by the lambda's synthesized class. Each distinct method-reference call
 * site in source produces a stable class, so the refltaction/serialization cost is paid once per site.
 * See ADR-0003 for the trade-off discussion.
 */
final class PropertyResolver {

    private static final ConcurrentHashMap<Class<?>, String> CACHE = new ConcurrentHashMap<>();

    private PropertyResolver() {
    }

    static String propertyName(Serializable methodRef) {
        return CACHE.computeIfAbsent(methodRef.getClass(), c -> resolve(methodRef));
    }

    private static String resolve(Serializable methodRef) {
        SerializedLambda lambda = serializedLambda(methodRef);
        String impl = lambda.getImplMethodName();
        return stripAccessorPrefix(impl);
    }

    private static SerializedLambda serializedLambda(Serializable methodRef) {
        try {
            Method writeReplace = methodRef.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            Object replacement = writeReplace.invoke(methodRef);
            if (replacement instanceof SerializedLambda sl) {
                return sl;
            }
            throw new FactoryBotException(
                    "Expected a method reference (e.g. User::setName), but got a lambda that cannot be "
                            + "introspected. Use a method reference, or the String-keyed attribute overload.");
        } catch (ReflectiveOperationException e) {
            throw new FactoryBotException(
                    "Could not introspect the method reference to recover its property name. "
                            + "Use a method reference (e.g. User::setName), or the String-keyed attribute overload.",
                    e);
        }
    }

    private static String stripAccessorPrefix(String methodName) {
        if (methodName.startsWith("set") && methodName.length() > 3) {
            return decapitalize(methodName.substring(3));
        }
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return decapitalize(methodName.substring(3));
        }
        if (methodName.startsWith("is") && methodName.length() > 2 && Character.isUpperCase(methodName.charAt(2))) {
            return decapitalize(methodName.substring(2));
        }
        // Record accessor or already-bare name (e.g. User::firstName).
        return methodName;
    }

    private static String decapitalize(String s) {
        if (s.isEmpty()) {
            return s;
        }
        // Match JavaBeans: leave leading acronyms (e.g. "URL") intact.
        if (s.length() > 1 && Character.isUpperCase(s.charAt(1))) {
            return s;
        }
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
