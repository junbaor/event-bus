package com.junbaor.event.concurrent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;

public class GoblinExecutionException extends RuntimeException {
    private static final long serialVersionUID = -5355808569842280913L;

    public GoblinExecutionException(@NotNull ExecutionException cause) {
        super(cause.getMessage(), cause);
    }

    @Nullable
    public Throwable getTargetException() {
        return getCause().getCause();
    }
}
