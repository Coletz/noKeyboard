package com.coletz.nokeyboard

import android.content.Context
import android.graphics.drawable.Drawable
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Build
import android.util.Log
import android.view.inputmethod.EditorInfo.*
import java.lang.Exception

class NoKeyboard(private val context: Context) {

    private lateinit var keyboardView: KeyboardView

    private val allKeyboards = NoLayout.values().associateBy({ it },{ Keyboard(context, it.xml) })

    private val currentKeyboard: Keyboard
        get() = allKeyboards[currentLayout] ?: throw Exception("Layout [${currentLayout.name}] is not initialized")

    var currentLayout: NoLayout = NoLayout.ERTY
        set(value) {
            field = value
            keyboardView.keyboard = currentKeyboard
            keyboardView.invalidateAllKeys()
        }

    var imeAction: Int = 0

    var shifted: Boolean = false
        set(value) {
            field = value
            allKeyboards[NoLayout.ERTY]?.isShifted = value
            adjustShiftKeyLabel()
            keyboardView.invalidateAllKeys()
        }

    // If enabled pressing an uppercase letter won't disable shift
    var capsLock: Boolean = false
    // If enabled pressing a num/symbol won't return to letter keys
    // numLock = symbol1, numLock2 = symbol2
    var numLock: Boolean = false

    fun attachTo(listener: KeyboardView.OnKeyboardActionListener, keyboardView: KeyboardView){
        this.keyboardView = keyboardView
        keyboardView.setOnKeyboardActionListener(listener)
        init()
    }

    private fun init(){
        capsLock = false
        numLock = false
        currentLayout = NoLayout.ERTY
        keyboardView.isPreviewEnabled = true
        shifted = true
    }

    fun nextKeyboard(longPressed: Boolean = false){
        if(longPressed && currentLayout == NoLayout.ERTY) {
            // Long pressed while seeing ERTY key => caps lock enabled
            capsLock = true
            allKeyboards[NoLayout.ERTY]?.isShifted = true
            keyboardView.invalidateAllKeys()
        } else if(longPressed && (currentLayout == NoLayout.SYM1 || currentLayout == NoLayout.SYM2)) {
            numLock = true
        } else if(capsLock) {
            // Single tap while caps lock enabled = disable caps lock
            capsLock = false
            allKeyboards[NoLayout.ERTY]?.isShifted = false
            keyboardView.invalidateAllKeys()
        } else {
            // single tap means numLocks automatically disabled
            numLock = false
            // Update to new kb
            val nextVal = currentLayout.next(shifted || capsLock)
            currentLayout = nextVal.first
            shifted = nextVal.second
        }

        // Prepare the shift key for next kb
        adjustShiftKeyLabel()
    }

    private fun adjustShiftKeyLabel(){
        val label = currentLayout.next(shifted || capsLock).let { (nextLayout, isShifted) ->
            val nextShiftKeyLabel = nextLayout.shiftKey.label
            if(isShifted) nextShiftKeyLabel.toUpperCase() else nextShiftKeyLabel.toLowerCase()
        }
        currentKeyboard.keys[currentLayout.shiftKey.position].label = label
    }

    fun setImeActionKey(ims: InputMethodService){
        imeAction = ims.currentInputEditorInfo.imeOptions and IME_MASK_ACTION

        val label = when(imeAction) {
            IME_ACTION_DONE,
            IME_ACTION_GO,
            IME_ACTION_SEND -> { "\u2713" /*✓*/ }
            IME_ACTION_SEARCH -> { "\uD83D\uDD0D"/*magnifier*/ }
            IME_ACTION_NEXT -> { "Next" }
            IME_ACTION_PREVIOUS -> { "Prev" }
            else -> { "\u23CE" /*⏎*/ }
        }

        // TODO set icon instead of label

        val imeKeyPosition = currentLayout.shiftKey.position + 2
        keyboardView.keyboard.keys[imeKeyPosition].label = label
    }
}