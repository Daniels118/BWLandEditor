package it.ld.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MultiOutputStream extends OutputStream {
    private final List<OutputStream> outputs = new ArrayList<>(2);

    public MultiOutputStream(OutputStream... outputs) {
    	for (OutputStream output : outputs) {
    		this.outputs.add(output);
    	}
    }
    
    public void add(OutputStream output) {
    	this.outputs.add(output);
    }

    @Override
    public void write(int b) throws IOException {
        for (OutputStream out : outputs) {
            out.write(b);
        }
    }

    @Override
    public void flush() throws IOException {
        for (OutputStream out : outputs) {
            out.flush();
        }
    }

    @Override
    public void close() throws IOException {
        for (OutputStream out : outputs) {
            out.close();
        }
    }
}
