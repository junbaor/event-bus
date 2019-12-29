package com.junbaor.event.util;

abstract public class SystemUtils {

    public static int estimateThreads() {
        int processors = Runtime.getRuntime().availableProcessors();
        return Math.max(processors, 2);
    }

}
