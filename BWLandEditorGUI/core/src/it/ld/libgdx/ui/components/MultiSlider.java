package it.ld.libgdx.ui.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntMap;

import java.util.Arrays;

/**
 * Multi-slider with N cursors (thumb) based on Scene2D UI.
 * Supports both horizontal and vertical orientation, and min separation between cursors.
 */
public class MultiSlider extends Widget {

    public static class Style {
        public Drawable background;
        public Drawable knob;
        public Drawable knobOver;
        public Drawable knobDown;

        public Style() {}
        public Style(Slider.SliderStyle sliderStyle) {
            this.background = sliderStyle.background;
            this.knob = sliderStyle.knob;
            this.knobOver = sliderStyle.knobOver;
            this.knobDown = sliderStyle.knobDown;
        }
    }

    private final Style style;
    private boolean vertical;

    private float min;
    private float max;
    private float step;

    /** Min separation between cursors. */
    private float minSeparation;

    private float[] values; // always sorted
    private int hoverIndex = -1;

    // pointer -> thumbIndex (multitouch)
    private final IntIntMap pointerToThumb = new IntIntMap();
    // thumbIndex -> pointer
    private final IntMap<Integer> thumbToPointer = new IntMap<>();
    
    public MultiSlider(float min, float max, float step, float minSeparation, boolean vertical, float[] initialValues, Style style) {
        if (style == null || style.background == null || style.knob == null) {
            throw new IllegalArgumentException("Style muste have at least background and knob");
        }
        if (max <= min) throw new IllegalArgumentException("max must be greater than min");
        if (step <= 0) throw new IllegalArgumentException("step must be grater than 0");
        if (minSeparation < 0) throw new IllegalArgumentException("minSeparation must be greater than or equal to 0");
        this.min = min;
        this.max = max;
        this.step = step;
        this.minSeparation = minSeparation;
        this.vertical = vertical;
        this.style = style;

        setValues(initialValues);

        setSize(getPrefWidth(), getPrefHeight());
        addListener(buildInputListener());
    }

    public MultiSlider(float min, float max, float step, float minSeparation, boolean vertical, int thumbs, Skin skin, String sliderStyleName) {
        this(min, max, step, minSeparation, vertical,
                buildEvenlySpaced(min, max, thumbs),
                new Style(skin.get(sliderStyleName, Slider.SliderStyle.class)));
    }
    
    public MultiSlider(float min, float max, float step, float minSeparation, boolean vertical, int thumbs, Skin skin) {
    	this(min, max, step, minSeparation, vertical, thumbs, skin, "default-" + (vertical ? "vertical" : "horizontal"));
    }

    public boolean isVertical() { return vertical; }
    public void setVertical(boolean vertical) { this.vertical = vertical; invalidateHierarchy(); }

    public float getMinSeparation() { return minSeparation; }
    public void setMinSeparation(float minSeparation) {
        if (minSeparation < 0) throw new IllegalArgumentException("minSeparation deve essere >= 0.");
        this.minSeparation = minSeparation;
        enforceNoCrossing();
        fireChanged();
    }

    public float getPrefWidth() {
        if (vertical) return 24f;
        float w = 0;
        w = Math.max(w, style.background.getMinWidth());
        w = Math.max(w, style.knob.getMinWidth());
        if (style.knobOver != null) w = Math.max(w, style.knobOver.getMinWidth());
        if (style.knobDown != null) w = Math.max(w, style.knobDown.getMinWidth());
        return Math.max(w, 24f);
    }

    public float getPrefHeight() {
    	if (!vertical) return 24f;
        float h = 0;
        h = Math.max(h, style.background.getMinHeight());
        h = Math.max(h, style.knob.getMinHeight());
        if (style.knobOver != null) h = Math.max(h, style.knobOver.getMinHeight());
        if (style.knobDown != null) h = Math.max(h, style.knobDown.getMinHeight());
        return Math.max(h, 24f);
    }

    public int getThumbCount() { return values.length; }

    public float[] getValues() {
        return Arrays.copyOf(values, values.length);
    }

    public void setValues(float[] newValues) {
        this.values = Arrays.copyOf(newValues, newValues.length);
        for (int i = 0; i < values.length; i++) {
        	values[i] = quantizeClamp(values[i]);
        }
        Arrays.sort(values);
        enforceNoCrossing();
        invalidateHierarchy();
    }
    
