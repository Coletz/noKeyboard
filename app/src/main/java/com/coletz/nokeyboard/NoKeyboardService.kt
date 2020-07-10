package com.coletz.nokeyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.view.View
import android.view.inputmethod.InputConnection

class NoKeyboardService : InputMethodService(), OnKeyboardActionListener {

    private var kv: KeyboardView? = null
    private lateinit var noKeyboard: NoKeyboard
    private var timerStart: Long = 0

    override fun onCreateInputView(): View {
        return kv ?: run {
            val view = layoutInflater.inflate(R.layout.keyboard, null) as KeyboardView
            kv = view
            view.isPreviewEnabled = false
            noKeyboard = NoKeyboard(this)
            noKeyboard.attachTo(this, view)
            noKeyboard.setImeActionKey(this)
            view
        }
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray) {}

    override fun onPress(primaryCode: Int) {
        // Disable preview for mod keys and space
        kv?.isPreviewEnabled = !(primaryCode <= 0 || primaryCode == 32)
        timerStart = System.currentTimeMillis()
    }

    override fun onRelease(primaryCode: Int) {
        kv?.isPreviewEnabled = false
        val ic = currentInputConnection
        val character: String?
        val upperCase = noKeyboard.capsLock || noKeyboard.shifted

        when (primaryCode) {
            0 -> return
            Keyboard.KEYCODE_MODE_CHANGE -> {
                noKeyboard.nextKeyboard(System.currentTimeMillis() - timerStart > LONGPRESS_DELAY)
                return
            }
            Keyboard.KEYCODE_DONE -> {
                ic.performEditorAction(noKeyboard.imeAction)
                return
            }
            Keyboard.KEYCODE_DELETE -> {
                val selectedText = ic.getSelectedText(0)
                if (selectedText != null && selectedText.isNotEmpty()) {
                    ic.commitText("", 1)
                    shiftAfterDelete(ic)
                } else {
                    val deletableChar = ic.getTextBeforeCursor(1, 0)
                    if (deletableChar.isEmpty()) {
                        return
                    }
                    ic.deleteSurroundingText(1, 0)
                    // If lastly deleted character was uppercase
                    if (Character.isUpperCase(deletableChar[0])) {
                        noKeyboard.shifted = true
                        return
                    }
                    shiftAfterDelete(ic)
                }
                return
            }
            97, 101, 105, 111, 117 -> {
                if (System.currentTimeMillis() - timerStart < LONGPRESS_DELAY) {
                    var code = primaryCode.toChar()
                    if (Character.isLetter(code) && upperCase) {
                        code = Character.toUpperCase(code)
                    }
                    character = code.toString()
                } else {
                    character = AccentedChar.charForCode(primaryCode, upperCase)
                }
                ic.commitText(character, 1)
            }
            else -> {
                var code = primaryCode.toChar()
                if (Character.isLetter(code) && upperCase) {
                    code = Character.toUpperCase(code)
                }
                ic.commitText(code.toString(), 1)
            }
        }
        if (noKeyboard.currentLayout !== NoLayout.ERTY) {
            if(!noKeyboard.numLock){
                noKeyboard.currentLayout = NoLayout.ERTY
            }
        } else if (noKeyboard.shifted) {
            noKeyboard.shifted = false
        }
    }

    override fun onText(text: CharSequence) {}

    override fun swipeDown() {
        requestHideSelf(0)

    }

    override fun swipeLeft() {
        val ic = currentInputConnection
        val selectedText = ic.getSelectedText(0)
        if (selectedText != null && selectedText.isNotEmpty()) {
            ic.commitText("", 1)
            shiftAfterDelete(ic)
        } else {
            val text = ic.getTextBeforeCursor(100, 0)
            val lastWordLength = text.getLastWordLength()
            if (lastWordLength > 0) {
                ic.deleteSurroundingText(lastWordLength, 0)
                shiftAfterDelete(ic)
            }
        }
    }

    override fun swipeRight() {}

    override fun swipeUp() {}

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        if (kv != null) {
            noKeyboard.setImeActionKey(this)
        }
        return true
    }

    private fun shiftAfterDelete(ic: InputConnection) {
        // If there are no char before (count == 0)
        // OR
        // there is 1 char before and it is a `.` (dot)
        // OR
        // there are 2 char before and they are `. ` (dot space)
        // THEN
        // enable shift
        val prevChars = ic.getTextBeforeCursor(2, 0)
        if (prevChars.isEmpty() || prevChars.length == 1 && prevChars == "." || prevChars.length == 2 && prevChars == ". ") {
            noKeyboard.shifted = true
        } else if (noKeyboard.shifted) {
            noKeyboard.shifted = false
        }
    }

    companion object {
        private const val LONGPRESS_DELAY = 300L
    }
}