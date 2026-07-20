package org.factorybot.core.example;

import org.factorybot.core.Definition;
import org.factorybot.core.Factory;

/** Exercises most of the DSL: dynamic + dependent attributes, an inline sequence, an association,
 *  a transient attribute driving an after-build callback, and traits. */
public class UserFactory extends Factory<User> {

    public UserFactory() {
        super(User.class);
    }

    @Override
    protected void define(Definition<User> f) {
        f.attr(User::setFirstName, () -> faker().name().firstName());
        f.attr(User::setLastName, () -> faker().name().lastName());

        // Dependent attribute: reads other attributes' resolved values via the evaluator.
        f.attr(User::setEmail, ev ->
                (ev.get(User::getFirstName) + "." + ev.get(User::getLastName) + "@example.com").toLowerCase());

        // Inline, factory-scoped sequence.
        f.sequence(User::setLogin, n -> "user" + n);

        // Association: built with the parent's strategy (build→build, create→create).
        f.association(User::setAccount, AccountFactory.class);

        // Transient attribute, not assigned to the product but visible to callbacks.
        f.transientAttr("postCount", 0);
        f.afterBuild((user, ev) -> user.setNotes("posts=" + ev.get("postCount", Integer.class)));

        f.trait("admin", t -> t
                .attr(User::setRole, () -> Role.ADMIN)
                .attr(User::setActive, () -> true));

        f.trait("inactive", t -> t.attr(User::setActive, () -> false));
    }
}
