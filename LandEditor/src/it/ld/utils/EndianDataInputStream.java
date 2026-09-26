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

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EndianDataInputStream extends InputStream implements DataInput {
    private InputStream stream;
	private DataInputStream dataIn;
    private ByteBuffer buffer = ByteBuffer.allocate(8);
    protected final byte[] raw = new byte[8];
    
    private int pos = 0;
    
    public EndianDataInputStream(InputStream stream) {
        this.stream = stream;
    	dataIn = new DataInputStream(stream);
    }
    
    public EndianDataInputStream order(ByteOrder o) {
        buffer.order(o);
        return this;
    }
    
    public InputStream getInputStream() {
    	return stream;
    }
    
    public byte[] getRawData() {
    	return raw;
    }
    
    public int getPosition() {
    	return pos;
    }
    
    private int incPos(int step) {
    	pos += step;
    	return step;
    }
    
    @Override
    public int available() throws IOException {
    	return dataIn.available();
    }
    
    @Override
    public int read(byte[] b) throws IOException {
        return incPos(dataIn.read(b));
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return incPos(dataIn.read(b, off, len));
    }
    
    @Deprecated
    @Override
    public String readLine() throws IOException {
        return dataIn.readLine();
    }
    
    @Override
    public boolean readBoolean() throws IOException {
    	incPos(1);
    	int n = Compat.readNBytes(dataIn, raw, 0, 1);
    	if (n < 1) throw new EOFException();
    	return raw[0] != 0;
    }
    
    @Override
    public byte readByte() throws IOException {
    	incPos(1);
    	int n = Compat.readNBytes(dataIn, raw, 0, 1);
    	if (n < 1) throw new EOFException();
    	return raw[0];
    }
    
    @Override
    public int read() throws IOException {
        incPos(1);
        int n = Compat.readNBytes(dataIn, raw, 0, 1);
    	if (n < 1) return -1;
    	return raw[0] & 0xFF;
    }
    
    @Override
    public boolean markSupported(){
        return dataIn.markSupported();
    }
    
    @Override
    public void mark(int readlimit) {
        dataIn.mark(readlimit);
    }
    
    @Override
    public void reset() throws IOException {
        dataIn.reset();
    }
    
    @Override
    public char readChar() throws IOException {
    	incPos(2);
    	int n = Compat.readNBytes(dataIn, raw, 0, 2);
    	if (n < 2) throw new EOFException();
    	buffer.put(raw, 0, 2);
    	buffer.flip();
        return buffer.getChar();
    }
    
    @Override
    public void readFully(byte[] b) throws IOException {
    	incPos(b.length);
    	dataIn.readFully(b);
    }
    
    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
    	incPos(len);
    	dataIn.readFully(b, off, len);
    }
    
    @Override
    public String readUTF() throws IOException {
        return dataIn.readUTF();
    }
    
    @Override
    public int skipBytes(int n) throws IOException {
        return incPos(dataIn.skipBytes(n));
    }
    
    @Override
    public double readDouble() throws IOException {
    	incPos(8);
    	buffer.clear();
    	int n = Compat.readNBytes(dataIn, raw, 0, 8);
    	if (n < 8) throw new EOFException();
    	buffer.put(raw, 0, 8);
    	buffer.flip();
        return buffer.getDouble();
    }

    @Override
    public float readFloat() throws IOException {
    	incPos(4);
    	buffer.clear();
    	int n = Compat.readNBytes(dataIn, raw, 0, 4);
    	if (n < 4) throw new EOFException();
    	buffer.put(raw, 0, 4);
    	buffer.flip();
        return buffer.getFloat();
    }

    @Override
    public int readInt() throws IOException {
    	incPos(4);
    	buffer.clear();
    	int n = Compat.readNBytes(dataIn, raw, 0, 4);
    	if (n < 4) throw new EOFException();
    	buffer.put(raw, 0, 4);
    	buffer.flip();
        return buffer.getInt();
    }

    @Override
    public long readLong() throws IOException {
    	incPos(8);
    	buffer.clear();
    	int n = Compat.readNBytes(dataIn, raw, 0, 8);
    	if (n < 8) throw new EOFException();
    	buffer.put(raw, 0, 8);
    	buffer.flip();
        return buffer.getLong();
    }

    @Override
    public short readShort() throws IOException {
    	incPos(2);
    	buffer.clear();
    	int n = Compat.readNBytes(dataIn, raw, 0, 2);
    	if (n < 2) throw new EOFException();
    	buffer.put(raw, 0, 2);
    	buffer.flip();
        return buffer.getShort();
    }

    @Override
    public int readUnsignedByte() throws IOException {
    	incPos(1);
    	int n = Compat.readNBytes(dataIn, raw, 0, 1);
    	if (n < 1) throw new EOFException();
        return raw[0] & 0xFF;
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return readShort() & 0xFFFF;
    }
}
