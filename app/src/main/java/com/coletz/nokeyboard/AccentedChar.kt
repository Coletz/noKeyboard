package com.coletz.nokeyboard

enum class AccentedChar(val primaryCode: Int, val lowercase: String, val uppercase: String) {
    A(97, "à", "À"),
    E(101, "è", "È"),
    I(105, "ì", "Ì"),
    O(111, "ò", "Ò"),
    U(117, "ù", "Ù");

    companion object {
        fun charForCode(primaryCode: Int, uppercased: Boolean) = values()
                .firstOrNull { it.primaryCode == primaryCode }
                ?.let { if(uppercased) it.uppercase else it.lowercase }
    }
}