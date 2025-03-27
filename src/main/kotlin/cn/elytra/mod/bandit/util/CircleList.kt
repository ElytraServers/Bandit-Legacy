package cn.elytra.mod.bandit.util

/**
 * A util list wrapper with [index] inside.
 *
 * It behaves like a circle list, that you can get last element at prev of index 0,
 * and first element at next of index last.
 *
 * @see cn.elytra.mod.bandit.client.VeinMiningHUD
 */
open class CircleList<E>(private val delegate: List<E>) : List<E> by delegate {

    open var index: Int = 0

    fun prev(): E {
        return if(index > 0) delegate[index - 1] else this.last()
    }

    fun curr(): E {
        return delegate[index]
    }

    fun next(): E {
        return if(index < delegate.size - 1) delegate[index + 1] else this.first()
    }

    fun move(offset: Int) {
        index += offset
        if(index < 0) {
            index = this.size - 1
        } else if(index >= this.size) {
            index = 0
        }
    }

}
