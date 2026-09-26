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
package it.ld.bw.lndgui;

import java.net.BindException;
import java.util.Map;

import com.badlogic.gdx.Gdx;

import io.javalin.Javalin;
import it.ld.bw.lndgui.gfx.Coord;

public class DesktopApiServer {
	private static final int MAX_PORTS = 5;
	
	private Javalin server;

    public void start(MainApp app, int port) {
        for (int attempt = 0; attempt < MAX_PORTS; attempt++) {
    		try {
    			server = createServer(app);
    			server.start(port);
    			//System.out.println("Listening on port " + port);
    			break;
    		} catch (Exception e) {
    			if (e.getCause() != null && e.getCause().getCause() instanceof BindException) {
    				if (attempt < MAX_PORTS - 1) {
    					server.stop();
    					port++;
    					continue;
    				}
    				System.err.println("Can't find an available port");
    				break;
    			}
    			throw e;
    		}
		}
    }
    
    private Javalin createServer(MainApp app) {
    	Javalin server = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
            // config.plugins.enableCors(cors -> cors.add(it -> it.anyHost()));
        });

        server.post("/show_tmp_marker", ctx -> {
        	Coord coord = ctx.bodyAsClass(Coord.class);
        	//System.out.println("/show_tmp_marker " + coord);
        	Gdx.app.postRunnable(() -> {
        		app.getView3D().showTmpMarker(coord);
        	});
        });
        
        server.post("/hide_tmp_marker", ctx -> {
        	Gdx.app.postRunnable(() -> {
        		app.getView3D().hideTmpMarker();
        	});
        });
        
        server.exception(Exception.class, (e, ctx) -> {
            ctx.status(400).json(Map.of(
                "error", "invalid json",
                "details", e.getMessage()
            ));
        });
        
        return server;
    }

    public void stop() {
        if (server != null) server.stop();
    }
}
