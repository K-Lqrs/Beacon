package net.rk4z.beacon

import net.rk4z.beacon.EventBus.logger
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener

class DiscoveryService {
    private val jmdns: JmDNS = JmDNS.create()

    // 子ノードが自分をローカルネットワーク上に登録
    fun registerChildNode(name: String, port: Int) {
        val serviceInfo = ServiceInfo.create("_beacon._tcp.local.", name, port, "Beacon Child Node")
        jmdns.registerService(serviceInfo)
        logger.info("Child node registered: $name on port $port")
    }

    // 親ノードが子ノードを自動検出
    fun discoverChildNodes(listener: (ServiceEvent) -> Unit) {
        jmdns.addServiceListener("_beacon._tcp.local.", object : ServiceListener {
            override fun serviceAdded(event: ServiceEvent) {
                logger.info("Service added: ${event.name}")
                listener(event)
            }

            override fun serviceRemoved(event: ServiceEvent) {
                logger.info("Service removed: ${event.name}")
            }

            override fun serviceResolved(event: ServiceEvent) {
                logger.info("Service resolved: ${event.info}")
            }
        })
    }
}
