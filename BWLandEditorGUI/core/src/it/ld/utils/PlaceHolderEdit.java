package it.ld.utils;

public class PlaceHolderEdit implements Edit {
	private String description;
	
	public PlaceHolderEdit(String description) {
		this.description = description;
	}
	
	@Override
	public void execute() {
		throw new RuntimeException("This should never happen");
	}
	
	@Override
	public String toString() {
		return description;
	}
}
