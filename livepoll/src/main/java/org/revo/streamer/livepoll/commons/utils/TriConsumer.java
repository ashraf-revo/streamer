package org.revo.streamer.livepoll.commons.utils;

import java.util.Objects;

public interface TriConsumer<T, U, R> {
    void accept(T var1, U var2, R var3);


    default TriConsumer<T, U, R> andThen(TriConsumer<? super T, ? super U, ? super R> after) {
        Objects.requireNonNull(after);
        return (t, u, r) -> {
            this.accept(t, u, r);
            after.accept(t, u, r);
        };
    }

}
