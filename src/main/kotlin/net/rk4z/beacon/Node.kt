package net.rk4z.beacon

import net.rk4z.beacon.EventBus.logger

open class Node(val name: String) {
    var parent: ParentNode? = null
        private set

    // 親ノードへの接続を設定
    fun setParent(parentNode: ParentNode) {
        this.parent = parentNode
    }

    // イベントを送信 - 親ノードを介して他のノードに送信される
    fun sendEvent(event: Event) {
        parent?.routeEvent(event, this)
    }

    // イベントを受信 - サブクラスでオーバーライドして処理を定義
    open fun receiveEvent(event: Event) {
        // 子ノード側でのイベント処理を実装するためにオーバーライドされる
        logger.info("Event received at node $name: ${event::class.simpleName}")
    }
}