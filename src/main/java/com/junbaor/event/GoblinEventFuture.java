package com.junbaor.event;

import com.junbaor.event.concurrent.GoblinFuture;
import com.junbaor.event.function.Block1;
import org.jetbrains.annotations.NotNull;

/**
 * Goblin event future abstraction.
 *
 * @author Xiaohai Zhang
 * @since Dec 3, 2019
 */
public interface GoblinEventFuture extends GoblinFuture<GoblinEventContext> {

    /**
     * Callback the specified action in case of event future discarded.
     */
    void addDiscardListener(@NotNull Block1<GoblinEventContext> action);
}
