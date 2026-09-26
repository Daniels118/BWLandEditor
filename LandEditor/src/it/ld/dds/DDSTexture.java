/* Copyright (c) 2024-2026 Daniele Lombardi / Daniels118
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
package it.ld.dds;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteOrder;
import java.util.Arrays;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class DDSTexture extends Struct {
	public final DDSHeader header = new DDSHeader();
	public byte[] bdata = new byte[0];

	private int hash;

	public DDSTexture() {}

	public DDSTexture(BufferedImage image) {
		this.setImage(image);
	}

	@Override
	public void read(EndianDataInputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		header.read(str);
		if (header.hasHeader10()) {
			throw new RuntimeException("DX10 is not suported");
			//Read DDS_HEADER_DXT10
		}
		bdata = new byte[getMipDataSize()];
		str.read(bdata);
		hash = Arrays.hashCode(bdata);
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		str.order(ByteOrder.LITTLE_ENDIAN);
		header.write(str);
		if (header.hasHeader10()) {
			throw new RuntimeException("DX10 is not suported");
			//Write DDS_HEADER_DXT10
		}
		str.write(bdata);
	}

	public int getSize() {
		if (header.hasHeader10()) {
			throw new RuntimeException("DX10 is not suported");
		}
		return DDSHeader.SIZE + bdata.length;
	}

	private boolean isDXT1() {
		return "DXT1".equals(header.ddspf.fourCC);
	}

	private boolean isDXT3() {
		return "DXT3".equals(header.ddspf.fourCC);
	}

	private int getBlockSize() {
		if (isDXT1()) return 8;
		if (isDXT3()) return 16;
		throw new UnsupportedOperationException("Unsupported DDS compression: " + header.ddspf.fourCC);
	}

	public void writeDdsFile(File file) throws Exception {
		try (EndianDataOutputStream str = new EndianDataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));) {
			writeDdsFile(str);
		} catch (Exception e) {
			throw new Exception(e.getMessage() + ", writing " + file.getName(), e);
		}
	}

	public void writeDdsFile(EndianDataOutputStream str) throws Exception {
		str.writeBytes("DDS ");
		write(str);
	}

	public BufferedImage getImage() {
		return getImage(0);
	}

	public BufferedImage getImage(int mipLevel) {
		int width = mipWidth(header.width, mipLevel);
		int height = mipHeight(header.height, mipLevel);
		int[] pixels = getPixels(mipLevel);
		for (int i = 0; i < pixels.length; i++) {
			int abgr = pixels[i];
			int a =  abgr >> 24;
			int b = (abgr >> 16) & 0xFF;
			int g = (abgr >> 8) & 0xFF;
			int r =  abgr & 0xFF;
			pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
		}
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);
		return bufferedImage;
	}

	public int[] getPixels() {
		return getPixels(0);
	}

	/**
	 * @param mipLevel
	 * @return an array of {@code width*height} pixels in packed ABGR format ({@code a<<24 | b<<16 | g<<8 | r}).
	 */
	public int[] getPixels(int mipLevel) {
		if (mipLevel < 0) {
			throw new IllegalArgumentException("mipLevel must be >= 0");
		}

		int mipCount = header.mipMapCount > 0 ? header.mipMapCount : 1;
		if (mipLevel >= mipCount) {
			throw new IllegalArgumentException("Invalid mipLevel " + mipLevel + ", mipMapCount is " + mipCount);
		}

		int width = mipWidth(header.width, mipLevel);
		int height = mipHeight(header.height, mipLevel);
		int blockSize = getBlockSize();
		int offset = getMipOffset(mipLevel, blockSize);

		int blocksX = blockCount(width);
		int blocksY = blockCount(height);
		int levelSize = blocksX * blocksY * blockSize;

		if (offset + levelSize > bdata.length) {
			throw new IllegalStateException("Not enough DDS data for mipLevel " + mipLevel + ": required " + (offset + levelSize) + ", available " + bdata.length);
		}

		int[] pixels = new int[width * height];

		int[] codes = new int[16];
		int[] colorIndices = new int[16];

		for (int blockY = 0; blockY < blocksY; blockY++) {
			for (int blockX = 0; blockX < blocksX; blockX++) {
				int blk = offset + ((blockY * blocksX) + blockX) * blockSize;
				int colorOffset = isDXT1() ? 0 : 8;

				decodeColorBlock(blk + colorOffset, codes, colorIndices);

				int src = 0;
				for (int y = 0; y < 4; y++) {
					int py = blockY * 4 + y;

					for (int x = 0; x < 4; x++) {
						int px = blockX * 4 + x;
						int idx = colorIndices[src];
						int codeOffset = idx * 4;

						int r = codes[codeOffset];
						int g = codes[codeOffset + 1];
						int b = codes[codeOffset + 2];
						int a = codes[codeOffset + 3];

						if (isDXT3()) {
							a = decodeAlphaDxt3(blk, src);
						}

						if (px < width && py < height) {
							pixels[py * width + px] =
								(a << 24) |
								(b << 16) |
								(g << 8) |
								r;
						}

						src++;
					}
				}
			}
		}

		return pixels;
	}

	private void decodeColorBlock(int blk, int[] codes, int[] colorIndices) {
		int c0 = (bdata[blk] & 0xFF) | ((bdata[blk + 1] & 0xFF) << 8);
		int c1 = (bdata[blk + 2] & 0xFF) | ((bdata[blk + 3] & 0xFF) << 8);

		decodeRgb565(c0, codes, 0);
		decodeRgb565(c1, codes, 4);

		codes[3] = 0xFF;
		codes[7] = 0xFF;

		if (isDXT1() && c0 <= c1) {
			codes[8] = (codes[0] + codes[4]) / 2;
			codes[9] = (codes[1] + codes[5]) / 2;
			codes[10] = (codes[2] + codes[6]) / 2;
			codes[11] = 0xFF;

			codes[12] = 0;
			codes[13] = 0;
			codes[14] = 0;
			codes[15] = 0;
		} else {
			codes[8]  = (2 * codes[0] + codes[4]) / 3;
			codes[9]  = (2 * codes[1] + codes[5]) / 3;
			codes[10] = (2 * codes[2] + codes[6]) / 3;
			codes[11] = 0xFF;

			codes[12] = (codes[0] + 2 * codes[4]) / 3;
			codes[13] = (codes[1] + 2 * codes[5]) / 3;
			codes[14] = (codes[2] + 2 * codes[6]) / 3;
			codes[15] = 0xFF;
		}

		for (int src = 4, dst = 0; src < 8; src++) {
			int packed = bdata[blk + src] & 0xFF;

			colorIndices[dst++] = packed & 0b11;
			colorIndices[dst++] = (packed >> 2) & 0b11;
			colorIndices[dst++] = (packed >> 4) & 0b11;
			colorIndices[dst++] = (packed >> 6) & 0b11;
		}
	}

	private static void decodeRgb565(int c, int[] codes, int offset) {
		int r = (c >> 11) & 0x1F;
		int g = (c >> 5) & 0x3F;
		int b = c & 0x1F;

		codes[offset] = (r << 3) | (r >> 2);
		codes[offset + 1] = (g << 2) | (g >> 4);
		codes[offset + 2] = (b << 3) | (b >> 2);
	}

	private int decodeAlphaDxt3(int blk, int src) {
		int q = bdata[blk + src / 2] & 0xFF;
		if ((src & 1) == 0) {
			int lo = q & 0x0F;
			return lo | (lo << 4);
		} else {
			int hi = (q >> 4) & 0x0F;
			return hi | (hi << 4);
		}
	}

	/**
	 * @param width
	 * @param height
	 * @param pixels an array of {@code width*height} pixels in packed ABGR format ({@code a<<24 | b<<16 | g<<8 | r}).
	 */
	public void setPixels(int width, int height, int[] pixels) {
		if (width <= 0) throw new IllegalArgumentException("Image width must be > 0");
		if (height <= 0) throw new IllegalArgumentException("Image height must be > 0");
		if (width * height != pixels.length) throw new IllegalArgumentException("Pixels array doesn't match image size");

		header.width = width;
		header.height = height;

		int blockSize = getBlockSize();
		int blocksX = blockCount(width);
		int blocksY = blockCount(height);
		bdata = new byte[blocksX * blocksY * blockSize];

		Vec3 metric = new Vec3(1);
		int blk = 0;
		int[] rgba = new int[16];	//Block color

		for (int yBase = 0; yBase < header.height; yBase += 4) {
			for (int xBase = 0; xBase < header.width; xBase += 4) {
				copyBlock(pixels, xBase, yBase, rgba);

				ColourSet colors = new ColourSet(rgba);
				RangeFit fit = new RangeFit(colors, metric);

				if (isDXT1()) {
					fit.compress(bdata, blk);
				} else if (isDXT3()) {
					compressAlphaDxt3(rgba, bdata, blk);
					fit.compress(bdata, blk + 8);
				}

				blk += blockSize;
			}
		}

		header.pitchOrLinearSize = bdata.length;
		hash = Arrays.hashCode(bdata);
	}

	public void setImage(BufferedImage img) {
		int[] pixels = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
		setPixels(img.getWidth(), img.getHeight(), pixels);
	}

	private void copyBlock(int[] pixels, final int xBase, final int yBase, int[] rgba) {
		int dst = 0;
		for (int y = 0; y < 4; y++) {
			int py = yBase + y;
			for (int x = 0; x < 4; x++) {
				int px = xBase + x;
				if (px < header.width && py < header.height) {
					rgba[dst++] = pixels[py * header.width + px];
				} else {
					rgba[dst++] = 0;
				}
			}
		}
	}

	private void compressAlphaDxt3(int[] rgba, byte[] bdata, int blk) {
		//Quantise and pack the alpha values pairwise
		for (int i = 0; i < 16; ) {
			//Quantise down to 4 bits
			float alpha1 = (float)((rgba[i++] >> 24) & 0xFF) * (15f / 255f);
			float alpha2 = (float)((rgba[i++] >> 24) & 0xFF) * (15f / 255f);
			int quant1 = Maths.floatToInt(alpha1, 15);
			int quant2 = Maths.floatToInt(alpha2, 15);
			//Pack into the byte
			bdata[blk++] = (byte) (quant1 | (quant2 << 4));
		}
	}

	private int getMipOffset(int mipLevel, int blockSize) {
		int offset = 0;
		for (int i = 0; i < mipLevel; i++) {
			offset += getLevelSize(mipWidth(header.width, i), mipHeight(header.height, i), blockSize);
		}
		return offset;
	}

	private static int getLevelSize(int width, int height, int blockSize) {
		return blockCount(width) * blockCount(height) * blockSize;
	}

	private static int blockCount(int size) {
		return Math.max(1, (size + 3) / 4);
	}

	private static int mipWidth(int baseWidth, int level) {
		return Math.max(1, baseWidth >> level);
	}

	private static int mipHeight(int baseHeight, int level) {
		return Math.max(1, baseHeight >> level);
	}

	private int getMipDataSize() {
		int size = 0;
		int blockSize = getBlockSize();
		int count = Math.max(1, header.mipMapCount);
		for (int i = 0; i < count; i++) {
			size += getLevelSize(mipWidth(header.width, i), mipHeight(header.height, i), blockSize);
		}
		return size;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof DDSTexture)) return false;
		DDSTexture other = (DDSTexture) obj;
		if (this.hash != other.hash) return false;
		return Arrays.equals(this.bdata, other.bdata);
	}
}
