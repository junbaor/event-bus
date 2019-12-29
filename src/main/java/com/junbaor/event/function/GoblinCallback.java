package com.junbaor.event.function;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface GoblinCallback<E> extends Callable<E> {
}
