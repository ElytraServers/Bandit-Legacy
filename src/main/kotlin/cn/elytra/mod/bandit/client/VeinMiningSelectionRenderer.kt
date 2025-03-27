package cn.elytra.mod.bandit.client

import codechicken.lib.colour.ColourRGBA
import codechicken.lib.render.RenderUtils
import codechicken.lib.vec.Cuboid6
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.RenderWorldLastEvent
import org.lwjgl.opengl.GL11.*

object VeinMiningSelectionRenderer {

    fun render(event: RenderWorldLastEvent, posList: List<BlockPos>) {
        val frame = event.partialTicks
        val mc = Minecraft.getMinecraft()
        val entity = mc.renderViewEntity

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        ColourRGBA(1.0, 1.0, 1.0, 1.0).glColour()
        glLineWidth(2.0F)
        glDepthMask(false)

        glPushMatrix()
        RenderUtils.translateToWorldCoords(entity, frame)
        for(pos in posList) {
            val c = Cuboid6(
                pos.x.toDouble(),
                pos.y.toDouble(),
                pos.z.toDouble(),
                pos.x.toDouble() + 1,
                pos.y.toDouble() + 1,
                pos.z.toDouble() + 1,
            )
            RenderUtils.drawCuboidOutline(c)
        }
        glPopMatrix()
        glDepthMask(true)
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
    }
}
