package cn.elytra.mod.bandit.mining2.selector

object BuiltinSelectors {
    object Default : DefaultSelector(limitCount = 64, limitRange = 256.0, allowCornerTouched = true)

    object LargeScan : LargeScanSelector(limitCount = Int.MAX_VALUE, radiusXZ = 320, radiusY = 256)
}
