/* Copyright (c) 2023-2026 Daniele Lombardi / Daniels118
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
package it.ld.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**Base class with helper methods to read and write both fixed and variable length struct.
 * 
 */
public abstract class Struct {
	protected static final Charset ASCII = Charset.forName("windows-1252");
	
	/**Read this struct from a stream.
	 * @param str
	 * @throws Exception
	 */
	public final void read(InputStream str) throws Exception {
		if (str instanceof EndianDataInputStream) {
			read((EndianDataInputStream)str);
		} else {
			try (EndianDataInputStream edis = new EndianDataInputStream(str)) {
				read(edis);
			}
		}
	}
	
	/**Write this struct to a stream.
	 * @param str
	 * @throws Exception
	 */
	public final void write(OutputStream str) throws Exception {
		if (str instanceof EndianDataOutputStream) {
			write((EndianDataOutputStream)str);
		} else {
			try (EndianDataOutputStream edos = new EndianDataOutputStream(str)) {
				write(edos);
			}
		}
	}
	
	/**Read this struct from a stream.
	 * @param str
	 * @throws Exception
	 */
	public abstract void read(EndianDataInputStream str) throws Exception;
	
	/**Write this struct to a stream.
	 * @param str
	 * @throws Exception
	 */
	public abstract void write(EndianDataOutputStream str) throws Exception;
	
	protected static boolean readBool32(EndianDataInputStream str) throws IOException {
		int v = str.readInt();
		if (v == 0) return false;
		if (v == 1) return true;
		throw new IOException("Expected 0 or 1, found " + v);
	}
	
	protected static void writeBool32(EndianDataOutputStream str, boolean val) throws IOException {
		str.writeInt(val ? 1 : 0);
	}
	
	/**Reads a null-terminated ASCII string from a stream.
	 * @param str
	 * @return
	 * @throws IOException
	 */
	protected static String readZString(EndianDataInputStream str) throws IOException {
		byte[] buf = new byte[256];
		int l = 0;
		byte b = str.readByte();
		while (b != 0) {
			if (l >= buf.length) {
				byte[] newBuf = new byte[buf.length * 2];
				System.arraycopy(buf, 0, newBuf, 0, l);
				buf = newBuf;
			}
			buf[l++] = b;
			b = str.readByte();
		}
		String s = new String(buf, 0, l, ASCII);
		return s;
	}
	
	/**Writes a null-terminated ASCII string to a stream.
	 * @param str
	 * @param s
	 * @throws IOException
	 */
	protected static void writeZString(EndianDataOutputStream str, String s) throws IOException {
		byte[] buf = s.getBytes(ASCII);
		str.write(buf);
		str.writeByte(0);
	}
	
	protected static void writeFixedString(EndianDataOutputStream str, String s, int len) throws IOException {
		byte[] buf = new byte[len];
        if (s != null) {
            byte[] src = s.getBytes(ASCII);
            System.arraycopy(src, 0, buf, 0, Math.min(src.length, len));
        }
        str.write(buf);
	}
	
	protected static String readFixedString(EndianDataInputStream str, int len, boolean err) throws IOException {
		byte[] buf = new byte[len];
		int n = str.read(buf);
		if (n != len && err) throw new EOFException();
		if (n <= 0) return "";
		int end = 0;
		while (end < n && buf[end] != 0) {
			end++;
		}
		String s = new String(buf, 0, end, ASCII);
		return s;
	}
	
	/**Reads an array of null-terminated strings from a stream.
	 * @param str
	 * @return
	 * @throws IOException
	 */
	protected static ArrayList<String> readZStringArray(EndianDataInputStream str) throws IOException {
		int count = str.readInt();
		ArrayList<String> res = new ArrayList<String>(count);
		for (int i = 0; i < count; i++) {
			String v = readZString(str);
			res.add(v);
		}
		return res;
	}
	
	/**Writes an array of null-terminated strings to a stream.
	 * @param str
	 * @param strings
	 * @throws IOException
	 */
	protected static void writeZStringArray(EndianDataOutputStream str, List<String> strings) throws IOException {
		str.writeInt(strings.size());
		for (String s : strings) {
			writeZString(str, s);
		}
	}
	
