package cn.elytra.mod.bandit

object MixinBridger {

    /**
     * Callbacks in this list will be fired when mouse scrolls.
     * Return `true` to cancel the scroll operation (the selected item in hotbar won't change), but
     * will not prevent continuing the callback.
     */
    val onMouseScrollCancelable: MutableList<(scrollValue: Int) -> Boolean> = mutableListOf()

    // internal usage only
    @JvmStatic
    fun fireMouseScrollCancelable(value: Int): Boolean {
        var canceled = false
        for(callback in onMouseScrollCancelable) {
            canceled = canceled or callback(value)
        }
        return canceled
    }

}
