package com.junbaor.event.worker

import com.junbaor.event.GoblinEventListener
import com.junbaor.event.context.GoblinEventContextImpl
import java.util.concurrent.atomic.LongAdder

class EventBusWorkerEvent {

    var taskId: Int? = null
    var ctx: GoblinEventContextImpl? = null
    var listeners: List<GoblinEventListener>? = null
    var receivedCount: LongAdder? = null
    var succeedCount: LongAdder? = null
    var failedCount: LongAdder? = null

    fun clear() {
        taskId = null
        ctx = null
        listeners = null
        receivedCount = null
        succeedCount = null
        failedCount = null
    }
}