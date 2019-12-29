package com.junbaor.event.context

import com.junbaor.event.GoblinEventContext
import com.junbaor.event.GoblinEventFuture
import com.junbaor.event.concurrent.GoblinFuture
import com.junbaor.event.concurrent.GoblinFutureImpl
import com.junbaor.event.exception.EventBusException
import com.junbaor.event.function.Block1

class GoblinEventFutureImpl : GoblinFutureImpl<GoblinEventContext>(), GoblinEventFuture {

    private var context: GoblinEventContextImpl? = null

    override fun bypassExecutionException(): Boolean {
        return true
    }

    override fun complete(result: GoblinEventContext?): GoblinFuture<GoblinEventContext> {
        val context = result as GoblinEventContextImpl
        this.context = context
        if (context.isSuccess) {
            return super.complete(result)
        }
        if (!context.event.isRaiseException) {
            return super.complete(result)
        }
        var error: EventBusException? = null
        context.throwExceptionIfNecessary()?.run {
            error = this
        }
        return super.complete(result, error)
    }

    override fun addDiscardListener(action: Block1<GoblinEventContext>) {
        addListener {
            context?.run {
                if (this.isDiscard) {
                    action.apply(this)
                }
            }
        }
    }
}
