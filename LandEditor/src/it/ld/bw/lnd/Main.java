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
package it.ld.bw.lnd;

import java.io.File;

import javax.imageio.ImageIO;

import it.ld.bw.lnd.model.*;
import it.ld.utils.CmdLine;

public class Main {
	public static void main(String[] args) throws Exception {
		CmdLine cmd = new CmdLine(args);
		File inp = mandatory(cmd.getArgFile("-i"), "-i");
		File out = mandatory(cmd.getArgFile("-o"), "-o");
		LndFile lnd = LndFile.load(inp, false);
		lnd.updateLowResTextures();
		/*for (LH3DLandBlock block : lnd.getLandBlocks().values()) {
			for (LH3DLandCell cell : block.getCells()) {
				int a = cell.getAltitude();
				double y = (double)a / 255.0;
				
				y = Math.pow(y, 2);
				
				a = (int)Math.round(y * 255.0);
				a = Math.max(0, Math.min(a, 255));
				cell.setAltitude(a);
			}
		}
		lnd.write(out);*/
		int i = 0;
		for (LNDLowresTexture texture : lnd.getLowresTextures()) {
			//BufferedImage img = texture.getDdsTexture().getImage();
			//DDSTexture newTexture = new DDSTexture(img);
			//newTexture.writeDdsFile(new File(out, i + "_new.dds"));
			//ImageIO.write(newTexture.getImage(), "png", new File(out, i + "_new.png"));
			ImageIO.write(texture.getDdsTexture().getImage(), "png", new File(out, i + ".png"));
			//texture.getDdsTexture().writeDdsFile(new File(out, i + ".dds"));
			i++;
			//break;
		}
		/*for (LNDMaterial mat : lnd.getMaterials()) {
			ImageIO.write(mat.getImage(), "png", new File(out, i + ".png"));
			//LNDMaterial newMat = new LNDMaterial();
			//newMat.setImage(mat.getImage());
			//ImageIO.write(newMat.getImage(), "png", new File(out, i + "_new.png"));
			i++;
			//break;
		}*/
		/*for (LH3DLandBlock block : lnd.getLandBlocks()) {
			System.out.println("TransformUVBefore:");
			float[] uv = block.getTransformUVBefore();
			for (int j = 0; j < uv.length; j += 4) {
				System.out.println(uv[j + 0] + ", " + uv[j + 1] + ", " + uv[j + 2] + ", " + uv[j + 3]);
			}
			System.out.println("TransformUVAfter:");
			uv = block.getTransformUVAfter();
			for (int j = 0; j < uv.length; j += 4) {
				System.out.println(uv[j + 0] + ", " + uv[j + 1] + ", " + uv[j + 2] + ", " + uv[j + 3]);
			}
			System.out.println();
			//break;
		}*/
		//ImageIO.write(lnd.getBumpMap().getImage(), "png", new File(out, "bumpmap.png"));
		//SimpleMap newBump = new SimpleMap(lnd.getBumpMap().getImage());
		//ImageIO.write(newBump.getImage(), "png", new File(out, "bumpmap_new.png"));
		//ImageIO.write(lnd.getNoiseMap().getImage(), "png", new File(out, "noisemap.png"));
		System.out.println("Done.");
	}
	
	private static <T> T mandatory(T value, String name) {
		if (value == null) throw new RuntimeException(name + " is mandatory");
		return value;
	}
}
