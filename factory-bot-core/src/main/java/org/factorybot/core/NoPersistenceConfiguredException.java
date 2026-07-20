package org.factorybot.core;

/** Thrown when {@code create}/{@code createList} is called but no {@link PersistenceHandler} is configured. */
public class NoPersistenceConfiguredException extends FactoryBotException {

    public NoPersistenceConfiguredException() {
        super("create(...) requires a PersistenceHandler. factory-bot-core builds objects only; "
                + "add factory-bot-spring-data-jpa (or call FactoryBot.setPersistenceHandler(...)) to persist. "
                + "Use build(...) for a non-persisted object.");
    }
}
