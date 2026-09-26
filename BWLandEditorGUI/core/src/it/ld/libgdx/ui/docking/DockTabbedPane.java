package it.ld.libgdx.ui.docking;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import java.util.ArrayList;
import java.util.List;

public class DockTabbedPane extends Table {
    private final Skin skin;
    private final Table tabBar = new Table();
    private final Table content = new Table();

    private final List<Actor> tabs = new ArrayList<>();
    private Actor selected;

    public DockTabbedPane(Skin skin) {
        this.skin = skin;

        add(tabBar).expandX().fillX().row();
        add(content).expand().fill();
    }

    public void addTab(String title, Actor actor) {
        tabs.add(actor);

        TextButton button = new TextButton(title, skin);
        button.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                select(actor);
            }
        });

        tabBar.add(button).left().padRight(2);
        select(actor);
    }

    public void select(Actor actor) {
        if (!tabs.contains(actor)) return;

        selected = actor;
        content.clearChildren();

        if (actor.getParent() != null) {
            actor.remove();
        }

        content.add(actor).expand().fill();
        invalidateHierarchy();
    }

    public Actor getSelected() {
        return selected;
    }

    public void removeTab(Actor actor) {
        tabs.remove(actor);

        if (selected == actor) {
            selected = null;
            content.clearChildren();

            if (!tabs.isEmpty()) {
                select(tabs.get(0));
            }
        }

        rebuildTabBar();
    }

    private void rebuildTabBar() {
        tabBar.clearChildren();

        for (Actor actor : tabs) {
            String title = actor.getName() != null ? actor.getName() : "Window";

            TextButton button = new TextButton(title, skin);
            button.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
                @Override
                public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                    select(actor);
                }
            });

            tabBar.add(button).left().padRight(2);
        }
    }
}