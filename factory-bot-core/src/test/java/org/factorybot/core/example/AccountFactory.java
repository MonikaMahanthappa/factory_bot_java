package org.factorybot.core.example;

import org.factorybot.core.Definition;
import org.factorybot.core.Factory;

public class AccountFactory extends Factory<Account> {

    public AccountFactory() {
        super(Account.class);
    }

    @Override
    protected void define(Definition<Account> f) {
        f.attr(Account::setName, () -> faker().company().name());
        f.attr(Account::setPlan, () -> "free");

        f.trait("premium", t -> t.attr(Account::setPlan, () -> "premium"));
    }
}
