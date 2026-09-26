package it.ld.bw.lndgui.interfaces;

public interface ClipboardListener {
	public enum DataType {LAND, OBJECTS}
	
	public void contentsChanged(DataType type, Object data);
}
