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
package it.ld.libgdx.utils;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;

import it.ld.utils.EditManager;

/**This class simplifies the handling of transactions on a {@link EditManager} when dealing with components which may generate
 * continuous change events, such as sliders, buttons and text fields.
 * <p>
 * 	The helper will call {@link EditManager#begin(String)} when the component is pressed, and {@link EditManager#end()} when it
 * 	is released, letting the EditManager to collect all change events in a single transaction.<br/>
 * 	For {@link TextField text fields} the focus in/out are used instead of the mouse press events.
 * </p>
 * <p>
 * 	Any individual {@link Actor} can be managed using {@link #attachActors(Actor...)} the method, however all instances of the registered
 * 	classes which belong to a {@link Group} can be added in one shot using the {@link #attachGroup(Group)} method.
 * </p>
 * <p>
 * 	Any class extending Actor can be registered using the {@link #registerInputClass(Class...)} method. By default the following
 * 	classes are registered:
 * 	<ul>
 * 		<li>{@link Button}</li>
 * 		<li>{@link Slider}</li>
 * 		<li>{@link TextField}</li>
 * 	</ul>
 * </p>
 */
public class UndoHelper {
	private static final Set<Class<? extends Actor>> inputClasses = new HashSet<>();
	
	{
		inputClasses.add(Slider.class);
		inputClasses.add(Button.class);
		inputClasses.add(TextField.class);
	}
	
	private final EditManager manager;
	private final String description;
	
	private final InputListener downListener = new InputListener() {
		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
			manager.begin(description);
			return false;
		};
	};
	
	private final InputListener upListener = new InputListener() {
		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
			return true;
		};
		
		public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
			manager.end();
		};
	};
	
	private final FocusListener focusListener = new FocusListener() {
		@Override
		public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
			if (event.isFocused()) {
				manager.begin(description);
			} else {
				manager.end();
			}
		}
	};
	
	/**
	 * @param manager
	 * @param description the default description of the edit which will be passed to {@link EditManager#begin(String)}.
	 */
	public UndoHelper(EditManager manager, String description) {
		this.manager = manager;
		this.description = description;
	}
	
	/**Manage the given actor.
	 * @param actors
	 * @return the UndoHelper for chaining.
	 */
	public UndoHelper attachActors(Actor...actors) {
		for (Actor actor : actors) {
			if (actor instanceof TextField) {
				actor.addListener(focusListener);
			} else {
				actor.addCaptureListener(downListener);
				actor.addListener(upListener);
			}
		}
		return this;
	}
	
	/**Manage the given actors with a custom edit description.
	 * @param description the description of the edit which will be passed to {@link EditManager#begin(String)}.
	 * @param actors
	 * @return the UndoHelper for chaining.
	 */
	public UndoHelper attachActors(String description, Actor...actors) {
		InputListener downListener = new InputListener() {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				manager.begin(description);
				return false;
			};
		};
		FocusListener focusListener = new FocusListener() {
			@Override
			public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
				if (event.isFocused()) {
					manager.begin(description);
				} else {
					manager.end();
				}
			}
		};
		for (Actor actor : actors) {
			if (actor instanceof TextField) {
				actor.addListener(focusListener);
			} else {
				actor.addCaptureListener(downListener);
				actor.addListener(upListener);
			}
		}
		return this;
	}
	
	/**Recursively scans the given container and attach any instance of the registered classes.
	 * @param group group the group to scan.
	 * @return the UndoHelper for chaining.
	 */
	public UndoHelper attachGroup(Group group) {
		for (Actor actor : group.getChildren()) {
			if (canHandle(actor)) {
				attachActors(actor);
			} else if (actor instanceof Group) {
				attachGroup((Group)actor);
			}
		}
		return this;
	}
	
	/**Recursively scans the given container and attach any instance of the registered classes.
	 * @param description the description of the edit which will be passed to {@link EditManager#begin(String)}.
	 * @param group the group to scan.
	 * @return
	 */
	public UndoHelper attachGroup(String description, Group group) {
		for (Actor actor : group.getChildren()) {
			if (canHandle(actor)) {
				attachActors(description, actor);
			} else if (actor instanceof Group) {
				attachGroup(description, (Group)actor);
			}
		}
		return this;
	}
	
	/**Helper method to create an instance of UndoHelper and to attach a group in one shot.
	 * @param group group the group to scan.
	 * @param manager
	 * @param description the default description of the edit which will be passed to {@link EditManager#begin(String)}.
	 * @return the new UndoHelper for chaining.
	 */
	public static UndoHelper attachGroup(Group group, EditManager manager, String description) {
		UndoHelper helper = new UndoHelper(manager, description);
		helper.attachGroup(group);
		return helper;
	}
	
	/**Register the given classes to the be attached automatically when scanning a {@link Group}.
	 * @param classes
	 */
	@SafeVarargs
	public static void registerInputClass(Class<? extends Actor>...classes) {
		for (Class<? extends Actor> clazz : classes) {
			inputClasses.add(clazz);
		}
	}
	
	/**Checks if the given actor can be handled. The default implementation will check if the class of the given
	 * actor is one of the registered classes. You may override this method to provide a different criteria.
	 * @param actor
	 * @return
	 */
	protected boolean canHandle(Actor actor) {
		Class<? extends Actor> actorClass = actor.getClass();
		for (Class<? extends Actor> clazz : inputClasses) {
			if (clazz.isAssignableFrom(actorClass)) {
				return true;
			}
		}
		return false;
	}
}
