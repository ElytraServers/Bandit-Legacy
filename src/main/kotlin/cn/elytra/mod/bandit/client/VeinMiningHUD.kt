package cn.elytra.mod.bandit.client

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

    internal fun withActiveMenu(block: CircleList<*>.() -> Unit) {
        activeMenu?.let(block)
    }

}
