package org.factorybot.core;

/** Base unchecked exception for all factory_bot_java errors. */
public class FactoryBotException extends RuntimeException {

    public FactoryBotException(String message) {
        super(message);
    }

    public FactoryBotException(String message, Throwable cause) {
        super(message, cause);
    }
}
