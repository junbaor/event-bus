package com.junbaor.event.util;

import java.util.concurrent.atomic.AtomicBoolean;

public class StopWatch {

    private long startTime;
    private long endTime;

    private AtomicBoolean started = new AtomicBoolean();
    private AtomicBoolean stopped = new AtomicBoolean();

    public StopWatch() {
    }

    public StopWatch(boolean autoStart) {
        if (autoStart) {
            start();
        }
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            startTime = System.currentTimeMillis();
        }
    }

    public void stop() {
        if (stopped.compareAndSet(false, true)) {
            endTime = System.currentTimeMillis();
        }
    }

    public String toString() {
        return (endTime - startTime) + " ms";
    }
}
