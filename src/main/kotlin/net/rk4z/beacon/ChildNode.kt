package net.rk4z.beacon

import net.rk4z.beacon.EventBus.logger

open class ChildNode(name: String, val ip: String, val port: Int) : Node(name) {
    val discoveryService = DiscoveryService()

    init {
        // 自身をローカルネットワーク上に登録
        discoveryService.registerChildNode(name, port)
    }

    override fun receiveEvent(event: Event) {
        // 子ノード固有の処理を追加できる
        logger.info("Child node $name received event: ${event::class.simpleName}")
    }
}