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
import net.minecraft.client.resources.I18n
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL

object VeinMiningHUD {

    private const val EDGE_X = 20
    private const val EDGE_Y = 20

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

            GuiDraw.drawString(I18n.format("bandit.message.activating"), EDGE_X, EDGE_Y, -1)

            activeMenu = if(GuiScreen.isShiftKeyDown()) {
                drawExecutorSelectionMenu()
                executors
            } else if(GuiScreen.isCtrlKeyDown()) {
                drawBlockFilterSelectionMenu()
                blockFilters
            } else {
                null
            }

            VeinMiningHandlerClient.selectedBlockPosList?.let {
                GuiDraw.drawString(
                    I18n.format("bandit.message.precalculated", it.size),
                    EDGE_X,
                    EDGE_Y + 30 + 8 * 3,
                    -1,
                )
            }

            glEnable(GL_RESCALE_NORMAL)
        }
    }

    private fun <K, V : HasUnlocalizedName> drawCircleList(circleList: CircleList<Pair<K, V>>) {
        val prev = circleList.prev()
        val curr = circleList.curr()
        val next = circleList.next()

        GuiDraw.drawString(
            " ${I18n.format(prev.second.getUnlocalizedName())} ",
            EDGE_X,
            EDGE_Y + 25 + 8 * 0,
            0xFFA0A0A0.toInt(),
            true
        )
        GuiDraw.drawString(
            ">${I18n.format(curr.second.getUnlocalizedName())}<",
            EDGE_X,
            EDGE_Y + 25 + 8 * 1,
            0xFFFFFFFF.toInt(),
            true
        )
        GuiDraw.drawString(
            " ${I18n.format(next.second.getUnlocalizedName())} ",
            EDGE_X,
            EDGE_Y + 25 + 8 * 2,
            0xFFA0A0A0.toInt(),
            true
        )
    }

    private fun drawExecutorSelectionMenu() = drawCircleList(executors)
    private fun drawBlockFilterSelectionMenu() = drawCircleList(blockFilters)

    internal fun withActiveMenu(block: CircleList<*>.() -> Unit) {
        activeMenu?.let(block)
    }

}