    public void setValue(int index, float newValue) {
        values[index] = quantizeClamp(newValue);
        Arrays.sort(values);
        enforceNoCrossing();
        invalidateHierarchy();
    }

    public void setThumbCount(int thumbs) {
        if (thumbs <= 0) throw new IllegalArgumentException("thumbs deve essere > 0");
        float[] next = buildEvenlySpaced(min, max, thumbs);

        // preserva il più possibile
        float[] old = values;
        int m = Math.min(old.length, next.length);
        for (int i = 0; i < m; i++) next[i] = old[Math.min(i, old.length - 1)];

        setValues(next);
        clearPointers();
        fireChanged();
    }

    public void setRange(float min, float max) {
        if (max <= min) throw new IllegalArgumentException("max deve essere > min.");
        this.min = min;
        this.max = max;
        for (int i = 0; i < values.length; i++) values[i] = quantizeClamp(values[i]);
        Arrays.sort(values);
        enforceNoCrossing();
        fireChanged();
    }

    public void setStep(float step) {
        if (step <= 0) throw new IllegalArgumentException("step deve essere > 0.");
        this.step = step;
        for (int i = 0; i < values.length; i++) values[i] = quantizeClamp(values[i]);
        Arrays.sort(values);
        enforceNoCrossing();
        fireChanged();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();

        // track
        final Drawable bg = style.background;
        float bgW = vertical ? bg.getMinWidth() : getWidth();
        float bgH = vertical ? getHeight() : bg.getMinHeight();
        float bgX = getX() + (getWidth() - bgW) * 0.5f;
        float bgY = getY() + (getHeight() - bgH) * 0.5f;
        bg.draw(batch, bgX, bgY, bgW, bgH);

        // thumbs
        for (int i = 0; i < values.length; i++) {
            boolean isDown = thumbToPointer.containsKey(i);
            boolean isHover = (i == hoverIndex);

            Drawable knobDrawable = style.knob;
            if (isDown && style.knobDown != null) knobDrawable = style.knobDown;
            else if (isHover && style.knobOver != null) knobDrawable = style.knobOver;

            float kW = knobDrawable.getMinWidth();
            float kH = knobDrawable.getMinHeight();
            if (vertical) {
            	float t = kW;
            	kW = kH;
            	kH = t;
            }

            float cx = axisValueToPos(values[i]); // in local coords [0..width] or [0..height]

            float kX, kY;
            if (!vertical) {
                kX = getX() + cx - kW * 0.5f;
                kY = getY() + (getHeight() - kH) * 0.5f;
            } else {
                kX = getX() + (getWidth() - kW) * 0.5f;
                kY = getY() + cx - kH * 0.5f;
            }

            knobDrawable.draw(batch, kX, kY, kW, kH);
        }
    }

