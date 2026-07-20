package org.factorybot.core.example;

import org.factorybot.core.Definition;
import org.factorybot.core.Factory;

/** Demonstrates inheritance: inherits every UserFactory attribute, overriding only the role. */
public class AdminFactory extends Factory<User> {

    public AdminFactory() {
        super(User.class);
    }

    @Override
    protected void define(Definition<User> f) {
        f.parent(UserFactory.class);
        f.attr(User::setRole, () -> Role.ADMIN);
        f.attr(User::setActive, () -> true);
    }
}
