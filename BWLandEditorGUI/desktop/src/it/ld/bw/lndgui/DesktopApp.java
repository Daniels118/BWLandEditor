package it.ld.bw.lndgui;

import javax.swing.JOptionPane;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;

import it.ld.utils.UChangeListener;

public class DesktopApp implements ApplicationListener {
	private final MainApp app;
    private final DesktopApiServer apiServer;

    public DesktopApp(MainApp app) {
        this.app = app;
        this.apiServer = new DesktopApiServer();
        //
        Settings.listeners.add(settingsListener);
    }
    
    private final UChangeListener settingsListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getType() == EventType.CHANGE) {
				if (event.getProperty() == Settings.HTTP_PORT) {
	        		try {
	                	apiServer.stop();
	                } catch (Exception ignored) {}
	        		try {
	        			apiServer.start(app, Settings.HTTP_PORT.getInt());
	        		} catch (Exception e) {
	        			Gdx.app.postRunnable(() -> {
	        				app.showError("Settings", e);
	        			});
	        		}
				}
        	}
		}
    };

    @Override
    public void create() {
    	try {
	    	apiServer.start(app, Settings.HTTP_PORT.getInt());
    		app.create();
    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    		throw e;
    	}
    }

    @Override
    public void render() {
    	app.render();
    }
    
    @Override
    public void resize(int w, int h) {
    	app.resize(w, h);
    }
    
    @Override
    public void pause() {
    	app.pause();
    }
    
    @Override
    public void resume() {
    	app.resume();
    }
    
    public void exit() {
    	app.exit();
    }

    @Override
    public void dispose() {
    	Settings.listeners.remove(settingsListener);
    	try {
        	apiServer.stop();
        } catch (Exception ignored) {}
        app.dispose();
        System.out.println("HTTP server stopped.");
    }
}

