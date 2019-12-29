package com.junbaor.event.util;

import com.junbaor.event.function.Ordered;
import org.jetbrains.annotations.NotNull;

abstract public class ObjectUtils {

    public static int calculateOrder(@NotNull Object obj) {
        if (obj instanceof Ordered) {
            return ((Ordered) obj).getOrder();
        }
        return 0;
    }

}
