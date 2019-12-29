package com.junbaor.event.function;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ValueWrapper<E> {

    @Nullable
    E getValue();

}
