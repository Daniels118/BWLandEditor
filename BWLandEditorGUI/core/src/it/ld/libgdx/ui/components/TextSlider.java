/* Copyright (c) 2026 Daniele Lombardi / Daniels118
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.ld.libgdx.ui.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class TextSlider extends Table {
    public interface ValueChangeListener {
        void onValueChanged(float newValue);
    }
    
    public interface ContinuousChangeListener extends ValueChangeListener {
        void onStart();
        void onEnd();
    }

    private final Slider slider;
    private final TextField textField;
    private final TextButton minusButton;
    private final TextButton plusButton;

    private float value;
    private float min;
    private float max;
    private float step;

    private boolean integerOnly;
    private int decimals;
    
    private float repeatInitialDelay = 0.45f;
    private float repeatInterval = 0.08f;

    private boolean minusHeld = false;
    private boolean plusHeld = false;
    private float holdTimer = 0f;
    private float repeatTimer = 0f;

    private ValueChangeListener valueChangeListener;
    private ContinuousChangeListener continuousChangeListener;
    private boolean programmaticChange = false;
    
    public TextSlider(Skin skin, float initialValue, float min, float max, float step) {
        this(skin, initialValue, min, max, step, false, -1, "default", "default-horizontal");
    }
    
    public TextSlider(Skin skin, float initialValue, float min, float max, float step, boolean integerOnly) {
        this(skin, initialValue, min, max, step, integerOnly, -1, "default", "default-horizontal");
    }
    
    public TextSlider(Skin skin, float initialValue, float min, float max, float step, int decimals) {
    	this(skin, initialValue, min, max, step, false, decimals, "default", "default-horizontal");
    }
    
    public TextSlider(Skin skin, float initialValue, float min, float max, float step, boolean integerOnly, int decimals, String textFieldStyle, String sliderStyle) {
        if (step <= 0) {
            throw new IllegalArgumentException("step must be > 0");
        }
        if (max < min) {
            throw new IllegalArgumentException("max must be >= min");
        }
        this.setSkin(skin);
        
        this.min = min;
        this.max = max;
        this.step = step;
        this.integerOnly = integerOnly;
        if (integerOnly) {
        	this.decimals = 0;
        } else if (decimals >= 0) {
        	this.decimals = decimals;
        } else {
        	DecimalFormat df = new DecimalFormat("#", DecimalFormatSymbols.getInstance(Locale.US));
        	df.setMaximumFractionDigits(7);
        	String s = df.format(step);
        	while (s.endsWith("0")) s = s.substring(0, s.length() - 1);
        	if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
        	int p = s.indexOf('.');
        	this.decimals = p < 0 ? 0 : (s.length() - p - 1);
        }

        this.value = clampAndNormalize(initialValue);

        slider = new Slider(min, max, step, false, skin, sliderStyle);
        slider.setValue(this.value);
        textField = new TextField(formatValue(this.value), skin, textFieldStyle);
        minusButton = new TextButton("-", skin);
        plusButton = new TextButton("+", skin);

        setupTextField();
        setupListeners();
        buildLayout();
    }
    
    private void buildLayout() {
        defaults().pad(1);

        add(slider).minWidth(30).prefWidth(120).expandX();
        add(textField).minWidth(20).prefWidth(80);
        
        float halfh = Math.max(slider.getPrefHeight(), textField.getPrefHeight()) / 2f;
        Table t = new Table();
        t.add(plusButton).size(halfh).row();
        t.add(minusButton).size(halfh);
        add(t).fillY();
    }

    private void setupTextField() {
        textField.setAlignment(Align.right);
        final char sep = ((DecimalFormat)DecimalFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator();
        textField.setTextFieldFilter((field, c) -> {
            if (Character.isDigit(c)) return true;
            if (c == '-' && min < 0) return true;
            if (!integerOnly && c == sep) return true;
            return false;
        });
        minusButton.addListener(new HoldButtonListener(() -> changeBy(-step), true));
        plusButton.addListener(new HoldButtonListener(() -> changeBy(step), false));
    }

    private void setupListeners() {
    	slider.addListener(new InputListener() {
    		@Override
    		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
    			if (continuousChangeListener != null) {
                	continuousChangeListener.onStart();
                }
    			return true;
    		}
    		
    		@Override
    		public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
    			if (continuousChangeListener != null) {
                	continuousChangeListener.onEnd();
                }
    		}
    	});
    	
        slider.addListener(new ChangeListener() {
        	@Override
			public void changed(ChangeEvent event, Actor actor) {
        		if (!programmaticChange) {
        			setValue(slider.getValue());
        		}
			}
        });
        
        textField.addListener(new FocusListener() {
        	@Override
        	public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
        		if (!programmaticChange) {
                    validateLiveText();
                }
        	}
        });

        textField.setFocusTraversal(false);
    }
    
    public TextFieldStyle getTextFieldStyle() {
    	return textField.getStyle();
    }
    
    public void setTextFieldStyle(TextFieldStyle style) {
    	textField.setStyle(style);
    }
    
    public void setTextFieldStyle(String styleName) {
    	textField.setStyle(getSkin().get(styleName, TextFieldStyle.class));
    }
    
    @Override
    public void act(float delta) {
        super.act(delta);
        
        if (minusHeld || plusHeld) {
            holdTimer += delta;

            if (holdTimer >= repeatInitialDelay) {
                repeatTimer += delta;
                if (repeatTimer >= repeatInterval) {
                    repeatTimer = 0;
                    if (minusHeld) {
                        changeBy(-step);
                    }
                    if (plusHeld) {
                        changeBy(step);
                    }
                }
            }
        }
    }
    
    private void validateLiveText() {
        String text = textField.getText();
        if (text == null) {
            return;
        }

        text = text.trim();

        if (text.isEmpty() || text.equals("-") || text.equals(".") || text.equals(",") || text.equals("-.") || text.equals("-,")) {
            return;
        }

        try {
        	float parsed = parseText(text);
        	float normalized = clampAndNormalize(parsed);

            if (Math.abs(normalized - value) > epsilon()) {
                value = normalized;
                notifyValueChanged();
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private float parseText(String text) {
        text = text.trim().replace(',', '.');
        if (integerOnly) {
            return Long.parseLong(text);
        }
        return Float.parseFloat(text);
    }
    
    private void changeBy(float delta) {
        setValue(value + delta);
    }

    public float getValue() {
        commitText();
        return value;
    }

    public int getIntValue() {
        commitText();
        return (int) Math.round(value);
    }

    public void setValue(float newValue) {
    	float normalized = clampAndNormalize(newValue);
        boolean changed = Math.abs(normalized - value) > epsilon();

        value = normalized;
        updateFields();

        if (changed) {
            notifyValueChanged();
        }
    }

    public void commitText() {
        String text = textField.getText();
        if (text == null || text.trim().isEmpty() || text.trim().equals("-")) {
            updateFields();
            return;
        }

        try {
        	float parsed = parseText(text);
            float normalized = clampAndNormalize(parsed);
            boolean changed = Math.abs(normalized - value) > epsilon();

            value = normalized;
            updateFields();

            if (changed) {
                notifyValueChanged();
            }
        } catch (NumberFormatException e) {
            updateFields();
        }
    }

    private void updateFields() {
        programmaticChange = true;
        slider.setValue(value);
        textField.setText(formatValue(value));
        textField.setCursorPosition(textField.getText().length());
        programmaticChange = false;
    }

    private float clampAndNormalize(float v) {
        v = MathUtils.clamp(v, min, max);

        if (integerOnly) {
            return Math.round(v);
        }

        float factor = (float)Math.pow(10, decimals);
        return Math.round(v * factor) / factor;
    }

    private String formatValue(float v) {
        if (integerOnly) {
            return String.valueOf((long) Math.round(v));
        }
        return String.format("%." + decimals + "f", v);
    }

    private float epsilon() {
        return integerOnly ? 0f : 1f / (float)Math.pow(10, decimals + 2);
    }

    private void notifyValueChanged() {
        if (valueChangeListener != null) {
            valueChangeListener.onValueChanged(value);
        }
        if (continuousChangeListener != null) {
        	continuousChangeListener.onValueChanged(value);
        }
    }

    public void setValueChangeListener(ValueChangeListener listener) {
        this.valueChangeListener = listener;
    }
    
    public void setContinuousChangeListener(ContinuousChangeListener listener) {
        this.continuousChangeListener = listener;
    }

    public TextField getTextField() {
        return textField;
    }

    public Slider getSlider() {
        return slider;
    }
    
    
    private class HoldButtonListener extends InputListener {
        private final Runnable action;
        private final boolean minus;

        public HoldButtonListener(Runnable action, boolean minus) {
            this.action = action;
            this.minus = minus;
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        	if (continuousChangeListener != null) {
            	continuousChangeListener.onStart();
            }
        	
        	action.run();

            holdTimer = 0f;
            repeatTimer = 0f;

            if (minus) {
                minusHeld = true;
            } else {
                plusHeld = true;
            }

            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        	off();
            holdTimer = 0f;
            repeatTimer = 0f;
        }

        @Override
        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            if (pointer != -1) {
                off();
            }
        }
        
        private void off() {
        	if (minus) {
            	if (minusHeld) {
            		minusHeld = false;
            		if (continuousChangeListener != null) {
                    	continuousChangeListener.onEnd();
                    }
            	}
            } else {
            	if (plusHeld) {
            		plusHeld = false;
            		if (continuousChangeListener != null) {
                    	continuousChangeListener.onEnd();
                    }
            	}
            }
        }
    }
}