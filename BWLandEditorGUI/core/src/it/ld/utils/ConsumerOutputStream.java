package it.ld.utils;

import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ConsumerOutputStream extends OutputStream {
	private int maxLines;
    private Set<Consumer<String[]>> consumers = new HashSet<>(2);
    private boolean cumulative = false;
	private Filter filter = null;
    
	private final StringBuilder currentLine = new StringBuilder(128);
	private final Deque<String> lines = new ArrayDeque<>();
	
	private final Object monitor = new Object();
	
	public ConsumerOutputStream(int maxLines) {
    	this(maxLines, null);
    }
	
	public ConsumerOutputStream(Consumer<String[]> consumer) {
    	this(1, consumer);
    }
	
    public ConsumerOutputStream(int maxLines, Consumer<String[]> consumer) {
    	this.setMaxLines(maxLines);
    	if (consumer != null) consumers.add(consumer);
    }
    
    public int getMaxLines() {
		return maxLines;
	}

	public void setMaxLines(int maxLines) {
		synchronized (monitor) {
			this.maxLines = maxLines;
			while (lines.size() > maxLines) {
	            lines.removeFirst();
	        }
		}
	}
	
	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	
    public boolean addConsumer(Consumer<String[]> consumer) {
    	boolean r = consumers.add(consumer);
    	flush();
    	return r;
    }
    
    public boolean removeConsumer(Consumer<String[]> consumer) {
    	return consumers.remove(consumer);
    }
    
    public boolean isCumulative() {
		return cumulative;
	}

	public void setCumulative(boolean cumulative) {
		this.cumulative = cumulative;
	}
	
	public void ltrim() {
		synchronized (monitor) {
			while (!lines.isEmpty() && lines.getFirst().isEmpty()) {
				lines.removeFirst();
			}
		}
	}
    
    public void clear() {
    	synchronized (monitor) {
    		currentLine.setLength(0);
    		lines.clear();
    	}
    }
    
    public boolean isEmpty() {
    	synchronized (monitor) {
    		return lines.isEmpty() && currentLine.length() == 0;
    	}
    }
    
    public String[] getLines() {
    	synchronized (monitor) {
	    	String[] tmpLines = new String[lines.size() + (currentLine.length() == 0 ? 0 : 1)];
			lines.toArray(tmpLines);
			if (currentLine.length() != 0) {
				tmpLines[tmpLines.length - 1] = currentLine.toString();
			}
			return tmpLines;
    	}
    }
    
    private void addLine(String line) {
    	if (filter != null) {
    		if (!filter.filter(line)) {
    			return;
    		}
    	}
    	lines.addLast(line);
        while (lines.size() > maxLines) {
            lines.removeFirst();
        }
    }
    
    @Override
    public void write(int b) {
    	synchronized (monitor) {
	        char c = (char) b;
	        currentLine.append(c);
	        if (c == '\n') {
	            String line = currentLine.toString();
	            currentLine.setLength(0);
	            addLine(line);
	        	flush();
	        }
    	}
    }
    
    @Override
    public void flush() {
    	synchronized (monitor) {
	    	if (!consumers.isEmpty() && (!lines.isEmpty() || currentLine.length() > 0)) {
	    		String[] tmpLines = getLines();
				for (Consumer<String[]> consumer : consumers) {
	    			consumer.accept(tmpLines);
	    		}
	    		if (!cumulative) {
	    			lines.clear();
	    			currentLine.setLength(0);
	    		}
	    	}
    	}
    }
    
    
    public interface Filter {
    	public boolean filter(String line);
    }
}
