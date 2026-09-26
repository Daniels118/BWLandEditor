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
package it.ld.libgdx.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ShaderLoader extends AsynchronousAssetLoader<ShaderProgram, ShaderLoader.ShaderParameter> {
    private String vertProgram;
	private String fragProgram;
	
	public ShaderLoader(FileHandleResolver resolver) {
		super(resolver);
	}
	
	@SuppressWarnings("rawtypes")
	public Array<AssetDescriptor> getDependencies(String fileName, ShaderParameter parameter) {
		return null;
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file, ShaderParameter parameter) {
		FileHandle shaderFile = Gdx.files.internal(fileName);
		if (shaderFile.exists()) {
			String shader = shaderFile.readString();
			int p1 = shader.indexOf("[VS]\r\n");
			if (p1 < 0) throw new GdxRuntimeException("Missing [VS] section in shader file " + fileName);
			int p2 = shader.indexOf("[FS]\r\n");
			if (p2 < 0) throw new GdxRuntimeException("Missing [FS] section in shader file " + fileName);
			vertProgram = shader.substring(p1 + 6, p2);
			fragProgram = shader.substring(p2 + 6);
		} else {
			FileHandle vertFile = Gdx.files.internal(fileName + ".vert");
			FileHandle fragFile = Gdx.files.internal(fileName + ".frag");
			if (vertFile.exists() && fragFile.exists()) {
				vertProgram = vertFile.readString();
		        fragProgram = fragFile.readString();
		    } else {
		    	throw new GdxRuntimeException("Cannot find shader file " + fileName);
		    }
		}
	}

	@Override
	public ShaderProgram loadSync(AssetManager manager, String fileName, FileHandle file, ShaderParameter parameter) {
		ShaderProgram.pedantic = false;
        return new ShaderProgram(vertProgram, fragProgram);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, ShaderParameter parameter) {
		return null;
	}
	
	static public class ShaderParameter extends AssetLoaderParameters<ShaderProgram> {}
}