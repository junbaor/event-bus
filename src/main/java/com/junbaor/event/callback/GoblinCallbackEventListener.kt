package com.junbaor.event.callback;

import com.junbaor.event.GoblinEventChannel
import com.junbaor.event.GoblinEventContext
import com.junbaor.event.GoblinEventListener
import com.junbaor.event.annotation.Install
import com.junbaor.event.annotation.Singleton

@Singleton
class GoblinCallbackEventListener private constructor() : GoblinEventListener {

    companion object {
        val INSTANCE = GoblinCallbackEventListener()
    }

    override fun accept(context: GoblinEventContext): Boolean {
        return context.event is GoblinCallbackEvent
    }

    override fun onEvent(context: GoblinEventContext) {
        val event = context.event as GoblinCallbackEvent
        val callback = event.callback
        try {
            callback.call()?.run {
                context.setExtension("GoblinCallback.Result", this)
            }

        } catch (ex: Throwable) {
            GoblinCallbackException.throwException(ex)
        }
    }

    @Install
    @GoblinEventChannel("/goblin/core")
    class Installer : GoblinEventListener by INSTANCE
}
