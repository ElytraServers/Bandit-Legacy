package cn.elytra.mod.bandit.client

import cn.elytra.mod.bandit.common.registry.Named
import cn.elytra.mod.bandit.util.remEuclid
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

    internal fun render() {
        val mc = Minecraft.getMinecraft()
        if (mc.currentScreen == null && mc.theWorld != null && !mc.gameSettings.keyBindPlayerList.isKeyPressed) {
            glDisable(GL_RESCALE_NORMAL)
            glDisable(GL_LIGHTING)
            glDisable(GL_DEPTH_TEST)

            var y = marginY
            drawStringWithMargin(I18n.format("bandit.message.activating"), marginX, y)
            y += 9 // font height
            if (GuiScreen.isShiftKeyDown()) {
                drawSelectorSelectionMenu(y)
            } else if (GuiScreen.isCtrlKeyDown()) {
                drawMatcherSelectionMenu(y)
            }
            y += 27 // list height
            VeinMiningHandlerClient.selectedBlockPosList?.let {
                drawStringWithMargin(I18n.format("bandit.message.precalculated", it.size), marginX, y)
            }
            // y += 9 // font height

            glEnable(GL_RESCALE_NORMAL)
        }
    }

    private fun getPosXWithMargin(
        margin: Int,
        str: String,
    ): Int {
        val mc = Minecraft.getMinecraft()
        val textRender = mc.fontRenderer
        val scaler = ScaledResolution(mc, mc.displayWidth, mc.displayHeight)

        return when (pos) {
            VeinMiningConfigClient.MenuPosition.TOP_LEFT,
            VeinMiningConfigClient.MenuPosition.BOTTOM_LEFT,
            -> margin

            VeinMiningConfigClient.MenuPosition.TOP_RIGHT,
            VeinMiningConfigClient.MenuPosition.BOTTOM_RIGHT,
            -> scaler.scaledWidth - margin - textRender.getStringWidth(str)
        }
    }

    private fun getPosYWithMargin(
        margin: Int,
        str: String,
    ): Int {
        val mc = Minecraft.getMinecraft()
        val textRender = mc.fontRenderer
        val scaler = ScaledResolution(mc, mc.displayWidth, mc.displayHeight)

        return when (pos) {
            VeinMiningConfigClient.MenuPosition.TOP_LEFT,
            VeinMiningConfigClient.MenuPosition.TOP_RIGHT,
            -> margin

            VeinMiningConfigClient.MenuPosition.BOTTOM_LEFT,
            VeinMiningConfigClient.MenuPosition.BOTTOM_RIGHT,
            -> scaler.scaledHeight - margin - textRender.FONT_HEIGHT
        }
    }

    private fun drawStringWithMargin(
        str: String,
        marginX: Int,
        marginY: Int,
        color: Int = -1,
        shadow: Boolean = true,
    ) {
        val x = getPosXWithMargin(marginX, str)
        val y = getPosYWithMargin(marginY, str)
        GuiDraw.drawString(str, x, y, color, shadow)
    }

    private fun drawCircleList(
        list: List<Named<*>>,
        currIndex: Int,
        y: Int,
    ) {
        val prev = list.getLoop(currIndex - 1)
        val curr = list.getLoop(currIndex + 0)
        val next = list.getLoop(currIndex + 1)

        drawStringWithMargin(
            " ${prev.getString()} ",
            marginX,
            y,
            0xFFA0A0A0.toInt(),
        )
        drawStringWithMargin(
            ">${curr.getString()}<",
            marginX,
            y + 9,
            0xFFFFFFFF.toInt(),
        )
        drawStringWithMargin(
            " ${next.getString()} ",
            marginX,
            y + 9 + 9,
            0xFFA0A0A0.toInt(),
        )
    }

    private fun drawMatcherSelectionMenu(heightBase: Int) =
        drawCircleList(
            VeinMiningHandlerClient.matcherNames,
            VeinMiningHandlerClient.matcherIndex,
            heightBase,
        )

    private fun drawSelectorSelectionMenu(heightBase: Int) =
        drawCircleList(
            VeinMiningHandlerClient.selectorNames,
            VeinMiningHandlerClient.selectorIndex,
            heightBase,
        )

    private fun <E> List<E>.getLoop(index: Int): E = get(index remEuclid size)

    private fun Named<*>.getString(): String {
        val translated = I18n.format(translationKey)
        // if the translated is equal to translationKey, it means the key is missing, we fallback to the default name
        return if (translated == translationKey) name else translated
    }
}
