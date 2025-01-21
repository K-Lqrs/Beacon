package net.rk4z.beacon.test

import net.rk4z.beacon.*

class MainKotlin : IEventHandler {
    override fun initHandlers() {
        handler<MyEventKotlin> {
            println("Event received: " + it.message)
        }

        returnableHandler<MyReturnableEventKotlin, String> {
            return@returnableHandler "Returnable event received: " + it.message
        }
    }

    // ...or
    //    val onEvent = handler<MyEventKotlin> {
    //        println("Event received: " + it.message)
    //    }
    //
    //    val onReturnableEvent = returnableHandler<MyReturnableEventKotlin, String> {
    //        return@returnableHandler "Returnable event received: " + it.message
    //    }
}

class MyEventKotlin(
    val message: String
) : Event()

class MyReturnableEventKotlin(
    val message: String
) : ReturnableEvent<String>()

fun main() {
    EventBus.initialize("net.rk4z.beacon.test")
    EventBus.postAsync(MyEventKotlin("Hello, world!"))
    val result = EventBus.postReturnable(MyReturnableEventKotlin("Hello, world!"), EventProcessingType.ASYNC)
    println(result)
}