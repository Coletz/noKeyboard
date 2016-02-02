package com.coletz.nokeyboard;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.view.View;
import android.view.inputmethod.InputConnection;

public class NoKeyboard extends InputMethodService
        implements OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard qwertyKeyboard, symbolKeyboard1, symbolKeyboard2;
    private int layout; //0=qwerty, 1=QWERTY, 2=symbol1, 3=symbol2
    private long timerStart;
    private boolean caps, firstChar, keyIsShift;

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        qwertyKeyboard = new Keyboard(this, R.xml.qwerty);
        symbolKeyboard1 = new Keyboard(this, R.xml.symbol1);
        symbolKeyboard2 = new Keyboard(this, R.xml.symbol2);
        caps = true;
        layout = 1;
        kv.setKeyboard(qwertyKeyboard);
        qwertyKeyboard.setShifted(caps);
        kv.setPreviewEnabled(false);
        kv.setOnKeyboardActionListener(this);
        return kv;
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {}

    @Override
    public void onPress(int primaryCode) {
        timerStart = System.currentTimeMillis();
    }

    @Override
    public void onRelease(int primaryCode) {
        InputConnection ic = getCurrentInputConnection();
        String character;
        keyIsShift = false;
        switch(primaryCode){
            case Keyboard.KEYCODE_MODE_CHANGE:
                keyIsShift = true;
                layout = (layout == 3) ? 0 : layout+1;
                switch(layout){
                    case 0:
                        kv.setKeyboard(qwertyKeyboard);
                        caps = false;
                        qwertyKeyboard.setShifted(caps);
                        qwertyKeyboard.getKeys().get(26).label = "ABC";
                        kv.invalidateAllKeys();
                        break;
                    case 1:
                        caps = true;
                        qwertyKeyboard.setShifted(caps);
                        qwertyKeyboard.getKeys().get(26).label = "123";
                        kv.invalidateAllKeys();
                        break;
                    case 2:
                        caps = false;
                        kv.setKeyboard(symbolKeyboard1);
                        kv.invalidateAllKeys();
                        break;
                    case 3:
                        caps = false;
                        kv.setKeyboard(symbolKeyboard2);
                        kv.invalidateAllKeys();
                        break;
                }
                break;
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                break;
            case 97:
                if(System.currentTimeMillis()-timerStart < 300){
                    character = caps ? "A" : "a";
                }else{
                    character = caps ? "À" : "à";
                }
                ic.commitText(character, 1);
                break;
            case 101:
                if(System.currentTimeMillis()-timerStart < 300){
                    character = caps ? "E" : "e";
                }else{
                    character = caps ? "È" : "è";
                }
                ic.commitText(character, 1);
                break;
            case 105:
                if(System.currentTimeMillis()-timerStart < 300){
                    character = caps ? "I" : "i";
                }else{
                    character = caps ? "Ì" : "ì";
                }
                ic.commitText(character, 1);
                break;
            case 111:
                if(System.currentTimeMillis()-timerStart < 300){
                    character = caps ? "O" : "o";
                }else{
                    character = caps ? "Ò" : "ò";
                }
                ic.commitText(character, 1);
                break;
            case 117:
                if(System.currentTimeMillis()-timerStart < 300){
                    character = caps ? "U" : "u";
                }else{
                    character = caps ? "Ù" : "ù";
                }
                ic.commitText(character, 1);
                break;
            default:
                char code = (char)primaryCode;
                if(Character.isLetter(code) && caps){
                    code = Character.toUpperCase(code);
                }
                ic.commitText(String.valueOf(code),1);
        }
        if(firstChar && ic.getTextBeforeCursor(1,0).length()>=1){
            firstChar = false;
            layout = 0;
            caps = false;
            qwertyKeyboard.setShifted(caps);
            qwertyKeyboard.getKeys().get(26).label = "ABC";
            kv.invalidateAllKeys();
        }
        if(ic.getTextBeforeCursor(1,0).length()<1 && !keyIsShift){
            firstChar = true;
            layout = 1;
            caps = true;
            qwertyKeyboard.setShifted(caps);
            qwertyKeyboard.getKeys().get(26).label = "123";
            kv.invalidateAllKeys();
        }

    }

    @Override
    public void onText(CharSequence text) {}

    @Override
    public void swipeDown() {}

    @Override
    public void swipeLeft() {}

    @Override
    public void swipeRight() {}

    @Override
    public void swipeUp() {}

    @Override
    public boolean onEvaluateInputViewShown(){
        if(kv != null){
            firstChar = true;
            layout = 1;
            kv.setKeyboard(qwertyKeyboard);
            caps = true;
            qwertyKeyboard.setShifted(caps);
            qwertyKeyboard.getKeys().get(26).label = "123";
            kv.invalidateAllKeys();
        }
        return true;
    }

}