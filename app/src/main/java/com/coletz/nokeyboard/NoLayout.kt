package com.coletz.nokeyboard

import java.lang.Exception

enum class NoLayout(val xml: Int, val shiftKey: ShiftKey) {
    ERTY(R.xml.four_qwerty, ShiftKey(28, "abc")),
    SYM1(R.xml.four_symbol1, ShiftKey(32, "123")),
    SYM2(R.xml.four_symbol2, ShiftKey(32, ",.?"));

    // Return the next NoLayout and the next shifted value
    // Basically shift is enabled in the "ERTY" mode, while
    // it is disabled in the "erty" mode
    fun next(shifted: Boolean) = when {
        this == ERTY && shifted -> ERTY to false
        this == ERTY && !shifted -> SYM1 to false
        this == SYM1 -> SYM2 to false
        this == SYM2 -> ERTY to true
        else -> throw Exception("Undefined state: [$this] + [shifted = $shifted]")
    }
}