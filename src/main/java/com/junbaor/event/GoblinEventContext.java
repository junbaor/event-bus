package com.junbaor.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface GoblinEventContext {

    @NotNull
    String getChannel();

    @NotNull
    GoblinEvent getEvent();

    boolean isSuccess();

    boolean isDiscard();

    @NotNull
    Map<String, Object> getExtensions();

    @Nullable
    Object getExtension(@NotNull String name);

    @Nullable
    Object removeExtension(@NotNull String string);

    @Nullable
    Object setExtension(@NotNull String name, @NotNull Object extension);

}
