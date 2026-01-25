package cn.elytra.mod.bandit.client

import cn.elytra.mod.bandit.common.player_data.VeinMiningNoticeType
import cn.elytra.mod.bandit.common.util.HasUnlocalizedName
import cn.elytra.mod.bandit.mining.BlockFilterRegistry
import cn.elytra.mod.bandit.mining.ExecutorGeneratorRegistry
import cn.elytra.mod.bandit.mining.executor.VeinMiningExecutorGenerator
import cn.elytra.mod.bandit.mining.filter.VeinMiningBlockFilter
import cn.elytra.mod.bandit.network.BanditNetwork
import cn.elytra.mod.bandit.util.CircleList
import codechicken.lib.gui.GuiDraw
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.resources.I18n
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL

object VeinMiningHUD {

    private val pos get() = VeinMiningConfigClient.menuPosition
    private val marginX get() = VeinMiningConfigClient.menuMarginX
    private val marginY get() = VeinMiningConfigClient.menuMarginY

    private val executors: CircleList<Pair<Int, VeinMiningExecutorGenerator>> = ExecutorGeneratorCircleList
    private val blockFilters: CircleList<Pair<Int, VeinMiningBlockFilter>> = BlockFilterCircleList

    /**
     * Represents a HUD (Heads-Up Display) notification message.
     *
     * HUD notices are time-based display elements that can fade in/out and contain
     * various types of game notifications.
     *
     * @property id Unique identifier for tracking and stopping specific notices.
     * @property noticeType Type of notice. Use predefined constants for specific notice types by [VeinMiningNoticeType].
     * @property startTick The fade-out animation start tick.
     * @property fadeDelay Number of ticks to wait before starting the fade-out animation.
     * @property fadeTicks Duration (in ticks) of the fade-out animation.
     * @property extraData Additional structured data for notice-specific information.
     * @property isEnded Flag indicating whether this notice is about to end.
     */
    data class HudNotice(
        val id: Long,
        var noticeType: Int = 0,
        var startTick: Long,
        var fadeDelay: Int = 0,
        var fadeTicks: Int = 20,
        var extraData: Map<String, Int> = emptyMap(),
        var isEnded: Boolean = false
    )
    private val notices = mutableListOf<HudNotice>()

    /**
     * Reference to the active menu, used to either show the menu and handle the mouse scroll inputs.
     */
    private var activeMenu: CircleList<*>? = null

    private object ExecutorGeneratorCircleList :
        CircleList<Pair<Int, VeinMiningExecutorGenerator>>(ExecutorGeneratorRegistry.all().map { it.key to it.value }) {
        override var index: Int
            get() = VeinMiningHandlerClient.veinMiningExecutorId
            set(value) {
                VeinMiningHandlerClient.veinMiningExecutorId = value
                BanditNetwork.syncSettingsToServer(executorId = value)
            }
    }

    private object BlockFilterCircleList :
        CircleList<Pair<Int, VeinMiningBlockFilter>>(BlockFilterRegistry.all().map { it.key to it.value }) {
        override var index: Int
            get() = VeinMiningHandlerClient.veinMiningBlockFilterId
            set(value) {
                VeinMiningHandlerClient.veinMiningBlockFilterId = value
                BanditNetwork.syncSettingsToServer(blockFilterId = value)
            }
    }

    internal fun render() {
        val mc = Minecraft.getMinecraft()
        if(mc.currentScreen == null && mc.theWorld != null && !mc.gameSettings.keyBindPlayerList.isKeyPressed) {
            glDisable(GL_RESCALE_NORMAL)
            glDisable(GL_LIGHTING)
            glDisable(GL_DEPTH_TEST)

            var y = marginY
            drawStringWithMargin(I18n.format("bandit.message.activating"), marginX, y)
            y += 9 // font height
            activeMenu = if(GuiScreen.isShiftKeyDown()) {
                drawExecutorSelectionMenu(y)
                executors
            } else if(GuiScreen.isCtrlKeyDown()) {
                drawBlockFilterSelectionMenu(y)
                blockFilters
            } else {
                null
            }
            y += 27 // list height
            VeinMiningHandlerClient.selectedBlockPosList?.let {
                drawStringWithMargin(I18n.format("bandit.message.precalculated", it.size), marginX, y)
            }
            // y += 9 // font height

            glEnable(GL_RESCALE_NORMAL)
        }
    }

    fun pushNotice(
        noticeType: Int,
        id: Long,
        extraData: Map<String, Int> = emptyMap(),
        fadeDelay: Int = 0,
        fadeTicks: Int = 0
    ): Long {
        val isEnd = !(fadeDelay == 0 && fadeTicks == 0)
        var now: Long = 0
        if (isEnd) {
            now = Minecraft.getMinecraft().theWorld.totalWorldTime
        }
        notices += HudNotice(
            id = id,
            noticeType = noticeType,
            extraData = extraData,
            startTick = now,
            fadeDelay = fadeDelay,
            fadeTicks = fadeTicks,
            isEnded = isEnd
        )
        VeinMiningHandlerClient.noticeActive = true
        return id
    }

    fun endNotice(id: Long, fadeDelay: Int = 0, fadeTicks: Int = 20) {
        if (fadeTicks == 0 && fadeDelay == 0) {
            notices.removeIf { it.id == id }
            return
        }
        val now = Minecraft.getMinecraft().theWorld.totalWorldTime
        notices.find { it.id == id && !it.isEnded }?.apply {
            isEnded = true
            startTick = now
            this.fadeDelay = fadeDelay
            this.fadeTicks = fadeTicks
        }
    }

