package cn.elytra.mod.bandit.client

import cpw.mods.fml.client.config.IConfigElement
import cpw.mods.fml.common.Loader
import net.minecraftforge.common.config.ConfigElement
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.common.config.Property
import java.io.File

object VeinMiningConfigClient {

    val config = Configuration(File(Loader.instance().configDir, "bandit-client.cfg"))

    var menuMarginX = 40
    var menuMarginY = 40

    var menuPosition = MenuPosition.TOP_LEFT

    private val propMarginX = config.get("client", "menu-margin-x", 40)
    private val propMarginY = config.get("client", "menu-margin-y", 40)

    private val propMenuPosition = config.getAsEnum("client", "menu-position", MenuPosition.TOP_LEFT)

    fun reload() {
        menuMarginX = propMarginX.getInt(menuMarginX)
        menuMarginY = propMarginY.getInt(menuMarginY)
        menuPosition = try {
            enumValueOf<MenuPosition>(propMenuPosition.string)
        } catch(_: Exception) {
            propMenuPosition.set(MenuPosition.TOP_LEFT.name)
            MenuPosition.TOP_LEFT
        }
    }

    fun save() {
        config.save()
    }

    internal inline fun <reified T : Enum<T>> Configuration.getAsEnum(category: String, key: String, defaultValue: T): Property {
        return get(category, key, defaultValue.name).apply {
            setValidValues(enumValues<T>().map { it.name }.toTypedArray())
            comment = "[valid values: ${validValues.joinToString()}]"
        }
    }

    enum class MenuPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
    }

    fun listElements(): List<IConfigElement<*>> = ConfigElement<Any>(config.getCategory("client")).childElements

}