    private InputListener buildInputListener() {
        return new InputListener() {
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                hoverIndex = pickThumbIndex(x, y, -1);
                return false;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hoverIndex = -1;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                int idx = pickThumbIndex(x, y, pointer);
                if (idx < 0) idx = findNearestThumb(axisLocal(x, y));

                if (thumbToPointer.containsKey(idx)) return false;

                pointerToThumb.put(pointer, idx);
                thumbToPointer.put(idx, pointer);

                setThumbValueFromLocalAxis(idx, axisLocal(x, y));
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (!pointerToThumb.containsKey(pointer)) return;
                int idx = pointerToThumb.get(pointer, -1);
                if (idx < 0) return;
                setThumbValueFromLocalAxis(idx, axisLocal(x, y));
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                releasePointer(pointer);
            }
        };
    }

    private float axisLocal(float localX, float localY) {
        return vertical ? localY : localX;
    }

    private void setThumbValueFromLocalAxis(int idx, float localAxis) {
        float v = axisPosToValue(localAxis);

        // bounds con minSeparation
        float leftBound  = (idx == 0) ? min : (values[idx - 1] + minSeparation);
        float rightBound = (idx == values.length - 1) ? max : (values[idx + 1] - minSeparation);

        // se la separazione richiesta è impossibile (troppi cursori), clampa comunque.
        v = MathUtils.clamp(v, leftBound, rightBound);

        v = quantizeClamp(v);
        v = MathUtils.clamp(v, leftBound, rightBound);

        if (v != values[idx]) {
            values[idx] = v;
            enforceNoCrossing();
            fireChanged();
        }
    }

    private void fireChanged() {
        ChangeListener.ChangeEvent changeEvent = new ChangeListener.ChangeEvent();
        fire(changeEvent);
    }

    private void clearPointers() {
        pointerToThumb.clear();
        thumbToPointer.clear();
    }

    private void releasePointer(int pointer) {
        if (!pointerToThumb.containsKey(pointer)) return;
        int idx = pointerToThumb.get(pointer, -1);
        pointerToThumb.remove(pointer, -1);
        thumbToPointer.remove(idx);
    }

    private float quantizeClamp(float v) {
        v = MathUtils.clamp(v, min, max);
        float steps = Math.round((v - min) / step);
        float q = min + steps * step;
        return MathUtils.clamp(q, min, max);
    }

    private void enforceNoCrossing() {
        // Forward pass: force val >= prevVal + minSeparation
        for (int i = 1; i < values.length; i++) {
            float minAllowed = values[i - 1] + minSeparation;
            if (values[i] < minAllowed) values[i] = minAllowed;
        }
        // Backward pass: forece val <= nextVal - minSeparation
        for (int i = values.length - 2; i >= 0; i--) {
            float maxAllowed = values[i + 1] - minSeparation;
            if (values[i] > maxAllowed) values[i] = maxAllowed;
        }

        // final clamp and quantization
        for (int i = 0; i < values.length; i++) {
        	values[i] = quantizeClamp(values[i]);
        }

        // reapply separation after quantization
        for (int i = 1; i < values.length; i++) {
            float minAllowed = values[i - 1] + minSeparation;
            if (values[i] < minAllowed) values[i] = MathUtils.clamp(minAllowed, min, max);
        }
        for (int i = values.length - 2; i >= 0; i--) {
            float maxAllowed = values[i + 1] - minSeparation;
            if (values[i] > maxAllowed) values[i] = MathUtils.clamp(maxAllowed, min, max);
        }

        // absolute clamp
        for (int i = 0; i < values.length; i++) {
        	values[i] = MathUtils.clamp(values[i], min, max);
        }
    }

    private float trackStart() {
        float half = vertical ? style.knob.getMinHeight() * 0.5f : style.knob.getMinWidth() * 0.5f;
        return half;
    }

    private float trackEnd() {
        float half = vertical ? style.knob.getMinHeight() * 0.5f : style.knob.getMinWidth() * 0.5f;
        float len = vertical ? getHeight() : getWidth();
        return len - half;
    }

    private float axisValueToPos(float v) {
        float t = (v - min) / (max - min);
        return MathUtils.lerp(trackStart(), trackEnd(), t);
    }

    private float axisPosToValue(float localAxis) {
        float p = MathUtils.clamp(localAxis, trackStart(), trackEnd());
        float t = (p - trackStart()) / (trackEnd() - trackStart());
        return min + t * (max - min);
    }

    private int findNearestThumb(float localAxis) {
        float best = Float.MAX_VALUE;
        int bestIdx = 0;
        for (int i = 0; i < values.length; i++) {
            float dp = Math.abs(axisValueToPos(values[i]) - localAxis);
            if (dp < best) { best = dp; bestIdx = i; }
        }
        return bestIdx;
    }

    private int pickThumbIndex(float localX, float localY, int pointer) {
        for (int i = 0; i < values.length; i++) {
            if (thumbToPointer.containsKey(i)) {
                int p = thumbToPointer.get(i);
                if (p != pointer) continue;
            }

            Drawable knobDrawable = style.knob;
            float kW = knobDrawable.getMinWidth();
            float kH = knobDrawable.getMinHeight();
            float pos = axisValueToPos(values[i]);

            float kX, kY;
            if (!vertical) {
                kX = pos - kW * 0.5f;
                kY = (getHeight() - kH) * 0.5f;
            } else {
                kX = (getWidth() - kW) * 0.5f;
                kY = pos - kH * 0.5f;
            }

            if (localX >= kX && localX <= kX + kW && localY >= kY && localY <= kY + kH)
                return i;
        }
        return -1;
    }

    private static float[] buildEvenlySpaced(float min, float max, int thumbs) {
        float[] v = new float[thumbs];
        if (thumbs == 1) {
            v[0] = (min + max) * 0.5f;
            return v;
        }
        for (int i = 0; i < thumbs; i++) {
            float t = i / (float)(thumbs - 1);
            v[i] = MathUtils.lerp(min, max, t);
        }
        return v;
    }
}
