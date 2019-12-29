package com.junbaor.event.worker

import com.junbaor.event.*
import com.junbaor.event.concurrent.NamedDaemonThreadFactory
import com.junbaor.event.context.GoblinEventContextImpl
import com.junbaor.event.exception.EventWorkerBufferFullException
import com.junbaor.event.listener.GoblinEventListenerImpl
import com.junbaor.event.listener.GoblinEventListenerMXBean
import com.junbaor.event.service.GoblinManagedBean
import com.junbaor.event.service.GoblinManagedObject
import com.junbaor.event.service.GoblinManagedStopWatch
import com.junbaor.event.util.RandomUtils
import com.junbaor.event.util.SystemUtils
import com.lmax.disruptor.TimeoutException
import com.lmax.disruptor.dsl.Disruptor
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.LongAdder
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@GoblinManagedBean(type = "Core")
@GoblinManagedStopWatch
class EventBusWorker internal constructor(private val channel: String,
                                          private val bufferSize: Int,
                                          workers: Int)
    : GoblinManagedObject(), EventBusWorkerMXBean {

    companion object {
        private const val DEFAULT_SHUTDOWN_TIMEOUT_IN_SECONDS = 15
    }

    private val id = RandomUtils.nextObjectId()
    private val workers: Int
    private val disruptor: Disruptor<EventBusWorkerEvent>
    private val lock = ReentrantReadWriteLock()
    private val listeners = IdentityHashMap<GoblinEventListener, GoblinEventListenerImpl>()

    private val publishedCount = LongAdder()
    private val discardedCount = LongAdder()
    private val receivedCount = LongAdder()
    private val succeedCount = LongAdder()
    private val failedCount = LongAdder()

    init {
        val threadFactory = NamedDaemonThreadFactory.getInstance("EventBusWorker-$channel")
        val eventFactory = EventBusWorkerEventFactory.INSTANCE
        disruptor = Disruptor<EventBusWorkerEvent>(eventFactory, bufferSize, threadFactory)
        this.workers = if (workers <= 0) SystemUtils.estimateThreads() else workers
        val handlers = Array(this.workers) { EventBusWorkerEventHandler.INSTANCE }
        disruptor.handleEventsWithWorkerPool(*handlers)
        disruptor.start()
        logger.debug("{EventBus} EventBusWorker [$channel] disruptor started")
    }

    internal fun subscribe(listener: GoblinEventListener) {
        lock.write {
            if (listeners[listener] != null) {
                throw IllegalArgumentException("Listener [$listener] already subscribed on channel [$channel]")
            }
            val delegator = GoblinEventListenerImpl(listener)
            listeners[listener] = delegator
            logger.debug("{EventBus} EventBusWorker [$channel] subscribed with [$delegator]")
        }
    }

    internal fun unsubscribe(listener: GoblinEventListener) {
        lock.write { listeners.remove(listener) }?.run {
            this.dispose()
            logger.debug("{EventBus} EventBusWorker [$channel] unsubscribed [$this]")
        }
    }

    internal fun lookup(ctx: GoblinEventContext): List<GoblinEventListener> {
        return lock.read { listeners.values.filter { it.accept(ctx) }.toList() }
    }

    fun publish(taskId: Int, ctx: GoblinEventContextImpl, listeners: List<GoblinEventListener>) {
        val published = disruptor.ringBuffer.tryPublishEvent { e, _ ->
            e.taskId = taskId
            e.ctx = ctx
            e.listeners = listeners
            e.receivedCount = receivedCount
            e.succeedCount = succeedCount
            e.failedCount = failedCount
        }
        if (!published) {
            discardedCount.increment()
            ctx.workerExceptionCaught(taskId, EventWorkerBufferFullException())
            ctx.finishTask()
        } else {
            publishedCount.increment()
        }
    }

    override fun getId(): String {
        return id
    }

    override fun getUpTime(): String? {
        return stopWatch?.toString()
    }

    override fun getChannel(): String {
        return channel
    }

    override fun getBufferSize(): Int {
        return bufferSize
    }

    override fun getRemainingCapacity(): Int {
        return disruptor.ringBuffer.remainingCapacity().toInt()
    }

    override fun getWorkers(): Int {
        return workers
    }

    override fun getPublishedCount(): Long {
        return publishedCount.sum()
    }

    override fun getDiscardedCount(): Long {
        return discardedCount.sum()
    }

    override fun getReceivedCount(): Long {
        return receivedCount.sum()
    }

    override fun getSucceedCount(): Long {
        return succeedCount.sum()
    }

    override fun getFailedCount(): Long {
        return failedCount.sum()
    }

    override fun getEventListenerList(): Array<GoblinEventListenerMXBean> {
        return lock.read { listeners.values.toTypedArray() }
    }

    override fun disposeBean() {
        lock.write {
            listeners.values.forEach {
                it.dispose()
                logger.debug("{EventBus} EventBusWorker [$channel] unsubscribed [$it]")
            }
            listeners.clear()
        }
        val state = try {
            disruptor.shutdown(DEFAULT_SHUTDOWN_TIMEOUT_IN_SECONDS.toLong(), TimeUnit.SECONDS)
            "SUCCESS"
        } catch (ignore: TimeoutException) {
            "TIMEOUT"
        }
        logger.debug("{EventBus} EventBusWorker [$channel] disruptor shutdown [$state] and disposed")
    }
}