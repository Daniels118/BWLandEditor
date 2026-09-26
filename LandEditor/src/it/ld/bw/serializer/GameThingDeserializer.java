package it.ld.bw.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;

import it.ld.utils.EndianDataInputStream;

public class GameThingDeserializer extends EndianDataInputStream {
	/**The checksum is update everytime a value is read from the input stream.
	 * A value can be of any size in bytes.
	 * The checksum is computed summing up the first raw byte of the value read, plus the size of the value in bytes.
	 */
	private int checksum;
	/**The cache is used to reference previously defined objects.
	 */
	private ArrayList<GameThing> cache = new ArrayList<>(256);
	
	public GameThingDeserializer(InputStream stream) {
		super(stream);
		this.order(ByteOrder.LITTLE_ENDIAN);
	}
	
	public void readChecksum() throws IOException {
		int expectedSum = checksum;
		int readSum = readInt();
		if (expectedSum != readSum) {
			throw new IOException(String.format("Checksum mismatch: expected 0x%X, found 0x%X", expectedSum, readSum));
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends GameThing> T deserializeOne(Class<T> clazz, GameThingType type) throws Exception {
		int index = readInt();
		if (index == 0) {
			return null;
		}
		if (index == cache.size() + 1) {
			int readType = readInt();
			if (readType != type.ordinal()) {
				throw new Exception("Type mismatch while parsing "+type+": expected "+type.ordinal()+" but "+readType+" found");
			}
			@SuppressWarnings("unused")
			int playerId = readInt();
			readChecksum();
			T thing = clazz.getDeclaredConstructor().newInstance();
			cache.add(thing);
			thing.deserialize(this);
			return thing;
		}
		if (index < 0 || index > cache.size()) {
			throw new Exception("Invalid index: " + index);
		}
		GameThing thing = cache.get(index - 1);
		return (T) thing;
	}
	
	public <T extends GameThing> ArrayList<T> deserializeList(Class<T> clazz, GameThingType type) throws Exception {
		int count = readInt();
		ArrayList<T> res = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			T item = deserializeOne(clazz, type);
			if (item != null) {
				res.add(item);
			}
		}
		return res;
	}
	
	public MapCoords readCoords() throws IOException {
		MapCoords r = new MapCoords();
		r.x = super.readInt();
		checksum += (raw[0] & 0xFF) + 12;
		r.z = super.readInt();
		r.altitude = super.readFloat();
		return r;
	}
	
	@Override
	public int readInt() throws IOException {
		int r = super.readInt();
		checksum += (raw[0] & 0xFF) + 4;
		return r;
	}
	
	@Override
	public float readFloat() throws IOException {
		float r = super.readFloat();
		checksum += (raw[0] & 0xFF) + 4;
		return r;
	}
	
	@Override
	public byte readByte() throws IOException {
		byte r = super.readByte();
		checksum += (raw[0] & 0xFF) + 1;
		return r;
	}
	
	public int getChecksum() {
		return checksum;
	}
}
