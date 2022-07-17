package mc

import androidx.compose.ui.graphics.Color

enum class McColor(val char: Char, val hex: String, val formalName: String) {
    BLACK('0', "000000", "black"),
    DARK_BLUE('1', "0000AA", "dark_blue"),
    DARK_GREEN('2', "00AA00", "dark_green"),
    DARK_AQUA('3', "00AAAA", "dark_aqua"),
    DARK_RED('4', "AA0000", "dark_red"),
    DARK_PURPLE('5', "AA00AA", "dark_purple"),
    GOLD('6', "FFAA00", "gold"),
    GRAY('7', "AAAAAA", "gray"),
    DARK_GRAY('8', "555555", "dark_gray"),
    BLUE('9', "5555FF", "blue"),
    GREEN('a', "55FF55", "green"),
    AQUA('b', "55FFFF", "aqua"),
    RED('c', "FF5555", "red"),
    PURPLE('d', "FF55FF", "purple"),
    YELLOW('e', "FFFF55", "yellow"),
    WHITE('f', "FFFFFF", "white");

    companion object {
        fun valueOf(char: Char) = McColor.values().find { it.char == char }
    }

    val motd: String
        get() = "\\u00A7$char"

    val color: Color
        get() = Color(hex.toInt(16))
}