    private fun getActiveNotices(): List<Pair<HudNotice, Float>> {
        val now = Minecraft.getMinecraft().theWorld.totalWorldTime
        val iter = notices.iterator()
        val result = mutableListOf<Pair<HudNotice, Float>>()

        while (iter.hasNext()) {
            val n = iter.next()
            val age = (now - n.startTick).toInt()

            val alpha = when {
                !n.isEnded -> 1f
                age <= n.fadeDelay -> 1f
                age < n.fadeDelay + n.fadeTicks -> 1f - (age - n.fadeDelay) / n.fadeTicks.toFloat()
                else -> {
                    iter.remove()
                    continue
                }
            }

            result += n to alpha
        }

        if (result.isEmpty()) VeinMiningHandlerClient.noticeActive = false

        return result
    }

    internal fun renderNotice() {
        val mc = Minecraft.getMinecraft()
        if(mc.currentScreen == null && mc.theWorld != null) {
            glDisable(GL_RESCALE_NORMAL)
            glDisable(GL_LIGHTING)

            glDisable(GL_DEPTH_TEST)

            var y = marginY + 45 // list height + font height
            for ((notice, alpha) in getActiveNotices()) {
                val color = (alpha * 255).toInt() shl 24 or 0xFFFFFF
                val text = when(VeinMiningNoticeType.fromId(notice.noticeType)) {
                    VeinMiningNoticeType.TASK_STARTING -> I18n.format("bandit.message.task-starting")
                    VeinMiningNoticeType.TASK_HALT_HINT -> I18n.format("bandit.message.task-halt-hint")
                    VeinMiningNoticeType.TASK_STOP_KEY_RELEASE -> I18n.format("bandit.message.task-stop.key-release")
                    VeinMiningNoticeType.TASK_STOP_FOR_COMMAND -> I18n.format("bandit.message.task-stop.for-command")
                    VeinMiningNoticeType.TASK_DONE -> {
                        val statBlocksMined = notice.extraData["statBlocksMined"] ?: 0
                        val statItemDropped = notice.extraData["statItemDropped"] ?: 0
                        I18n.format(
                            "bandit.message.task-done",
                            statBlocksMined,
                            statItemDropped
                        )
                    }
                    null -> ""
                }
                drawStringWithMargin(text, marginX, y, color)
                y += 9 // font height
            }

            glEnable(GL_RESCALE_NORMAL)
        }
    }

    private fun getPosXWithMargin(margin: Int, str: String): Int {
        val mc = Minecraft.getMinecraft()
        val textRender = mc.fontRenderer
        val scaler = ScaledResolution(mc, mc.displayWidth, mc.displayHeight)

        return when(pos) {
            VeinMiningConfigClient.MenuPosition.TOP_LEFT,
            VeinMiningConfigClient.MenuPosition.BOTTOM_LEFT,
                -> margin

            VeinMiningConfigClient.MenuPosition.TOP_RIGHT,
            VeinMiningConfigClient.MenuPosition.BOTTOM_RIGHT,
                -> scaler.scaledWidth - margin - textRender.getStringWidth(str)
        }
    }

    private fun getPosYWithMargin(margin: Int, str: String): Int {
        val mc = Minecraft.getMinecraft()
        val textRender = mc.fontRenderer
        val scaler = ScaledResolution(mc, mc.displayWidth, mc.displayHeight)

        return when(pos) {
            VeinMiningConfigClient.MenuPosition.TOP_LEFT,
            VeinMiningConfigClient.MenuPosition.TOP_RIGHT,
                -> margin

            VeinMiningConfigClient.MenuPosition.BOTTOM_LEFT,
            VeinMiningConfigClient.MenuPosition.BOTTOM_RIGHT,
                -> scaler.scaledHeight - margin - textRender.FONT_HEIGHT
        }
    }

    private fun drawStringWithMargin(str: String, marginX: Int, marginY: Int, color: Int = -1, shadow: Boolean = true) {
        val x = getPosXWithMargin(marginX, str)
        val y = getPosYWithMargin(marginY, str)
        GuiDraw.drawString(str, x, y, color, shadow)
    }

    private fun <K, V : HasUnlocalizedName> drawCircleList(circleList: CircleList<Pair<K, V>>, y: Int) {
        val prev = circleList.prev()
        val curr = circleList.curr()
        val next = circleList.next()

        drawStringWithMargin(
            " ${I18n.format(prev.second.getUnlocalizedName())} ",
            marginX,
            y,
            0xFFA0A0A0.toInt(),
        )
        drawStringWithMargin(
            ">${I18n.format(curr.second.getUnlocalizedName())}<",
            marginX,
            y + 9,
            0xFFFFFFFF.toInt(),
        )
        drawStringWithMargin(
            " ${I18n.format(next.second.getUnlocalizedName())} ",
            marginX,
            y + 9 + 9,
            0xFFA0A0A0.toInt(),
        )
    }

    private fun drawExecutorSelectionMenu(heightBase: Int) = drawCircleList(executors, heightBase)
    private fun drawBlockFilterSelectionMenu(heightBase: Int) = drawCircleList(blockFilters, heightBase)

    internal inline fun withActiveMenu(block: CircleList<*>.() -> Unit) {
        activeMenu?.let(block)
    }

}
