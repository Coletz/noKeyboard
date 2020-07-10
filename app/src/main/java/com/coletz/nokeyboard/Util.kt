package com.coletz.nokeyboard

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log

fun CharSequence?.getLastWordLength(): Int {
    if(this.isNullOrEmpty()) return 0
    if(this.length == 1) return 1
    if(this.endsWith(" ")){
        val seqWithoutSpaces = this.dropLastWhile { it == ' ' }
        return this.length - seqWithoutSpaces.length
    }

    return this.split(' ').last().length
}

fun Context.drawable(resId: Int): Drawable? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        getDrawable(resId)
    } else {
        @Suppress("deprecation")
        resources.getDrawable(resId)
    }
}

val Any?.log: Int
    get()= Log.e("[LOG]", this?.toString() ?: "[null]")