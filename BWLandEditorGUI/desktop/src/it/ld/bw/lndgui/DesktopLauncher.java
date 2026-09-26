/* Copyright (c) 2024-2026 Daniele Lombardi / Daniels118
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
package it.ld.bw.lndgui;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import javax.swing.JOptionPane;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.GLEmulation;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;

import it.ld.bw.lndgui.interfaces.ExternalEditor;
import it.ld.utils.ConsumerOutputStream;
import it.ld.utils.MultiOutputStream;

public class DesktopLauncher {
	public static void main(String[] args) {
		ConsumerOutputStream uiOut = configureLogging();
		//
    	DesktopOS os = new DesktopOS();
		ExternalEditor externalEditor = new HttpExternalEditor();
        MainApp app = new MainApp(os, externalEditor, uiOut);
        if (args.length == 1) {
        	app.setFileToOpen(new File(args[0]));
        }
        Graphics.DisplayMode dm = Lwjgl3ApplicationConfiguration.getDisplayMode();
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setOpenGLEmulation(GLEmulation.GL32, 3, 2);
		config.setBackBufferConfig(8, 8, 8, 8, 24, 8, 4);
		config.setForegroundFPS(25);
		config.setTitle(MainApp.APP_NAME);
		config.setWindowIcon("icons/app.png");
		config.setWindowSizeLimits(600, 460, -1, -1);
		int w = Math.min((int)(16f/9f*dm.height), dm.width) - 2;
		config.setWindowedMode(w, dm.height - 102);
		config.setWindowPosition(dm.width - w - 2, 38);
		//config.setMaximized(true);
		config.setWindowListener(new Lwjgl3WindowAdapter() {
			@Override
			public boolean closeRequested() {
				return app.exit();
			}
		});
		DesktopApp desktopApp = new DesktopApp(app);
		int rc = 0;
		try {
			new Lwjgl3Application(desktopApp, config);
		} catch (Throwable e) {
			rc = 1;
			e.printStackTrace();
			String msg = e.getMessage();
			if (msg == null || msg.isEmpty()) msg = e.getClass().getName();
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			desktopApp.dispose();
		}
		os.close();
		System.exit(rc);
    }
	
	private static ConsumerOutputStream configureLogging() {
		if (System.getProperty("org.slf4j.simpleLogger.defaultLogLevel") == null) {
	        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
	    }
		PrintStream originalOut = System.out;
		PrintStream originalErr = System.err;
		ConsumerOutputStream uiOut = new ConsumerOutputStream(300);
		uiOut.setCumulative(true);
		uiOut.setFilter(line -> !line.startsWith("[main] ERROR io.javalin.Javalin - Failed to start Javalin"));
		PrintStream teeOut = new PrintStream(new MultiOutputStream(originalOut, uiOut), true, StandardCharsets.UTF_8);
		PrintStream teeErr = new PrintStream(new MultiOutputStream(originalErr, uiOut), true, StandardCharsets.UTF_8);
		System.setOut(teeOut);
		System.setErr(teeErr);
		return uiOut;
	}
}