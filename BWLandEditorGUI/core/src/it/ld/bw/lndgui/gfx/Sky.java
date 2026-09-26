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
package it.ld.bw.lndgui.gfx;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.Disposable;

import it.ld.bw.lndgui.gfx.SkyShader.SkyAttribute;

public class Sky extends Renderable implements Disposable {
	private final SkyAttribute skyAttribute = new SkyAttribute();
	private final Mesh mesh;
	
	public Sky() {
		mesh = new Mesh(true, 3, 0,
            new VertexAttribute(Usage.Position, 2, "a_pos")
        );
        mesh.setVertices(new float[] {
            -1f, -1f,
             3f, -1f,
            -1f,  3f
        });
        this.meshPart.mesh = mesh;
        this.meshPart.offset = 0;
        this.meshPart.size = 3;
        this.meshPart.primitiveType = GL20.GL_TRIANGLES;
        this.material = new Material(skyAttribute);
	}

	@Override
	public void dispose() {
		mesh.dispose();
	}
}
