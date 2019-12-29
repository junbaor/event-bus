package com.junbaor.event.listener

import com.junbaor.event.GoblinEvent
import com.junbaor.event.GoblinEventContext
import com.junbaor.event.GoblinEventListener
import com.junbaor.event.function.Ordered
import com.junbaor.event.service.GoblinManagedBean
import com.junbaor.event.service.GoblinManagedObject
import com.junbaor.event.util.ObjectUtils
import com.junbaor.event.util.RandomUtils
import com.junbaor.event.util.StopWatch
import java.util.*
import java.util.concurrent.atomic.LongAdder
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@GoblinManagedBean(type = "Core", name = "GoblinEventListener")
class GoblinEventListenerImpl
internal constructor(private val delegator: GoblinEventListener)
    : GoblinManagedObject(), GoblinEventListener, Ordered, GoblinEventListenerMXBean {

    private val id = RandomUtils.nextObjectId()
    private val watch = StopWatch()
    private val acceptedCount = LongAdder()
    private val rejectedCount = LongAdder()
    private val succeedCount = LongAdder()
    private val failedCount = LongAdder()

    private val lock = ReentrantReadWriteLock()
    private val counts = IdentityHashMap<Class<out GoblinEvent>, GoblinEventCount>()

    override fun getOrder(): Int {
        return ObjectUtils.calculateOrder(delegator)
    }

    override fun accept(context: GoblinEventContext): Boolean {
        val count = count(context.event.javaClass)
        val ret = delegator.accept(context)
        if (ret) {
            acceptedCount.increment()
            count.acceptedCount.increment()
        } else {
            rejectedCount.increment()
            count.rejectedCount.increment()
        }
        return ret
    }

    override fun onEvent(context: GoblinEventContext) {
        val count = count(context.event.javaClass)
        try {
            delegator.onEvent(context)
            succeedCount.increment()
            count.succeedCount.increment()
        } catch (ex: Exception) {
            failedCount.increment()
            count.failedCount.increment()
            throw ex
        }
    }

    private fun count(type: Class<out GoblinEvent>): GoblinEventCount {
        return lock.read { counts[type] } ?: lock.write {
            counts[type] ?: kotlin.run {
                val count = GoblinEventCount(type)
                counts[type] = count
                count
            }
        }
    }

    override fun disposeBean() {
        lock.write {
            counts.values.forEach { it.dispose() }
            counts.clear()
        }
        watch.stop()
    }

    override fun getId(): String {
        return id
    }

    override fun getUpTime(): String {
        return watch.toString()
    }

    override fun getListener(): String {
        return delegator.toString()
    }

    override fun getAcceptedCount(): Long {
        return acceptedCount.sum()
    }

    override fun getRejectedCount(): Long {
        return rejectedCount.sum()
    }

    override fun getSucceedCount(): Long {
        return succeedCount.sum()
    }

    override fun getFailedCount(): Long {
        return failedCount.sum()
    }

    override fun getEventCountList(): Array<GoblinEventCountMXBean> {
        return lock.read { counts.values.sortedByDescending { it.getAcceptedCount() }.toTypedArray() }
    }

    override fun toString(): String {
        return "GoblinEventListener:$delegator"
    }
}