package it.ld.libgdx.ui.docking;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class DockPreview extends Actor {
    private final ShapeRenderer renderer = new ShapeRenderer();

    private boolean visible;
    private float x, y, width, height;

    private final Color fill = new Color(0.2f, 0.45f, 1f, 0.25f);
    private final Color border = new Color(0.2f, 0.45f, 1f, 0.9f);

    public void show(float x, float y, float width, float height) {
        this.visible = true;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void hide() {
        this.visible = false;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!visible) return;

        Stage stage = getStage();
        if (stage == null) return;

        batch.end();

        renderer.setProjectionMatrix(stage.getCamera().combined);

        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(fill);
        renderer.rect(x, y, width, height);
        renderer.end();

        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(border);
        renderer.rect(x, y, width, height);
        renderer.end();

        batch.begin();
    }
}
