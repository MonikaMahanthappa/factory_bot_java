package org.factorybot.core;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongFunction;

/**
 * A monotonic value generator, mirroring factory_bot's sequences.
 *
 * <p>Each call to {@link #next()} yields the next formatted value. Sequences may be global
 * (registered on {@link FactoryBot}) or inline (scoped to a single factory attribute).
 */
public final class Sequence<V> {

    private final AtomicLong counter;
    private final LongFunction<V> formatter;

    Sequence(long start, LongFunction<V> formatter) {
        this.counter = new AtomicLong(start);
        this.formatter = formatter;
    }

    /** Returns the value for the current counter and advances it by one. */
    public V next() {
        long n = counter.getAndIncrement();
        return formatter.apply(n);
    }

    /** Resets the counter back to its starting value. */
    public void rewind(long start) {
        counter.set(start);
    }
}
