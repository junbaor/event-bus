package com.junbaor.event;

import java.util.*

interface GoblinEventListener : EventListener {

    fun accept(context: GoblinEventContext): Boolean

    fun onEvent(context: GoblinEventContext)

}
