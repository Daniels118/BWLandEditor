package it.ld.bw.serializer;

public class MapCoords {
	int x;
	int z;
	float altitude;
	
	public float getX() {
		return (float)x / 6553.5f;
	}
	
	public void setX(float x) {
		this.x = (int)(x * 6553.5f);
	}
	
	public float getZ() {
		return (float)z / 6553.5f;
	}
	
	public void setZ(float z) {
		this.z = (int)(z * 6553.5f);
	}
	
	public float getAltitude() {
		return altitude;
	}
	
	public void setAltitude(float altitude) {
		this.altitude = altitude;
	}
	
	public String toJsonString(String indent) {
		return "{\"x\": "+getX()+", \"z\": "+getZ()+", \"altitude\": "+altitude+"}";
	}
	
	@Override
	public String toString() {
		return "("+getX()+", "+getZ()+")";
	}
}
