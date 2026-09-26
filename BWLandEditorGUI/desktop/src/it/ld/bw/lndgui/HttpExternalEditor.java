package it.ld.bw.lndgui;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import it.ld.bw.lndgui.gfx.Coord;
import it.ld.bw.lndgui.interfaces.ExternalEditor;

public class HttpExternalEditor implements ExternalEditor {
	private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
	
	private String getBaseUrl() {
		return "http://127.0.0.1:" + Settings.EXT_EDITOR_PORT.getInt();
	}
	
	@Override
	public void insertCoord(Coord coord) {
		String data = coord.toString();
		
		HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/insert_coord"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();

        client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
        .exceptionally(e -> {
        	//e.printStackTrace();
        	return null;
        });
	}

}
