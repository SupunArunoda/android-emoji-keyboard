package com.zeuxislo.emojikeyboard;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.res.Resources;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

public class SoftKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
	
	static final boolean DEBUG = false;
	public static int count =0;// to check default keyboard
	private StringBuilder mComposing;
	private long mLastShiftTime;
	private boolean mCapsLock;
	private String mWordSeparators;
	private boolean mPredictionOn;
	private KeyboardView mInputView;
	private Mokokeyboard mQwertyKeyboard;
	private Mokokeyboard mSymbolsKeyboard;
	private Mokokeyboard mSymbolsShiftedKeyboard;
	private long mMetaState;
	private boolean mCompletionOn;
	private Resources mResources;
	private CandidateView mCandidateView;
	private CompletionInfo[] mCompletions;
	private Mokokeyboard mCurKeyboard;
	private Mokokeyboard mMokoKeyboard1;
	private Mokokeyboard mMokoKeyboard2;
	private Mokokeyboard mMokoKeyboard3;
	private Mokokeyboard mMokoKeyboard4;
	private Mokokeyboard mMokoKeyboardb1;
	private Mokokeyboard mMokoKeyboardb2;
	private Mokokeyboard mMokoKeyboardc1;
	private Mokokeyboard mMokoKeyboardc2;
	private Mokokeyboard mMokoKeyboardc3;
	private Mokokeyboard mMokoKeyboardc4;
	private Mokokeyboard mMokoKeyboardc5;
	private Mokokeyboard mMokoKeyboardd1;
	private Mokokeyboard mMokoKeyboardd2;
	private Mokokeyboard mMokoKeyboardd3;
	private Mokokeyboard mMokoKeyboarde1;
	private Mokokeyboard mMokoKeyboarde2;
	private Mokokeyboard mMokoKeyboarde3;
	private Mokokeyboard mMokoKeyboarde4;
	
	
	private Mokokeyboard mMokoKeyboarde_K;
	private Mokokeyboard mMokoKeyboarde_K_2;
	private Mokokeyboard mMokoKeyboarde_G;
	private Mokokeyboard mMokoKeyboarde_G_2;
	private Mokokeyboard mMokoKeyboarde_J;
	private Mokokeyboard mMokoKeyboarde_J_2;
	private Mokokeyboard mMokoKeyboarde_CH;
	private Mokokeyboard mMokoKeyboarde_CH_2;
	
	
	private int mLastDisplayWidth;
	
	ArrayList<Integer> list = new ArrayList<Integer>() {{
	    add(3530);add(3535);add(3536);add(3538);add(3540);add(3544);add(3551);add(3545);
	    add(3537);add(3539);add(3542);add(3546);add(3548);add(3549);add(3550);add(3570);add(3547);
	}};
	
	public SoftKeyboard() {
		this.mComposing = new StringBuilder();
	}
	
	private void checkToggleCapsLock() {
		long now = System.currentTimeMillis();
		if (this.mLastShiftTime + 800 > now) {
			this.mCapsLock = !this.mCapsLock;
			this.mLastShiftTime = 0;
		} else {
			this.mLastShiftTime = now;
		}
	}
	
	private void commitTyped(InputConnection inputConnection) {
		if (this.mComposing.length() > 0) {
			inputConnection.commitText(this.mComposing, 1);		// mComposing.length()
			mComposing.setLength(0);
			this.updateCandidates();
		}
    }
	
	private String getWordSeparators() {
		return this.mWordSeparators;
	}

	public void handleBackspace() {
		final int length = this.mComposing.length();
		if (length > 1) {
			this.mComposing.delete(length - 1, length);
			this.getCurrentInputConnection().setComposingText(this.mComposing, 1);
			this.updateCandidates();
		} else if (length > 0) {
			this.mComposing.setLength(0);
			this.getCurrentInputConnection().commitText("", 0);
			this.updateCandidates();
		} else {
			this.keyDownUp(KeyEvent.KEYCODE_DEL);
		}
		this.updateShiftKeyState(this.getCurrentInputEditorInfo());
	}
	
	private void handleCharacter(int primaryCode, int[] keyCodes) {
		if (isInputViewShown()) {
			if (this.mInputView.isShifted()) {
				primaryCode = Character.toUpperCase(primaryCode);
			}
		}
		if (this.isAlphabet(primaryCode) && this.mPredictionOn) {
			this.mComposing.append((char) primaryCode);
			this.getCurrentInputConnection().setComposingText(this.mComposing, 1);
            this.updateShiftKeyState(this.getCurrentInputEditorInfo());
            this.updateCandidates();
        } else {
            this.mComposing.append((char) primaryCode);
            this.getCurrentInputConnection().setComposingText(this.mComposing, 1);
        }
    }
	
	private void handleClose() {
		commitTyped(this.getCurrentInputConnection());
		this.requestHideSelf(0);
		this.mInputView.closing();
	}
	
	private void handleShift() {
		if (this.mInputView == null) {
			return;
		}

		Keyboard currentKeyboard = this.mInputView.getKeyboard();
		if (this.mQwertyKeyboard == currentKeyboard) {
        	
			this.checkToggleCapsLock();
			this.mInputView.setShifted(this.mCapsLock || !this.mInputView.isShifted());
            
		} else if (currentKeyboard == this.mSymbolsKeyboard) {
        	
			this.mSymbolsKeyboard.setShifted(true);
			this.mInputView.setKeyboard(this.mSymbolsShiftedKeyboard);
			this.mSymbolsShiftedKeyboard.setShifted(true);
            
		} else if (currentKeyboard == this.mSymbolsShiftedKeyboard) {
        	
			this.mSymbolsShiftedKeyboard.setShifted(false);
			this.mInputView.setKeyboard(this.mSymbolsKeyboard);
			this.mSymbolsKeyboard.setShifted(false);
            
		}
	}
	
	private boolean isAlphabet(int code) {
		return Character.isLetter(code);
    }
	
	private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }
	
	private void sendKey(int keyCode) {
		switch (keyCode) {
			case '\n':
				keyDownUp(KeyEvent.KEYCODE_ENTER);
				break;
			default:
				if (keyCode >= '0' && keyCode <= '9') {
					keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
				} else {
					getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
				}
				break;
		}
    }
	
	private void showOptionsMenu() {
		((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
	}
	
	private boolean translateKeyDown(int keyCode, KeyEvent event) {
		this.mMetaState = MetaKeyKeyListener.handleKeyDown(this.mMetaState, keyCode, event);
		int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(this.mMetaState));
        this.mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        InputConnection ic = this.getCurrentInputConnection();
        
        if (c == 0 || ic == null) {
            return false;
        }

		boolean dead = false;

		if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            dead = true;
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }

        if (this.mComposing.length() > 0) {
            char accent = this.mComposing.charAt(this.mComposing.length() - 1);
            int composed = KeyEvent.getDeadChar(accent, c);

            if (composed != 0) {
                c = composed;
                this.mComposing.setLength(this.mComposing.length() - 1);
            }
        }

        this.onKey(c, null);

        return true;
    }
	
	private void updateCandidates() {
		if (!mCompletionOn) {
			if (mComposing.length() > 0) {
				ArrayList<String> list = new ArrayList<String>();
				list.add(mComposing.toString());
				this.setSuggestions(list, true, true);
			} else {
				this.setSuggestions(null, false, false);
			}
		}
	}
	
	private void updateShiftKeyState(EditorInfo editorInfo) {
		if (editorInfo != null && mInputView != null && mQwertyKeyboard == mInputView.getKeyboard()) {
			int caps = 0;
			EditorInfo ei = getCurrentInputEditorInfo();
			if (ei != null && ei.inputType != EditorInfo.TYPE_NULL) {
				caps = getCurrentInputConnection().getCursorCapsMode(editorInfo.inputType);
			}
			mInputView.setShifted(mCapsLock || caps != 0);
		}
	}
	
	public boolean isWordSeparator(int code) {
		String separators = getWordSeparators();
		return separators.contains(String.valueOf((char) code));
    }
	
	public void pickDefaultCandidate() {
		this.pickSuggestionManually(0);
	}
	
	public void pickSuggestionManually(int index) {
		if (this.mCompletionOn && this.mCompletions != null && index >= 0 && index < this.mCompletions.length) {
			CompletionInfo ci = mCompletions[index];
			this.getCurrentInputConnection().commitCompletion(ci);
            
			if (this.mCandidateView != null) {
                this.mCandidateView.clear();
            }
            
			this.updateShiftKeyState(this.getCurrentInputEditorInfo());
        } else if (this.mComposing.length() > 0) {
            this.commitTyped(this.getCurrentInputConnection());
        }
    }
	
	public void setSuggestions(List<String> suggestions, boolean completions, boolean typedWordValid) {
		if (suggestions != null && suggestions.size() > 0) {
			this.setCandidatesViewShown(true);
		} else if (isExtractViewShown()) {
			this.setCandidatesViewShown(true);
		}
		
		if (this.mCandidateView != null) {
			this.mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
		}
    }
	
	public void changeEmojiKeyboard(Mokokeyboard[] emojiKeyboard) {
		int j = 0;
    	for(int i=0; i<emojiKeyboard.length; i++) {
    		if (emojiKeyboard[i] == this.mInputView.getKeyboard()) {
    			j = i;
    			break;
    		}
    	}
    	
    	if (j + 1 >= emojiKeyboard.length) {
    		this.mInputView.setKeyboard(emojiKeyboard[0]);
    	}else{
    		this.mInputView.setKeyboard(emojiKeyboard[j + 1]);
    	}		
	}
	
	public void changeEmojiKeyboardReverse(Mokokeyboard[] emojiKeyboard) {
		int j = emojiKeyboard.length - 1;
		for(int i=emojiKeyboard.length - 1; i>=0; i--) {
			if (emojiKeyboard[i] == this.mInputView.getKeyboard()) {
				j = i;
				break;
			}
		}
		
		if (j - 1 < 0) {
    		this.mInputView.setKeyboard(emojiKeyboard[emojiKeyboard.length - 1]);
    	}else{
    		this.mInputView.setKeyboard(emojiKeyboard[j - 1]);
    	}
	}
	
	public void onCreate() {
		super.onCreate();
		this.mResources = getResources();
		this.mWordSeparators = getResources().getString(R.string.word_separators);
	}
	
	public View onCreateCandidatesView() {
		this.mCandidateView = new CandidateView(this);
		this.mCandidateView.setService(this);
		return this.mCandidateView;
	}
	
	public View onCreateInputView() {
		this.mInputView = (KeyboardView) this.getLayoutInflater().inflate(R.layout.input, null);
		this.mInputView.setOnKeyboardActionListener(this);
		this.mInputView.setKeyboard(this.mQwertyKeyboard);
		return this.mInputView;
	}
	
	public void onDisplayCompletions(CompletionInfo[] completions) {
		if (this.mCompletionOn) {
            this.mCompletions = completions;
            if (completions == null) {
                this.setSuggestions(null, false, false);
                return;
            }

            List<String> stringList = new ArrayList<String>();
            for (int i = 0; i < (completions != null ? completions.length : 0); i++) {
                CompletionInfo ci = completions[i];
                if ((ci != null) && (ci.getText() != null))
                    stringList.add(ci.getText().toString());
            }
            this.setSuggestions(stringList, true, true);
        }
    }
	
	public void onFinishInput() {
		super.onFinishInput();

        this.mComposing.setLength(0);
		this.updateCandidates();
		this.setCandidatesViewShown(false);

        this.mCurKeyboard = mQwertyKeyboard;
        if (this.mInputView != null) {
            this.mInputView.closing();
        }
    }
	
	public void onInitializeInterface() {
		if (this.mQwertyKeyboard != null) {
			int displayWidth = getMaxWidth();
			
			if (displayWidth == mLastDisplayWidth) {
                return;
			}
			
			mLastDisplayWidth = displayWidth;
		}
		
		this.mQwertyKeyboard = new Mokokeyboard(this, R.xml.qwerty);
		this.mSymbolsKeyboard = new Mokokeyboard(this, R.xml.symbols);
		this.mSymbolsShiftedKeyboard = new Mokokeyboard(this, R.xml.symbols_shift);
		
		this.mMokoKeyboard1 = new Mokokeyboard(this, R.xml.moko_a1);
		this.mMokoKeyboard2 = new Mokokeyboard(this, R.xml.moko_a2);
		this.mMokoKeyboard3 = new Mokokeyboard(this, R.xml.moko_a3);
		this.mMokoKeyboard4 = new Mokokeyboard(this, R.xml.moko_a4);
		
		this.mMokoKeyboardb1 = new Mokokeyboard(this, R.xml.moko_b1);
		this.mMokoKeyboardb2 = new Mokokeyboard(this, R.xml.moko_b2);
		
		this.mMokoKeyboardc1 = new Mokokeyboard(this, R.xml.moko_c1);
		this.mMokoKeyboardc2 = new Mokokeyboard(this, R.xml.moko_c2);
		this.mMokoKeyboardc3 = new Mokokeyboard(this, R.xml.moko_c3);
		this.mMokoKeyboardc4 = new Mokokeyboard(this, R.xml.moko_c4);
		this.mMokoKeyboardc5 = new Mokokeyboard(this, R.xml.moko_c5);

		this.mMokoKeyboardd1 = new Mokokeyboard(this, R.xml.moko_d1);
		this.mMokoKeyboardd2 = new Mokokeyboard(this, R.xml.moko_d2);
		this.mMokoKeyboardd3 = new Mokokeyboard(this, R.xml.moko_d3);

		this.mMokoKeyboarde1 = new Mokokeyboard(this, R.xml.moko_e1);
		this.mMokoKeyboarde2 = new Mokokeyboard(this, R.xml.moko_e2);
		this.mMokoKeyboarde3 = new Mokokeyboard(this, R.xml.moko_e3);
		this.mMokoKeyboarde4 = new Mokokeyboard(this, R.xml.moko_e4);
		
		this.mMokoKeyboarde_K = new Mokokeyboard(this, R.xml.letter_k);
		this.mMokoKeyboarde_K_2 = new Mokokeyboard(this, R.xml.letter_k_2);
		this.mMokoKeyboarde_G = new Mokokeyboard(this, R.xml.letter_g);
		this.mMokoKeyboarde_G_2 = new Mokokeyboard(this, R.xml.letter_g_2);
		this.mMokoKeyboarde_J = new Mokokeyboard(this, R.xml.letter_j);
		this.mMokoKeyboarde_J_2 = new Mokokeyboard(this, R.xml.letter_j_2);
		this.mMokoKeyboarde_CH = new Mokokeyboard(this, R.xml.letter_ch);
		this.mMokoKeyboarde_CH_2 = new Mokokeyboard(this, R.xml.letter_ch_2);
		
	}
	
	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		Log.d("Main", "Primary Code: " + primaryCode);
		
		if (this.isWordSeparator(primaryCode)) {
			if (this.mComposing.length() > 0) {
				this.commitTyped(this.getCurrentInputConnection());
			}
			this.sendKey(primaryCode);
			this.updateShiftKeyState(this.getCurrentInputEditorInfo());
		} else if (primaryCode == Keyboard.KEYCODE_DELETE) {
			this.handleBackspace();
        } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
			this.handleShift();
        } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
        	handleClose();
            return;
        } else if (primaryCode == MokoKeyboardView.KEYCODE_OPTIONS) {
        	this.showOptionsMenu();
        } else if (primaryCode == MokoKeyboardView.KEYCODE_SYMBOL && this.mInputView != null) {
        	this.mInputView.setKeyboard(this.mSymbolsKeyboard);
        	this.mInputView.setShifted(false);
        } 
        else if (primaryCode == MokoKeyboardView.KEYCODE_ABC && this.mInputView != null) {
        	this.mInputView.setKeyboard(this.mQwertyKeyboard);
        }
		
		
		
        else if (primaryCode == MokoKeyboardView.KEYCODE_CHANGE && this.mInputView != null) {
        	this.mInputView.setKeyboard(this.mMokoKeyboarde_K_2);
        }
        else if (primaryCode == MokoKeyboardView.KEYCODE_LETTER_K && this.mInputView != null) {
        	++count;
        	this.handleCharacter(primaryCode, keyCodes);
        	this.mInputView.setKeyboard(this.mMokoKeyboarde_K);
        }
		
		
        else if (primaryCode == MokoKeyboardView.KEYCODE_CHANGE_G && this.mInputView != null) {
        	this.mInputView.setKeyboard(this.mMokoKeyboarde_G_2);
        }
        else if (primaryCode == MokoKeyboardView.KEYCODE_LETTER_G && this.mInputView != null) {
        	++count;
        	this.handleCharacter(primaryCode, keyCodes);
        	this.mInputView.setKeyboard(this.mMokoKeyboarde_G);
        }
		
		
        else if (primaryCode == MokoKeyboardView.KEYCODE_CHANGE_J && this.mInputView != null) {
        	this.mInputView.setKeyboard(this.mMokoKeyboarde_J_2);
        }
        else if (primaryCode == MokoKeyboardView.KEYCODE_LETTER_J && this.mInputView != null) {
        	++count;
        	this.handleCharacter(primaryCode, keyCodes);
        	this.mInputView.setKeyboard(this.mMokoKeyboarde_J);
        }
        
        
        else if (primaryCode == MokoKeyboardView.KEYCODE_CHANGE_CH && this.mInputView != null) {
        	this.mInputView.setKeyboard(this.mMokoKeyboarde_CH_2);
        }
        else if (primaryCode == MokoKeyboardView.KEYCODE_LETTER_CH && this.mInputView != null) {
        	++count;
        	this.handleCharacter(primaryCode, keyCodes);
        	this.mInputView.setKeyboard(this.mMokoKeyboarde_CH);
        }
        
		
        else if (primaryCode == MokoKeyboardView.KEYCODE_EMOJI && this.mInputView != null) {
        	this.mInputView.setKeyboard(this.mMokoKeyboard1);
        	this.mInputView.setShifted(false);
        }else if(list.contains(primaryCode) || primaryCode==3458){
        	count=0;
        	this.handleCharacter(primaryCode, keyCodes);
        	this.mInputView.setKeyboard(this.mQwertyKeyboard);
        }
        
        else if (primaryCode == MokoKeyboardView.KEYCODE_EMOJI_1 && this.mInputView != null) {
        	this.changeEmojiKeyboard(new Mokokeyboard[] {
        		this.mMokoKeyboard1,
        		this.mMokoKeyboard2,
        		this.mMokoKeyboard3,
        		this.mMokoKeyboard4
        	});
        } else if (primaryCode == MokoKeyboardView.KEYCODE_EMOJI_2 && this.mInputView != null) {
        	this.changeEmojiKeyboard(new Mokokeyboard[] {
            	this.mMokoKeyboardb1,
            	this.mMokoKeyboardb2
            });
        } else if (primaryCode == MokoKeyboardView.KEYCODE_EMOJI_3 && this.mInputView != null) {
        	this.changeEmojiKeyboard(new Mokokeyboard[] {
            	this.mMokoKeyboardc1,
            	this.mMokoKeyboardc2,
            	this.mMokoKeyboardc3,
            	this.mMokoKeyboardc4,
            	this.mMokoKeyboardc5
            });
        } else if (primaryCode == MokoKeyboardView.KEYCODE_EMOJI_4 && this.mInputView != null) {
        	this.changeEmojiKeyboard(new Mokokeyboard[] {
            	this.mMokoKeyboardd1,
            	this.mMokoKeyboardd2,
            	this.mMokoKeyboardd3
            });
        } else if (primaryCode == MokoKeyboardView.KEYCODE_EMOJI_5 && this.mInputView != null) {
        	this.changeEmojiKeyboard(new Mokokeyboard[] {
            	this.mMokoKeyboarde1,
            	this.mMokoKeyboarde2,
            	this.mMokoKeyboarde3,
            	this.mMokoKeyboarde4
            });
        } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE && this.mInputView != null) {
        	Keyboard current = mInputView.getKeyboard();
        	
        	if (current == this.mSymbolsKeyboard || current == this.mSymbolsShiftedKeyboard) {
                current = this.mQwertyKeyboard;
            } else {
                current = this.mSymbolsKeyboard;
            }
        	
        	this.mInputView.setKeyboard(current);
        	
            if (current == mSymbolsKeyboard) {
                current.setShifted(false);
            }
        } else {
			this.handleCharacter(primaryCode, keyCodes);
        }
	}

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:		// 4
				if (event.getRepeatCount() == 0 && this.mInputView != null) {
					if (this.mInputView.handleBack()) {
						return true;
					}
				}
				break;
			case KeyEvent.KEYCODE_DEL:		// 64
				if (this.mComposing.length() > 0) {
					this.onKey(Keyboard.KEYCODE_DELETE, null);
					return true;
				}
				break;
			case KeyEvent.KEYCODE_ENTER:	// 67
				return false;
			default:
				if (this.mPredictionOn && this.translateKeyDown(keyCode, event)) {
					return true;
				}
				break;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (this.mPredictionOn) {
			this.mMetaState = MetaKeyKeyListener.handleKeyUp(this.mMetaState, keyCode, event);
        }

        return super.onKeyUp(keyCode, event);
    }
	
	@Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);

		this.mComposing.setLength(0);
		this.updateCandidates();

        if (!restarting) {
			this.mMetaState = 0;
        }

		this.mPredictionOn = false;
		this.mCompletionOn = false;
		this.mCompletions = null;

        switch (attribute.inputType & EditorInfo.TYPE_MASK_CLASS) {
        	case EditorInfo.TYPE_CLASS_NUMBER:		// 2
				this.mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
        		break;
        	case EditorInfo.TYPE_CLASS_DATETIME:	// 4
				this.mCurKeyboard = this.mSymbolsKeyboard;
        		break;
        	case EditorInfo.TYPE_CLASS_PHONE:		// 3
        		this.mCurKeyboard = this.mSymbolsKeyboard;
        		break;
        	case EditorInfo.TYPE_CLASS_TEXT:		// 1
        		this.mCurKeyboard = this.mQwertyKeyboard;
        		this.mPredictionOn = true;
        		
        		int variation = attribute.inputType & EditorInfo.TYPE_MASK_VARIATION;
        		if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD || variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
        			this.mPredictionOn = false;
        		}
        		
        		if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS || variation == EditorInfo.TYPE_TEXT_VARIATION_URI || variation == EditorInfo.TYPE_TEXT_VARIATION_FILTER) {
        			this.mPredictionOn = false;
        		}
        		
        		if ((attribute.inputType & EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
					this.mPredictionOn = false;
					this.mCompletionOn = this.isFullscreenMode();
                }
        		
        		updateShiftKeyState(attribute);
        		break;
        	default:
				this.mCurKeyboard = this.mQwertyKeyboard;
				this.updateShiftKeyState(attribute);
        		break;
        }
	}

	@Override
	public void onStartInputView(EditorInfo attribute, boolean restarting) {
		super.onStartInputView(attribute, restarting);
		this.mInputView.setKeyboard(this.mCurKeyboard);
		this.mInputView.closing();
    }
	
	@Override
	public void onText(CharSequence text) {
		InputConnection ic = getCurrentInputConnection();
		
		if (ic == null)
            return;
        
		ic.beginBatchEdit();
        
		if (this.mComposing.length() > 0) {
			this.commitTyped(ic);
		}
		
		ic.commitText(text, 0);
		ic.endBatchEdit();
		
		this.updateShiftKeyState(this.getCurrentInputEditorInfo());
    }
	
	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);

		if (this.mComposing.length() > 0 && (newSelStart != candidatesEnd || newSelEnd != candidatesEnd)) {
			this.mComposing.setLength(0);
			this.updateCandidates();
			
			InputConnection ic = getCurrentInputConnection();
			
			if (ic != null) {
				ic.finishComposingText();
			}
		}
	}
	
	@Override
	public void onPress(int primaryCode) {
	}

	@Override
	public void onRelease(int primaryCode) {
	}

	@Override
	public void swipeDown() {
		this.handleClose();	
	}

	@Override
	public void swipeLeft() {
		Log.d("Main", "swipe left");
		this.changeEmojiKeyboard(new Mokokeyboard[] {
			this.mQwertyKeyboard, this.mSymbolsKeyboard, this.mSymbolsShiftedKeyboard,
        	this.mMokoKeyboard1, this.mMokoKeyboard2, this.mMokoKeyboard3, this.mMokoKeyboard4,
        	this.mMokoKeyboardb1, this.mMokoKeyboardb2,
        	this.mMokoKeyboardc1, this.mMokoKeyboardc2, this.mMokoKeyboardc3, this.mMokoKeyboardc4, this.mMokoKeyboardc5,
        	this.mMokoKeyboardd1, this.mMokoKeyboardd2, this.mMokoKeyboardd3,
        	this.mMokoKeyboarde1, this.mMokoKeyboarde2, this.mMokoKeyboarde3, this.mMokoKeyboarde4,
        });
	}

	@Override
	public void swipeRight() {
		Log.d("Main", "swipe right");
		this.changeEmojiKeyboardReverse(new Mokokeyboard[] {
			this.mQwertyKeyboard, this.mSymbolsKeyboard, this.mSymbolsShiftedKeyboard,
	        this.mMokoKeyboard1, this.mMokoKeyboard2, this.mMokoKeyboard3, this.mMokoKeyboard4,
	        this.mMokoKeyboardb1, this.mMokoKeyboardb2,
	        this.mMokoKeyboardc1, this.mMokoKeyboardc2, this.mMokoKeyboardc3, this.mMokoKeyboardc4, this.mMokoKeyboardc5,
	        this.mMokoKeyboardd1, this.mMokoKeyboardd2, this.mMokoKeyboardd3,
	        this.mMokoKeyboarde1, this.mMokoKeyboarde2, this.mMokoKeyboarde3, this.mMokoKeyboarde4,
		});
	}

	@Override
	public void swipeUp() {
		// TODO Auto-generated method stub
		
	}

}
