package com.junbaor.event.util;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

abstract public class RandomUtils {

    @NotNull
    public static String nextObjectId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
