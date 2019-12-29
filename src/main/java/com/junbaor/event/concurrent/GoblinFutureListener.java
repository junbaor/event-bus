package com.junbaor.event.concurrent;

import org.jetbrains.annotations.NotNull;

public interface GoblinFutureListener<T> {

    void futureCompleted(@NotNull GoblinFuture<T> future);

}
