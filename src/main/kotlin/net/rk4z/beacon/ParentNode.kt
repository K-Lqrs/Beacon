package net.rk4z.beacon

import net.rk4z.beacon.EventBus.logger

open class ParentNode(name: String) : Node(name) {
    val discoveryService = DiscoveryService()
    val nodeConfig = NodeConfig()
    val connectedChildren = mutableListOf<ChildNode>()

    init {
        // ローカルネットワーク上で子ノードを自動検出
        discoveryService.discoverChildNodes { event ->
            val info = event.info
            val childNode = ChildNode(info.name, info.inetAddresses.first().hostAddress, info.port)
            connectChild(childNode)
            nodeConfig.addChildNode(info.name, info.inetAddresses.first().hostAddress, info.port)
        }
    }

    fun connectChild(child: ChildNode) {
        connectedChildren.add(child)
        child.setParent(this)
        logger.info("Connected to child node: ${child.name} at ${child.ip}:${child.port}")
    }

    fun disconnectChild(child: ChildNode) {
        connectedChildren.remove(child)
        nodeConfig.removeChildNode(child.name)
        logger.info("Disconnected from child node: ${child.name}")
    }

    fun addChildNode(name: String, ip: String, port: Int) {
        val childNode = ChildNode(name, ip, port)
        connectChild(childNode)
        nodeConfig.addChildNode(name, ip, port)
    }

    fun removeChildNode(name: String) {
        val childNode = connectedChildren.find { it.name == name }
        if (childNode != null) {
            disconnectChild(childNode)
        } else {
            logger.info("Child node not found: $name")
        }
    }

    fun listConnectedChildren() {
        println("Currently connected child nodes:")
        nodeConfig.getChildNodes().forEach { nodeInfo ->
            logger.info("${nodeInfo.name} at ${nodeInfo.ip}:${nodeInfo.port}")
        }
    }

    fun routeEvent(event: Event, sourceNode: Node) {
        // 送信元のノードを除外してイベントをルーティング
        connectedChildren.filter { it != sourceNode }.forEach { it.receiveEvent(event) }
    }
}