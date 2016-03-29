package com.zeuxislo.emojikeyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

public class MokoKeyboardView extends KeyboardView {
	
	static final int KEYCODE_OPTIONS = -100;
	static final int KEYCODE_EMOJI = -10;
	static final int KEYCODE_ABC = -11;
	static final int KEYCODE_SYMBOL = -12;
	
	static final int KEYCODE_LETTER_K = 3482;
	static final int KEYCODE_LETTER_G = 3484;
	static final int KEYCODE_LETTER_J = 3489;
	static final int KEYCODE_LETTER_CH = 3488;
	
	static final int KEYCODE_CHANGE = -23;
	static final int KEYCODE_CHANGE_G = -24;
	static final int KEYCODE_CHANGE_J = -25;
	static final int KEYCODE_CHANGE_CH = -26;
	
	static final int KEYCODE_EMOJI_1 = -21;
	static final int KEYCODE_EMOJI_2 = -31;
	static final int KEYCODE_EMOJI_3 = -41;
	static final int KEYCODE_EMOJI_4 = -51;
	static final int KEYCODE_EMOJI_5 = -61;

	public MokoKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public MokoKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected boolean onLongPress(Key popupKey) {
		if (popupKey.codes[0] == 10) {
            getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
            return true;
        }
		
		if (popupKey.codes[0] == KEYCODE_ABC) {
			getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
			return true;
		}
		
		if (popupKey.codes[0] == KEYCODE_SYMBOL) {
			getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
			return true;
		}
		
		if (popupKey.codes[0] == KEYCODE_EMOJI) {
			getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
			return true;
		}
		
		return super.onLongPress(popupKey);
	}

}
