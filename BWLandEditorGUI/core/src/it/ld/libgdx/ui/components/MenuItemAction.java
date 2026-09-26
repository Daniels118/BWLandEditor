package it.ld.libgdx.ui.components;

import com.badlogic.gdx.Input.Keys;

public class MenuItemAction {
    public final String id;
    public String label;
    public final Runnable action;
    public final StateChecker enableStatus;
    public final StateChecker checkStatus;
    
    private String shortcut;
    private int shortcutKey = -1;
    private boolean shortcutCtrl;
    private boolean shortcutShift;
    private boolean shortcutAlt;
    
    public MenuItemAction(String id, String label, Runnable action) {
    	this(id, label, null, action, null, null);
    }
    
    public MenuItemAction(String id, String label, Runnable action, StateChecker status) {
    	this(id, label, null, action, status, null);
    }
    
    public MenuItemAction(String id, String label, Runnable action, StateChecker enableStatus, StateChecker checkStatus) {
    	this(id, label, null, action, enableStatus, checkStatus);
    }
    
    public MenuItemAction(String id, String label, String shortcut, Runnable action) {
    	this(id, label, shortcut, action, null, null);
    }
    
    public MenuItemAction(String id, String label, String shortcut, Runnable action, StateChecker enableStatus) {
    	this(id, label, shortcut, action, enableStatus, null);
    }
    
    public MenuItemAction(String id, String label, String shortcut, Runnable action, StateChecker enableStatus, StateChecker checkStatus) {
        this.id = id;
        this.label = label;
        this.shortcut = shortcut;
        this.action = action;
        this.enableStatus = enableStatus;
        this.checkStatus = checkStatus;
        if (shortcut != null) {
        	String[] parts = shortcut.split("\\+");
			for (int i = 0; i < parts.length - 1; i++) {
				String modifier = parts[i];
				if ("CTRL".equalsIgnoreCase(modifier)) {
					shortcutCtrl = true;
				} else if ("SHIFT".equalsIgnoreCase(modifier)) {
					shortcutShift = true;
				} else if ("ALT".equalsIgnoreCase(modifier)) {
					shortcutAlt = true;
				} else {
					System.err.println("Invalid shortcut modifier: " + this.shortcut);
				}
			}
			if (!shortcutCtrl && (shortcut.length() < 2)) {
				System.err.println("Invalid shortcut (CTRL is required with short keys): " + this.shortcut);
				this.shortcut = null;
			}
			String keyName = parts[parts.length - 1];
			if ("del".equalsIgnoreCase(keyName)) {
				this.shortcutKey = Keys.FORWARD_DEL;
			} else if ("backspace".equalsIgnoreCase(keyName)) {
				this.shortcutKey = Keys.BACKSPACE;
			} else {
				this.shortcutKey = Keys.valueOf(keyName);
			}
			if (this.shortcutKey == -1) {
				System.err.println("Invalid shortcut key: " + this.shortcut);
				this.shortcut = null;
			}
        }
    }
    
    public String getShortcut() {
    	return this.shortcut;
    }
    
    public int getShortcutKey() {
    	return this.shortcutKey;
    }
    
    public boolean isShortcutCtrl() {
    	return this.shortcutCtrl;
    }
    
    public boolean isShortcutShift() {
    	return this.shortcutShift;
    }
    
    public boolean isShortcutAlt() {
    	return this.shortcutAlt;
    }
    
    
    public interface StateChecker {
    	public boolean getStatus(MenuItemAction action);
    }
}
