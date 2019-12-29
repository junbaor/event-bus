package com.junbaor.event

import com.junbaor.event.boss.EventBusBoss
import com.junbaor.event.exception.EventBossChannelNotFoundException
import com.junbaor.event.exception.EventBossListenerNotFoundException
import com.junbaor.event.exception.EventBusException
import com.junbaor.event.util.RandomUtils
import org.junit.Assert.*
import org.junit.Test


class EventBusTest {

    class EventBusTestEvent : GoblinEvent()

    @Test
    fun channelNotFound() {
        val channel = "/${RandomUtils.nextObjectId()}"
        val future = EventBus.publish(channel, EventBusTestEvent())
        try {
            future.get()
            fail()
        } catch (ex: EventBusException) {
            assertTrue(ex.cause is EventBossChannelNotFoundException)
        }
    }

    @Test
    fun listenerNotFound() {
        val channel = "/${RandomUtils.nextObjectId()}"
        EventBus.register(channel, 32, 1)

        try {
            val future = EventBus.publish(channel, EventBusTestEvent())
            try {
                future.get()
                fail()
            } catch (ex: EventBusException) {
                assertTrue(ex.cause is EventBossListenerNotFoundException)
            }
        } finally {
            EventBus.unregister(channel)
        }
    }

    @Test
    fun execute() {
        EventBusBoss.INSTANCE.initialize()

        val s = RandomUtils.nextObjectId()
        assertEquals(s, echo(s))
        val i = RandomUtils.nextObjectId()
        assertEquals(i, echo(i))
        assertNull(echo(null))
    }

    private fun echo(input: Any?): Any? {
        return EventBus.execute { input }.get()
    }
}