package net.rk4z.beacon

import net.rk4z.beacon.EventBus.logger
import java.util.concurrent.ConcurrentHashMap

class NodeConfig {
    // 子ノードの設定を保持するConcurrentHashMap
    val childNodes = ConcurrentHashMap<String, NodeInfo>()

    // 子ノードを追加
    fun addChildNode(name: String, ip: String, port: Int) {
        childNodes[name] = NodeInfo(name, ip, port)
        logger.info("Added child node: $name at $ip:$port")
    }

    // 子ノードを削除
    fun removeChildNode(name: String) {
        childNodes.remove(name)
        logger.info("Removed child node: $name")
    }

    // 子ノードの一覧を取得
    fun getChildNodes(): Collection<NodeInfo> = childNodes.values
}

data class NodeInfo(val name: String, val ip: String, val port: Int)
