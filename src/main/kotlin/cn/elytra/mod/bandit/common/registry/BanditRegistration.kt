package cn.elytra.mod.bandit.common.registry

import cn.elytra.mod.bandit.mining2.matcher.BuiltinMatchers
import cn.elytra.mod.bandit.mining2.matcher.Matcher
import cn.elytra.mod.bandit.mining2.selector.BuiltinSelectors
import cn.elytra.mod.bandit.mining2.selector.Selector

internal object BanditRegistration {
    val matchers: MutableList<Named<Matcher>> = mutableListOf()
    val selectors: MutableList<Named<Selector>> = mutableListOf()

    fun getMatcher(index: Int): Named<Matcher> = matchers[index]

    fun getSelector(index: Int): Named<Selector> = selectors[index]

    fun getMatcherOrDefault(index: Int): Named<Matcher> = matchers.getOrNull(index) ?: matchers.first()

    fun getSelectorOrDefault(index: Int): Named<Selector> = selectors.getOrNull(index) ?: selectors.first()

    init {
        matchers +=
            Named(
                "All",
                "bandit.block-filter.all",
                BuiltinMatchers.All,
            )

        matchers +=
            Named(
                "Block",
                "bandit.block-filter.match-block",
                BuiltinMatchers.SameBlock,
            )

        matchers +=
            Named(
                "Block and Metadata",
                "bandit.block-filter.match-block-and-meta",
                BuiltinMatchers.SameBlockAndMetadata,
            )

        selectors +=
            Named(
                "Default",
                "bandit.executor.manhattan",
                BuiltinSelectors.Default,
            )

        selectors +=
            Named(
                "Large Scan",
                "bandit.executor.large-scan",
                BuiltinSelectors.LargeScan,
            )
    }
}
