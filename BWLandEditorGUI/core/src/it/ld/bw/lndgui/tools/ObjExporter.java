package it.ld.bw.lndgui.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;

public final class ObjExporter {
	private ObjExporter() {}

    public static void export(Model model, File objFile) throws IOException {
        String baseName = objFile.getName().replace(".obj", "");
        File texturesDir = new File(objFile.getParentFile(), baseName);
        File mtlFile = new File(objFile.getParentFile(), baseName + ".mtl");
        
        StringBuilder obj = new StringBuilder();
        StringBuilder mtl = new StringBuilder();
        
        obj.append("mtllib ").append(mtlFile.getName()).append("\n\n");
        
        Map<Material, Integer> materials = new HashMap<>();
        Map<Texture, Integer> textures = new HashMap<>();
        for (Material material : model.materials) {
        	int materialId = materials.size();
        	mtl.append("newmtl material_").append(materialId).append("\n");
        	if (material.has(ColorAttribute.Diffuse)) {
        		ColorAttribute attr = material.get(ColorAttribute.class, ColorAttribute.Diffuse);
        		mtl.append(String.format("Kd %.3f %.3f %.3f\n", attr.color.r, attr.color.g, attr.color.b));
        		if (attr.color.a < 1f) {
        			mtl.append(String.format("d %.3f\n", attr.color.a));
        		}
        	} else {
        		mtl.append("Kd 1.0 1.0 1.0\n");
        	}
            if (material.has(ColorAttribute.Ambient)) {
            	ColorAttribute attr = material.get(ColorAttribute.class, ColorAttribute.Ambient);
        		mtl.append(String.format("Ka %.3f %.3f %.3f\n", attr.color.r, attr.color.g, attr.color.b));
            } else {
            	mtl.append("Ka 0.0 0.0 0.0\n");
            }
            if (material.has(ColorAttribute.Specular)) {
            	ColorAttribute attr = material.get(ColorAttribute.class, ColorAttribute.Specular);
        		mtl.append(String.format("Ks %.3f %.3f %.3f\n", attr.color.r, attr.color.g, attr.color.b));
            } else {
            	mtl.append("Ks 0.0 0.0 0.0\n");
            }
            
            if (material.has(TextureAttribute.Diffuse)) {
            	if (!texturesDir.isDirectory()) texturesDir.mkdir();
            	TextureAttribute attr = material.get(TextureAttribute.class, TextureAttribute.Diffuse);
            	Texture texture = attr.textureDescription.texture;
            	int textureId = textures.getOrDefault(texture, -1);
            	if (textureId < 0) {
            		textureId = textures.size();
            		String txtName = textureId + "_d.png";
            		File textureFile = new File(texturesDir, txtName);
                	TextureData textureData = texture.getTextureData();
                	if (!textureData.isPrepared()) textureData.prepare();
                	Pixmap pixmap = textureData.consumePixmap();
                	PixmapIO.writePNG(new FileHandle(textureFile), pixmap, 5, false);
                	if (textureData.disposePixmap()) pixmap.dispose();
            		textures.put(texture, textureId);
            	}
            	String txtName = textureId + "_d.png";
            	String txtPath = baseName + "/" + txtName;
            	mtl.append("map_Kd ").append(txtPath).append("\n");
            	if (material.has(BlendingAttribute.Type)) {
            		mtl.append("map_d ").append(txtPath).append("\n");
                }
            }
            mtl.append("\n");
        	materials.put(material, materialId);
        }
        Files.write(mtlFile.toPath(), mtl.toString().getBytes(StandardCharsets.UTF_8));
        
        int vertexOffset = 1;
        Map<Mesh, short[]> meshIndices = new HashMap<>();
        Map<Mesh, Integer> vertexOffsets = new HashMap<>();
        for (int mi = 0; mi < model.meshes.size; mi++) {
            Mesh mesh = model.meshes.get(mi);
            short[] indices = new short[mesh.getNumIndices()];
            mesh.getIndices(indices);
            meshIndices.put(mesh, indices);
            vertexOffsets.put(mesh, vertexOffset);
            
            VertexAttributes attrs = mesh.getVertexAttributes();
            VertexAttribute pos = attrs.findByUsage(VertexAttributes.Usage.Position);
            VertexAttribute nor = attrs.findByUsage(VertexAttributes.Usage.Normal);
            VertexAttribute uv  = attrs.findByUsage(VertexAttributes.Usage.TextureCoordinates);
            
            int stride = attrs.vertexSize / 4;
            float[] verts = new float[mesh.getNumVertices() * stride];
            mesh.getVertices(verts);
            
            for (int i = 0; i < mesh.getNumVertices(); i++) {
                int b = i * stride + pos.offset / 4;
                obj.append("v ")
                   .append(verts[b]).append(' ')
                   .append(verts[b + 1]).append(' ')
                   .append(-verts[b + 2]).append('\n');
            }
            
            if (uv != null) {
                for (int i = 0; i < mesh.getNumVertices(); i++) {
                    int b = i * stride + uv.offset / 4;
                    obj.append("vt ")
                       .append(verts[b]).append(' ')
                       .append(1f - verts[b + 1])	//flip V
                       .append('\n');
                }
            }
            
            if (nor != null) {
                for (int i = 0; i < mesh.getNumVertices(); i++) {
                    int b = i * stride + nor.offset / 4;
                    obj.append("vn ")
                       .append(verts[b]).append(' ')
                       .append(verts[b + 1]).append(' ')
                       .append(-verts[b + 2]).append('\n');
                }
            }
            
            obj.append('\n');
            vertexOffset += mesh.getNumVertices();
        }
        
        String prevNodeId = "";
        int partId = 0;
        for (Node node : model.nodes) {
        	if (!node.id.equals(prevNodeId)) {
        		obj.append("o ").append(node.id).append("\n");
        		prevNodeId = node.id;
        	}
        	for (NodePart part : node.parts) {
        		Material material = part.material;
        		int materialId = materials.get(material);
        		
        		Mesh mesh = part.meshPart.mesh;
        		short[] indices = meshIndices.get(mesh);
        		vertexOffset = vertexOffsets.get(mesh);
        		
        		VertexAttributes attrs = mesh.getVertexAttributes();
                boolean hasNormal = attrs.findByUsage(VertexAttributes.Usage.Normal) != null;
                boolean hasUV = attrs.findByUsage(VertexAttributes.Usage.TextureCoordinates) != null;
                int stride = attrs.vertexSize / 4;
                float[] verts = new float[mesh.getNumVertices() * stride];
                mesh.getVertices(verts);
                
                obj.append("g part_").append(partId).append("\n");
                obj.append("usemtl material_").append(materialId).append("\n");
        		
        		final int startIndex = part.meshPart.offset;
        		final int endIndex = startIndex + part.meshPart.size;
        		for (int i = startIndex; i < endIndex; i+=3) {
        			int a = vertexOffset + (indices[i] & 0xffff);
                    int b = vertexOffset + (indices[i + 1] & 0xffff);
                    int c = vertexOffset + (indices[i + 2] & 0xffff);

                    obj.append("f ")
                       .append(formatFace(a, hasUV, hasNormal)).append(' ')
                       .append(formatFace(b, hasUV, hasNormal)).append(' ')
                       .append(formatFace(c, hasUV, hasNormal)).append('\n');
        		}
        		obj.append("\n");
        		partId++;
        	}
        }
        
        Files.write(objFile.toPath(), obj.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String formatFace(int index, boolean hasUv, boolean hasNormal) {
        if (hasUv && hasNormal) {
            return index + "/" + index + "/" + index;
        }
        if (hasUv) {
            return index + "/" + index;
        }
        if (hasNormal) {
            return index + "//" + index;
        }
        return String.valueOf(index);
    }
}
