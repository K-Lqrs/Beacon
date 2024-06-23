package net.rk4z.beacon.common

interface ListenerBase {
    fun handleEvents(): Boolean = true
    fun unregister()
}