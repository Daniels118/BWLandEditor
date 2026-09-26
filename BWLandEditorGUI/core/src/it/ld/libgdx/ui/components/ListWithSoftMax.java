package it.ld.libgdx.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class ListWithSoftMax<E> extends List<E> {
	private final int max;
	
	public ListWithSoftMax(int max, Skin skin) {
        super(skin);
        this.max = max;
    }

    @Override
    protected GlyphLayout drawItem(Batch batch, BitmapFont font, int index, E item, float x, float y, float width) {
    	Color oldColor = font.getColor();
    	if (index >= max) font.setColor(Color.RED);
		GlyphLayout r = super.drawItem(batch, font, index, item, x, y, width);
		font.setColor(oldColor);
		return r;
    }
}