	/**Calculates the size in bytes of an array of null-terminated strings.
	 * @param strings
	 * @return
	 */
	protected static int getZStringArraySize(List<String> strings) {
		int l = 4;
		for (String s : strings) {
			l += s.length() + 1;
		}
		return l;
	}
	
	protected static LinkedHashMap<String, Integer> readMapOfStringInt(EndianDataInputStream str) throws IOException {
		int count = str.readInt();
		LinkedHashMap<String, Integer> res = new LinkedHashMap<String, Integer>(count);
		for (int i = 0; i < count; i++) {
			String key = readZString(str);
			int val = str.readInt();
			res.put(key, val);
		}
		return res;
	}
	
	protected static void writeMapOfStringInt(EndianDataOutputStream str, Map<String, Integer> map) throws IOException {
		str.writeInt(map.size());
		for (Entry<String, Integer> e : map.entrySet()) {
			writeZString(str, e.getKey());
			str.writeInt(e.getValue());
		}
	}
	
	protected static int getMapOfStringIntSize(Map<String, Integer> map) {
		int l = 4;
		for (String s : map.keySet()) {
			l += s.length() + 1 + 4;
		}
		return l;
	}
	
	protected static ArrayList<Integer> readIntArray(EndianDataInputStream str) throws IOException {
		int count = str.readInt();
		ArrayList<Integer> res = new ArrayList<Integer>(count);
		for (int i = 0; i < count; i++) {
			int val = str.readInt();
			res.add(val);
		}
		return res;
	}
	
	protected static void writeIntArray(EndianDataOutputStream str, List<Integer> vals) throws IOException {
		str.writeInt(vals.size());
		for (Integer val : vals) {
			str.writeInt(val);
		}
	}
	
	protected static float[] readFloatArray(EndianDataInputStream str, int count) throws IOException {
        return readFloatArray(str, new float[count]);
    }
	
	protected static float[] readFloatArray(EndianDataInputStream str, float[] v) throws IOException {
        for (int i = 0; i < v.length; i++) {
        	v[i] = str.readFloat();
        }
        return v;
    }
    
	protected static void writeFloatArray(EndianDataOutputStream str, float[] v) throws IOException {
        writeFloatArray(str, v, v.length);
    }
	
    protected static void writeFloatArray(EndianDataOutputStream str, float[] v, int count) throws IOException {
        for (int i = 0; i < count; i++) {
        	str.writeFloat(v != null && i < v.length ? v[i] : 0f);
        }
    }
    
    protected static int[] readIntArray(EndianDataInputStream str, int count) throws IOException {
        return readIntArray(str, new int[count]);
    }
    
    protected static int[] readIntArray(EndianDataInputStream str, int[] v) throws IOException {
    	for (int i = 0; i < v.length; i++) {
        	v[i] = str.readInt();
        }
        return v;
    }
    
    protected static void writeIntArray(EndianDataOutputStream str, int[] v) throws IOException {
    	writeIntArray(str, v, v.length);
    }
    
    protected static void writeIntArray(EndianDataOutputStream str, int[] v, int count) throws IOException {
        for (int i = 0; i < count; i++) {
        	str.writeInt(v != null && i < v.length ? v[i] : 0);
        }
    }
    
    protected static byte[] readByteArray(EndianDataInputStream str, int count) throws IOException {
        byte[] v = new byte[count];
        str.readFully(v);
        return v;
    }
    
    protected static void writeByteArray(EndianDataOutputStream str, byte[] v, int count) throws IOException {
        byte[] out = new byte[count];
        if (v != null) System.arraycopy(v,0,out,0,Math.min(v.length,count));
        str.write(out);
    }
    
    protected static void little(EndianDataInputStream str) {
    	str.order(ByteOrder.LITTLE_ENDIAN);
    }
    
    protected static void little(EndianDataOutputStream str) {
    	str.order(ByteOrder.LITTLE_ENDIAN);
    }
}
