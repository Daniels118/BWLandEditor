package it.ld.bw.lndgui.gfx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Pool;

import it.ld.bw.info.GSpellSeedInfo;
import it.ld.bw.info.GVillagerInfo;
import it.ld.bw.l3d.L3DVec3;
import it.ld.bw.lhx.Command;
import it.ld.bw.lhx.Command.ArgType;
import it.ld.bw.lhx.Command.Argument;
import it.ld.bw.lhx.LHXCoord;
import it.ld.bw.lhx.Statement;
import it.ld.bw.lndgui.gfx.L3DModelManager.ModelInfo;
import it.ld.bw.lndgui.gfx.MorphableShader.MorphableAttribute;
import it.ld.utils.Listeners;
import it.ld.utils.UChangeListener;
import it.ld.utils.UChangeListener.EventType;

public class Object3D implements RenderableProvider, AutoCloseable {
	public enum Property {STATEMENT, NAME, POSITION, ID, PARENT, CHILDREN, SELECTED, SYNTAX_ERROR, DUPLICATE_ID, OUT_OF_BOUNDS}
	public final Listeners listeners = new Listeners(this);
	
	private static final ColorAttribute HIGHLIGHT_ATTR = ColorAttribute.createEmissive(new Color(0f, 0.5f, 0.5f, 1f));
	private static final ColorAttribute ERROR_ATTR = ColorAttribute.createEmissive(new Color(0.5f, 0f, 0f, 1f));
	private static final ColorAttribute HL_ERR_ATTR = ColorAttribute.createEmissive(new Color(0.5f, 0f, 0.5f, 1f));
	
	private LHX3D lhx3d;
	private Land3D land3d;
	private Statement statement;
	private ModelInfo modelInfo;
	private ModelInfo youngModelInfo;
	private ModelInstance instance;
	public final BoundingBox boundingBox = new BoundingBox();
	
	private boolean selected = false;
	private boolean syntaxError = false;
	private boolean dupIdError = false;
	private boolean outOfBoundsError = false;
	
	private Array<ColorAttribute> originalEmissive;
	
	private Object3D parent;
	private List<Object3D> children;
	
	public Object3D() {}
	
	public Object3D(ModelInfo modelInfo, int status) {
		set(null, null, null, modelInfo, null, status);
	}
	
	public Object3D(Land3D land3d, ModelInfo modelInfo, int status) {
		set(null, land3d, null, modelInfo, null, status);
	}
	
	public Object3D set(@Null LHX3D lhx3d, @Null Statement statement, ModelInfo modelInfo, int status) {
		set(lhx3d, lhx3d != null ? lhx3d.land3d : null, statement, modelInfo, null, status);
		return this;
	}
	
	public Object3D set(@Null LHX3D lhx3d, Land3D land3d, @Null Statement statement, ModelInfo modelInfo, ModelInfo youngModelInfo, int status) {
		this.lhx3d = lhx3d;
		this.land3d = land3d;
		this.statement = statement;
		this.modelInfo = modelInfo;
		this.youngModelInfo = youngModelInfo;
		createModelInstance();
		setEnabledStates(status, status >= 0 ? status : Integer.MAX_VALUE);
		if (statement != null) {
			statement.listeners.add(statementChangeListener);
		}
		return this;
	}
	
	public LHX3D getLHX3D() {
		return lhx3d;
	}
	
	public Land3D getLandD() {
		return land3d;
	}
	
	public Statement getStatement() {
		return statement;
	}
	
	public ModelInfo getModelInfo() {
		return modelInfo;
	}
	
	public ModelInfo getYoungModelInfo() {
		return youngModelInfo;
	}
	
	public boolean isSprite() {
		return modelInfo != null && modelInfo.isSprite();
	}
	
	public ModelInstance getModelInstance() {
		return instance;
	}
	
	public boolean isVirtual() {
		return modelInfo != null && modelInfo.isVirtual();
	}
	
	public boolean isTown() {
		return statement != null && statement.isTown();
	}
	
	public boolean isFlock() {
		return statement != null && statement.isFlock();
	}
	
	public boolean isForest() {
		return statement != null && statement.isForest();
	}
	
	public boolean isStream() {
		return statement != null && statement.isStream();
	}
	
	public boolean isTownCentre() {
		return statement != null && statement.isTownCentre();
	}
	
	public boolean hasParent() {
		return statement != null && (statement.hasParent() || statement.getCommand() == Command.CREATE_VILLAGER_POS);
	}
	
	public boolean isChildOfTown() {
		return statement != null && statement.isChildOfTown();
	}
	
	public boolean isChildOfFlock() {
		return statement != null && statement.isChildOfFlock();
	}
	
	public boolean isChildOfForest() {
		return statement != null && statement.isChildOfForest();
	}
	
	public boolean isChildOfStream() {
		return statement != null && statement.isChildOfStream();
	}
	
	public Object3D getParent() {
		return parent;
	}
	
	void setParent(Object3D parent) {
		if (parent != this.parent) {
			Object3D oldParent = this.parent;
			if (oldParent != null) {
				oldParent.children.remove(this);
				oldParent.listeners.notify(EventType.REMOVE, Property.CHILDREN, this, null);
			}
			this.parent = parent;
			if (parent != null) {
				if (parent.children == null) {
					parent.children = new ArrayList<>();
				}
				parent.children.add(this);
				parent.listeners.notify(EventType.ADD, Property.CHILDREN, null, this);
			}
			listeners.notify(EventType.CHANGE, Property.PARENT, oldParent, parent);
			//Objects without an explicit position could be positioned relative to their parent
			if (statement != null && statement.isCommand() && statement.getCommand().position < 0) {
				updatePosition(false);
			}
			if (isTownCentre() && parent != null && parent.isTown()) {
				updateTownSpells(parent);
			}
			if (statement != null && statement.getCommand() == Command.CREATE_NEW_TOWN_SPELL) {
				if (oldParent != null && oldParent.isTown()) {
					updateTownSpells(oldParent);
				}
			}
		}
	}
	
	public List<Object3D> getChildren() {
		return children == null ? Collections.emptyList() : new ArrayList<>(children);
	}
	
	public List<Object3D> getDescendants() {
		ArrayList<Object3D> descendants = new ArrayList<>(1000);
		getDescendants(descendants);
		descendants.trimToSize();
		return descendants;
	}
	
	public void getDescendants(List<Object3D> out) {
		if (children != null) {
			for (Object3D child : children) {
				out.add(child);
				child.getDescendants(out);
			}
		}
	}
	
	public boolean isAncestorOf(Object3D descendant) {
		return descendant.isDescendantOf(this);
	}
	
	public boolean isDescendantOf(Object3D ancestor) {
		Object3D obj = this.getParent();
		while (obj != null) {
			if (obj == ancestor) return true;
			obj = obj.getParent();
		}
		return false;
	}
	
	public int getId() {
		return statement.getId();
	}
	
	public boolean isAdult() {
		if (statement != null) {
			Command cmd = statement.getCommand();
			if (cmd == Command.CREATE_VILLAGER_POS || cmd == Command.CREATE_TOWN_VILLAGER) {
				GVillagerInfo info = (GVillagerInfo)modelInfo.info;
				return this.getAge() >= info.grownUpAge;
			}
		}
		return true;
	}
	
	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		instance.getRenderables(renderables, pool);
	}
	
	public boolean isMorphable() {
		for (Material material : instance.materials) {
			if (material.has(MorphableAttribute.MORPHABLE)) {
				return true;
			}
		}
		return false;
	}
	
	/**Set the model backed by this instance as morphable. This will affect all instances of the same model.
	 * @param morphable
	 */
	public void setMorphable(boolean morphable) {
		if (morphable != this.isMorphable()) {
			if (morphable) {
				for (Material material : instance.materials) {
					if (!material.has(MorphableAttribute.MORPHABLE)) {
						MorphableAttribute attr = new MorphableAttribute();
						attr.setLand(land3d);
						material.set(attr);
					}
				}
			} else {
				for (Material material : instance.materials) {
					if (material.has(MorphableAttribute.MORPHABLE)) {
						MorphableAttribute attr = material.get(MorphableAttribute.class, MorphableAttribute.MORPHABLE);
						attr.setLand(null);
						material.remove(MorphableAttribute.MORPHABLE);
					}
				}
			}
		}
	}
	
	private void createModelInstance() {
		if (instance != null) {
			for (Material material : instance.materials) {
				if (material.has(MorphableAttribute.MORPHABLE)) {
					MorphableAttribute attr = material.get(MorphableAttribute.class, MorphableAttribute.MORPHABLE);
					attr.setLand(null);
				}
			}
		}
		
		Model model = modelInfo.model;
		if (youngModelInfo != null && modelInfo.info instanceof GVillagerInfo) {
			int newAge = statement.getAge();
			int grownUpAge = ((GVillagerInfo)modelInfo.info).grownUpAge;
			if (newAge < grownUpAge) {
				model = youngModelInfo.model;
			}
		}
		
		this.instance = new ModelInstance(model);
		this.originalEmissive = new Array<>(instance.materials.size);
		for (Material material : instance.materials) {
			if (material.has(MorphableAttribute.MORPHABLE)) {
				MorphableAttribute attr = material.get(MorphableAttribute.class, MorphableAttribute.MORPHABLE);
				attr.setLand(land3d);
			}
			originalEmissive.add(material.has(ColorAttribute.Emissive) ? (ColorAttribute)material.get(ColorAttribute.Emissive) : null);
		}
		if (selected || isAnyError()) {
			updateMaterials();
		}
		updatePosition(true);
		instance.calculateBoundingBox(boundingBox);
	}
	
	public int getNumStates() {
		int c = 0;
		for (Node node : instance.nodes) {
			if (node.id.startsWith("status_")) {
				c++;
			}
		}
		return c;
	}
	
	public void enableStates(int statusMin, int statusMax) {
		for (Node node : instance.nodes) {
			int status = -1;
			if (node.id.startsWith("status_")) {
				status = Integer.parseInt(node.id.substring(7));
			}
			if (status >= statusMin && status <= statusMax) {
				for (NodePart part : node.parts) {
					part.enabled = true;
				}
			}
		}
	}
	
	public void disableStates(int statusMin, int statusMax) {
		for (Node node : instance.nodes) {
			int status = -1;
			if (node.id.startsWith("status_")) {
				status = Integer.parseInt(node.id.substring(7));
			}
			if (status >= statusMin && status <= statusMax) {
				for (NodePart part : node.parts) {
					part.enabled = false;
				}
			}
		}
	}
	
	public void setEnabledStates(int statusMin, int statusMax) {
		for (Node node : instance.nodes) {
			int status = -1;
			if (node.id.startsWith("status_")) {
				status = Integer.parseInt(node.id.substring(7));
			}
			boolean enabled = status >= statusMin && status <= statusMax;
			//TODO if ("footprint".equals(node.id)) enabled = true;
			for (NodePart part : node.parts) {
				part.enabled = enabled;
			}
		}
	}
	
	public boolean getState(int state) {
		for (Node node : instance.nodes) {
			if (node.id.startsWith("status_")) {
				int status = Integer.parseInt(node.id.substring(7));
				if (status == state) {
					for (NodePart part : node.parts) {
						return part.enabled;
					}
				}
			}
		}
		return false;
	}
	
	public void setState(int state, boolean enabled) {
		for (Node node : instance.nodes) {
			int status = -1;
			if (node.id.startsWith("status_")) {
				status = Integer.parseInt(node.id.substring(7));
			}
			if (status == state) {
				for (NodePart part : node.parts) {
					part.enabled = enabled;
				}
				break;
			}
		}
	}
	
	public void getWorldBoundingBox(BoundingBox out) {
		out.set(boundingBox).mul(instance.transform);
		if (isMorphable()) {
			out.min.y = 0;
			out.update();
		}
	}
	
	private static final BoundingBox tmpBB = new BoundingBox();
	
	public boolean intersect(Ray ray) {
		return intersect(ray, new Vector3());
	}
	
	public boolean intersect(Ray ray, Vector3 intersection) {
		if (isMorphable()) {
			return intersectMorphable(ray, intersection);
		} else {
			return intersectSolid(ray, intersection);
		}
	}
	
	public boolean intersectFast(Ray ray, Vector3 intersection) {
		getWorldBoundingBox(tmpBB);
		return Intersector.intersectRayBounds(ray, tmpBB, intersection);
	}
	
	public boolean intersectMorphable(Ray ray, Vector3 intersection) {
	    if (!intersectFast(ray, intersection)) return false;
	    instance.calculateTransforms();
	    instance.transform.getTranslation(tmpVec);
	    final float baseY = tmpVec.y;
	    Vector3 v0 = new Vector3();
	    Vector3 v1 = new Vector3();
	    Vector3 v2 = new Vector3();
	    Vector3 tmpHit = new Vector3();
	    Vector3 closestHit = new Vector3();
	    Matrix4 nodeToWorld = new Matrix4();
	    float closestDist2 = Float.MAX_VALUE;
	    boolean hit = false;
	    for (Node node : instance.nodes) {
	    	for (NodePart nodePart : node.parts) {
	    		if (!nodePart.enabled) continue;
		        MeshPart meshPart = nodePart.meshPart;
		        if (meshPart.primitiveType != GL20.GL_TRIANGLES) continue;
		        Mesh mesh = meshPart.mesh;
		        VertexAttribute posAttr = mesh.getVertexAttribute(VertexAttributes.Usage.Position);
		        if (posAttr == null) continue;
		        int vertexSize = mesh.getVertexSize() / 4;
		        int posOffset = posAttr.offset / 4;
		        int numVertices = mesh.getNumVertices();
		        float[] vertices = new float[numVertices * vertexSize];
		        mesh.getVertices(vertices);
		        int numIndices = mesh.getNumIndices();
		        nodeToWorld.set(instance.transform).mul(node.globalTransform);
		        if (numIndices > 0) {
		            short[] indices = new short[numIndices];
		            mesh.getIndices(indices);
		            int start = meshPart.offset;
		            int end = meshPart.offset + meshPart.size;
		            for (int i = start; i < end; i += 3) {
		                int i0 = indices[i] & 0xFFFF;
		                int i1 = indices[i + 1] & 0xFFFF;
		                int i2 = indices[i + 2] & 0xFFFF;
		                readVertex(vertices, vertexSize, posOffset, i0, v0);
		                readVertex(vertices, vertexSize, posOffset, i1, v1);
		                readVertex(vertices, vertexSize, posOffset, i2, v2);
		                v0.mul(nodeToWorld);
		                v1.mul(nodeToWorld);
		                v2.mul(nodeToWorld);
		                
		                float h0 = land3d.land.getHeight(v0.x, -v0.z);
		                float h1 = land3d.land.getHeight(v1.x, -v1.z);
		                float h2 = land3d.land.getHeight(v2.x, -v2.z);
		                v0.y += h0 - baseY;
		                v1.y += h1 - baseY;
		                v2.y += h2 - baseY;
	
		                if (Intersector.intersectRayTriangle(ray, v0, v1, v2, tmpHit)) {
		                    float dist2 = ray.origin.dst2(tmpHit);
		                    if (dist2 < closestDist2) {
		                        closestDist2 = dist2;
		                        closestHit.set(tmpHit);
		                        hit = true;
		                    }
		                }
		            }
		        } else {
		            int start = meshPart.offset;
		            int end = meshPart.offset + meshPart.size;
		            for (int i = start; i < end; i += 3) {
		                readVertex(vertices, vertexSize, posOffset, i, v0);
		                readVertex(vertices, vertexSize, posOffset, i + 1, v1);
		                readVertex(vertices, vertexSize, posOffset, i + 2, v2);
		                v0.mul(nodeToWorld);
		                v1.mul(nodeToWorld);
		                v2.mul(nodeToWorld);
		                
		                float h0 = land3d.land.getHeight(v0.x, -v0.z);
		                float h1 = land3d.land.getHeight(v1.x, -v1.z);
		                float h2 = land3d.land.getHeight(v2.x, -v2.z);
		                v0.y += h0 - baseY;
		                v1.y += h1 - baseY;
		                v2.y += h2 - baseY;
	
		                if (Intersector.intersectRayTriangle(ray, v0, v1, v2, tmpHit)) {
		                    float dist2 = ray.origin.dst2(tmpHit);
		                    if (dist2 < closestDist2) {
		                        closestDist2 = dist2;
		                        closestHit.set(tmpHit);
		                        hit = true;
		                    }
		                }
		            }
		        }
		    }
	    }
	    if (hit) {
	        intersection.set(closestHit);
	    }
	    return hit;
	}
	
	private boolean intersectSolid(Ray ray, Vector3 intersection) {
		if (!intersectFast(ray, intersection)) return false;
		instance.calculateTransforms();
		//
		Matrix4 invInstance = new Matrix4(instance.transform).inv();
	    Ray localRay = new Ray(ray.origin.cpy().mul(invInstance), ray.direction.cpy().rot(invInstance).nor());
	    Ray nodeRay = new Ray();
	    Vector3 v0 = new Vector3();
		Vector3 v1 = new Vector3();
		Vector3 v2 = new Vector3();
		Vector3 tmpHitNode = new Vector3();
	    Vector3 tmpHitWorld = new Vector3();
	    Vector3 closestHitWorld = new Vector3();
	    boolean hit = false;
	    for (Node node : instance.nodes) {
		    Matrix4 invNode = new Matrix4(node.globalTransform).inv();
		    nodeRay.origin.set(localRay.origin).mul(invNode);
		    nodeRay.direction.set(localRay.direction).rot(invNode).nor();
			//
		    float closestDist2 = Float.MAX_VALUE;
			for (NodePart nodePart : node.parts) {
				if (!nodePart.enabled) continue;
				MeshPart meshPart = nodePart.meshPart;
		        if (meshPart.primitiveType != GL20.GL_TRIANGLES) continue;
			    Mesh mesh = meshPart.mesh;
			    VertexAttribute posAttr = mesh.getVertexAttribute(VertexAttributes.Usage.Position);
			    if (posAttr == null) continue;
			    int vertexSize = mesh.getVertexSize() / 4;
		        int posOffset = posAttr.offset / 4;
		        int numVertices = mesh.getNumVertices();
		        float[] vertices = new float[numVertices * vertexSize];
		        mesh.getVertices(vertices);
		        int numIndices = mesh.getNumIndices();
		        
		        if (numIndices > 0) {
		            short[] indices = new short[numIndices];
		            mesh.getIndices(indices);
	
		            int start = meshPart.offset;
		            int end = meshPart.offset + meshPart.size;
	
		            for (int i = start; i < end; i += 3) {
		                int i0 = indices[i] & 0xFFFF;
		                int i1 = indices[i + 1] & 0xFFFF;
		                int i2 = indices[i + 2] & 0xFFFF;
	
		                readVertex(vertices, vertexSize, posOffset, i0, v0);
		                readVertex(vertices, vertexSize, posOffset, i1, v1);
		                readVertex(vertices, vertexSize, posOffset, i2, v2);
	
		                if (Intersector.intersectRayTriangle(nodeRay, v0, v1, v2, tmpHitNode)) {
		                	tmpHitWorld.set(tmpHitNode)
		                		.mul(node.globalTransform)
		                		.mul(instance.transform);
			                float dist2 = ray.origin.dst2(tmpHitWorld);
			                if (dist2 < closestDist2) {
			                    closestDist2 = dist2;
			                    closestHitWorld.set(tmpHitWorld);
			                    hit = true;
			                }
		                }
		            }
		        } else {
		            int start = meshPart.offset;
		            int end = meshPart.offset + meshPart.size;
		            for (int i = start; i < end; i += 3) {
		                readVertex(vertices, vertexSize, posOffset, i, v0);
		                readVertex(vertices, vertexSize, posOffset, i + 1, v1);
		                readVertex(vertices, vertexSize, posOffset, i + 2, v2);
		                if (Intersector.intersectRayTriangle(nodeRay, v0, v1, v2, tmpHitNode)) {
		                    tmpHitWorld.set(tmpHitNode)
		                        .mul(node.globalTransform)
		                        .mul(instance.transform);
		                    float dist2 = ray.origin.dst2(tmpHitWorld);
		                    if (dist2 < closestDist2) {
		                        closestDist2 = dist2;
		                        closestHitWorld.set(tmpHitWorld);
		                        hit = true;
		                    }
		                }
		            }
		        }
		    }
	    }
		if (hit) {
			intersection.set(closestHitWorld);
		}
		return hit;
	}
	
	private static void readVertex(float[] vertices, int vertexSize, int posOffset, int index, Vector3 out) {
	    int base = index * vertexSize + posOffset;
	    out.set(vertices[base], vertices[base + 1], vertices[base + 2]);
	}
	
	private final UChangeListener statementChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == Statement.Property.ARGS) {
				Command command = statement.getCommand();
				int argIndex = event.getIndex();
				if (argIndex == command.position || argIndex == command.elevation) {
					updatePosition(false);
				} else if (argIndex == command.rotation || argIndex == command.pitch || argIndex == command.roll || argIndex == command.scale) {
					updatePosition(true);
				} else if (argIndex == command.age && modelInfo.info instanceof GVillagerInfo) {
					int prevAge = (int)event.getOldValue();
					int newAge = (int)event.getNewValue();
					int grownUpAge = ((GVillagerInfo)modelInfo.info).grownUpAge;
					if ((prevAge < grownUpAge && newAge >= grownUpAge) || (prevAge >= grownUpAge && newAge < grownUpAge)) {
						createModelInstance();
					} else {
						updatePosition(true);
					}
				} else if (argIndex == command.id) {
					listeners.notify(EventType.CHANGE, Property.ID);
					listeners.notify(EventType.CHANGE, Property.NAME);
				} else if (argIndex == command.type) {
					listeners.notify(EventType.CHANGE, Property.NAME);
				}
			} else if (event.getProperty() == Statement.Property.COMMAND) {
				listeners.notify(EventType.CHANGE, Property.NAME);
			}
			listeners.notify(EventType.CHANGE, Property.STATEMENT, event.getOldValue(), event.getNewValue(), event.getIndex(), event);
		}
	};
	
	private void updatePosition(boolean withRotationAndScale) {
		if (statement == null) return;
		Coord coord = getPosition();
		float h = land3d == null ? 0f : land3d.land.getHeight(coord.x, coord.z);
		if (withRotationAndScale) {
			instance.transform.setToTranslation(coord.x, h + getElevation(), -coord.z);
			instance.transform.rotate(Vector3.X, getPitch() * MathUtils.radiansToDegrees);
			instance.transform.rotate(Vector3.Y, getRotation() * MathUtils.radiansToDegrees);
			instance.transform.rotate(Vector3.Z, getRoll() * MathUtils.radiansToDegrees);
			
			float scale = getScale();
			if (modelInfo.info instanceof GVillagerInfo) {
				GVillagerInfo villagerInfo = (GVillagerInfo)modelInfo.info;
				scale *= villagerInfo.ageToScale[MathUtils.clamp(getAge(), 0, villagerInfo.ageToScale.length - 1)];
			}
			if (scale != 1f) {
				instance.transform.scale(scale, scale, scale);
			}
		} else {
			instance.transform.setTranslation(coord.x, h + getElevation(), -coord.z);
		}
		listeners.notify(EventType.CHANGE, Property.POSITION);
		if (isTown()) {
			updateTownSpells(this);
		} else if (isTownCentre() && parent != null && parent.isTown()) {
			updateTownSpells(parent);
		}
	}
	
	public void lookAt(Vector3 target, Vector3 up) {
		Coord coord = getPosition();
		float h = land3d == null ? 0f : land3d.land.getHeight(coord.x, coord.z);
		Vector3 pos = new Vector3(coord.x, h + getElevation(), -coord.z);
		Vector3 forward = new Vector3(target).sub(pos).nor();
		Vector3 right = new Vector3(up).crs(forward).nor();
		Matrix4 tr = instance.transform;
		tr.idt();
		//X
		tr.val[Matrix4.M00] = right.x;
		tr.val[Matrix4.M10] = right.y;
		tr.val[Matrix4.M20] = right.z;
		//Y
		tr.val[Matrix4.M01] = up.x;
		tr.val[Matrix4.M11] = up.y;
		tr.val[Matrix4.M21] = up.z;
		//Z
		tr.val[Matrix4.M02] = forward.x;
		tr.val[Matrix4.M12] = forward.y;
		tr.val[Matrix4.M22] = forward.z;
		
		tr.setTranslation(pos);
		
		float scale = getScale();
		if (scale != 1f) {
			instance.transform.scale(scale, scale, scale);
		}
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean selected) {
		if (selected != this.selected) {
			this.selected = selected;
			updateMaterials();
			listeners.notify(EventType.CHANGE, Property.SELECTED, !selected, selected);
		}
	}
	
	public boolean isSyntaxError() {
		return syntaxError;
	}
	
	public boolean isDupIdError() {
		return dupIdError;
	}
	
	public boolean isOutOfBoundsError() {
		return outOfBoundsError;
	}
	
	public boolean isAnyError() {
		return syntaxError || dupIdError || outOfBoundsError;
	}
	
	public void setSyntaxError(boolean error) {
		if (error != this.syntaxError) {
			boolean wasAnyError = isAnyError();
			this.syntaxError = error;
			if (isAnyError() != wasAnyError) {
				updateMaterials();
			}
			listeners.notify(EventType.CHANGE, Property.SYNTAX_ERROR, !error, error);
		}
	}
	
	public void setDupIdError(boolean error) {
		if (error != this.dupIdError) {
			boolean wasAnyError = isAnyError();
			this.dupIdError = error;
			if (isAnyError() != wasAnyError) {
				updateMaterials();
			}
			listeners.notify(EventType.CHANGE, Property.DUPLICATE_ID, !error, error);
		}
	}
	
	public void setOutOfBoundsError(boolean error) {
		if (error != this.outOfBoundsError) {
			boolean wasAnyError = isAnyError();
			this.outOfBoundsError = error;
			if (isAnyError() != wasAnyError) {
				updateMaterials();
			}
			listeners.notify(EventType.CHANGE, Property.OUT_OF_BOUNDS, !error, error);
		}
	}
	
	private void updateMaterials() {
		for (int i = 0; i < instance.materials.size; i++) {
			Material material = instance.materials.get(i);
			ColorAttribute original = originalEmissive.get(i);
			boolean err = isAnyError();
			if (selected || err) {
				ColorAttribute attr;
				if (selected && err) {
					attr = HL_ERR_ATTR;
				} else if (selected) {
					attr = HIGHLIGHT_ATTR;
				} else {	//err
					attr = ERROR_ATTR;
				}
				//
				if (original == null) {
					material.set(attr);
				} else {
					float originalMax = Math.max(original.color.r, Math.max(original.color.g, original.color.b));
					float delta = Math.min(originalMax, 0.5f);
					Color col = new Color(attr.color).add(delta, delta, delta, 0f);
					material.set(ColorAttribute.createEmissive(col));
				}
			} else {
				if (original == null) {
					material.remove(ColorAttribute.Emissive);
				} else {
					material.set(original);
				}
			}
		}
	}
	
	private static int getTownSpellIndex(Object3D town, Object3D townSpell) {
		int index = 0;
		for (Object3D child : town.getChildren()) {
			if (child.getStatement().getCommand() == Command.CREATE_NEW_TOWN_SPELL) {
				if (child == townSpell) return index;
				index++;
			}
		}
		return -1;
	}
	
	public static void updateTownSpells(Object3D town) {
		for (Object3D child : town.getChildren()) {
			if (child.statement != null && child.statement.getCommand() == Command.CREATE_NEW_TOWN_SPELL) {
				child.updatePosition(true);
			}
		}
	}
	
	private Coord getTownSpellPosition() {
		Object3D town = this.getParent();
		if (town != null && town.getStatement() != null) {
			Object3D centre = lhx3d.getTownCentre(town);
			if (centre == null) centre = town;
			int index = getTownSpellIndex(town, this);
			//
			final float r = 11f;
			float a = (float)index * 0.898f - 0.67f;
			Vector3 pos = new Vector3(MathUtils.cos(a) * r, 0f, -MathUtils.sin(a) * r);
			pos.mul(centre.instance.transform);
			pos.y = 3.5f;
			return new Coord(pos);
		}
		return new Coord();
	}
	
	private Coord getStreamPosition() {
		if (children == null || children.isEmpty()) {
			return new Coord();
		} else {
			return children.get(0).getPosition();
		}
	}
	
	public Coord getPosition() {
		if (statement == null || !statement.isCommand()) return null;
		if (statement.getCommand() == Command.CREATE_NEW_TOWN_SPELL) {
			return getTownSpellPosition();
		} else if (statement.getCommand() == Command.CREATE_STREAM) {
			return getStreamPosition();
		}
		return new Coord(statement.getPosition());
	}
	
	public void setPosition(Coord coord) {
		if (statement == null) return;
		if (statement.getCommand() == Command.CREATE_STREAM) {
			if (children != null && !children.isEmpty()) {
				children.get(0).setPosition(coord);
			}
		} else {
			statement.setPosition(coord.x, coord.z);
		}
	}
	
	public void setPosition(float x, float z) {
		if (statement == null) return;
		if (statement.getCommand() == Command.CREATE_STREAM) {
			if (children != null && !children.isEmpty()) {
				children.get(0).setPosition(x, z);
			}
		} else {
			statement.setPosition(x, z);
		}
	}
	
	public float getElevation() {
		if (statement == null) return 0f;
		if (statement.getCommand() == Command.CREATE_NEW_TOWN_SPELL) {
			return isSprite() ? 4.5f : 3.5f;
		}
		return statement.getElevation();
	}
	
	public void setElevation(float elevation) {
		if (statement == null) return;
		statement.setElevation(elevation);
	}
	
	private float getTownSpellRotation() {
		Object3D town = this.getParent();
		if (town != null && town.getStatement() != null) {
			Object3D centre = lhx3d.getTownCentre(town);
			if (centre == null) centre = town;
			int index = getTownSpellIndex(town, this);
			//
			float a = (float)index * 0.898f - 0.67f + MathUtils.HALF_PI;
			return a - ((GSpellSeedInfo)modelInfo.info).yaw + centre.getRotation();
		}
		return 0f;
	}
	
	public float getRotation() {
		if (statement == null) return 0f;
		if (statement.getCommand() == Command.CREATE_NEW_TOWN_SPELL) {
			return getTownSpellRotation();
		}
		return statement.getRotation();
	}
	
	public void setRotation(float angle) {
		if (statement == null) return;
		statement.setRotation(angle);
	}
	
	public float getPitch() {
		if (statement == null) return 0f;
		return statement.getPitch();
	}
	
	public void setPitch(float angle) {
		if (statement == null) return;
		statement.setPitch(angle);
	}
	
	public float getRoll() {
		if (statement == null) return 0f;
		return statement.getRoll();
	}
	
	public void setRoll(float angle) {
		if (statement == null) return;
		statement.setRoll(angle);
	}
	
	public float getScale() {
		if (statement == null) return 1f;
		if (statement.getCommand() == Command.CREATE_NEW_TOWN_SPELL) {
			return ((GSpellSeedInfo)modelInfo.info).scaleMin;
		}
		return statement.getScale();
	}
	
	public void setScale(float scale) {
		if (statement == null) return;
		statement.setScale(scale);
	}
	
	public int getAge() {
		if (statement == null) return -1;
		return statement.getAge();
	}
	
	public void setAge(int age) {
		if (statement == null) return;
		statement.setAge(age);
	}
	
	public boolean hasDoor() {
		if (modelInfo != null) {
			return modelInfo.l3d.header.hasDoorPosition();
		}
		return false;
	}
	
	private static final Vector3 tmpVec = new Vector3();
	
	public Coord getDoorPosition(Coord out) {
		if (modelInfo == null) return null;
		L3DVec3 p = modelInfo.l3d.getDoorPosition();
		if (p == null) return null;
		float h = instance.transform.getTranslation(tmpVec).y;
		tmpVec.set(p.x, p.y, -p.z);
		tmpVec.mul(instance.transform);
		tmpVec.y -= h;
		return out.set(tmpVec);
	}
	
	public Coord getDoorPosition() {
		return getDoorPosition(new Coord());
	}
	
	public Object3D getHome() {
		if (statement.getCommand() == Command.CREATE_VILLAGER_POS) {
			LHXCoord homePos = statement.getSecondaryCoords();
			Coord doorPos = new Coord(homePos.x, homePos.z);
			return lhx3d.getBuildingFromDoor(doorPos, 1f);
		}
		return null;
	}
	
	public List<Object3D> getInhabitants() {
		List<Object3D> inhabitants = new ArrayList<>();
		Coord doorPos = this.getDoorPosition();
		if (doorPos != null) {
			Coord pos = new Coord(0, doorPos.y, 0);
			for (Object3D obj : lhx3d.getObjects()) {
				if (obj.statement.getCommand() == Command.CREATE_VILLAGER_POS) {
					LHXCoord homePos = obj.statement.getSecondaryCoords();
					pos.x = homePos.x;
					pos.z = homePos.z;
					if (doorPos.dst2(pos) <= 1f) {
						inhabitants.add(obj);
					}
				}
			}
		}
		return inhabitants;
	}
	
	private int spriteIndex;
	private float elapsed;
	
	public void act(float delta) {
		if (modelInfo != null && modelInfo.frameCount > 1) {
			elapsed += delta;
			if (elapsed >= modelInfo.frameInterval) {
				TextureAttribute diffuseAttr = instance.materials.get(0).get(TextureAttribute.class, TextureAttribute.Diffuse);
				TextureAttribute emissiveAttr = instance.materials.get(0).get(TextureAttribute.class, TextureAttribute.Emissive);
				int frames = (int)Math.floor(elapsed / modelInfo.frameInterval);
				spriteIndex = (spriteIndex + frames) % modelInfo.frameCount;
				int ix0 = (modelInfo.spriteOffset + spriteIndex) % 8;
				int iy0 = (modelInfo.spriteOffset + spriteIndex) / 8;
				diffuseAttr.offsetU = (float)ix0 / 8f;
				diffuseAttr.offsetV = (float)iy0 / 8f;
				emissiveAttr.offsetU = (float)ix0 / 8f;
				emissiveAttr.offsetV = (float)iy0 / 8f;
				elapsed -= modelInfo.frameInterval * frames;
			}
		}
	}
	
	@Override
	public String toString() {
		if (statement == null) return super.toString();
		Command cmd = statement.getCommand();
		if (cmd == null) return statement.toString();
		String r = cmd.objectDisplayName;
		if (cmd.id >= 0) r += " " + statement.getId();
		if (cmd.type >= 0) {
			Argument arg = cmd.args[cmd.type];
			Class<?> enumClass = arg.getEffectiveType().enumClass;
			if (arg.type == ArgType.INT && enumClass != null) {
				int type = statement.getTypeInt();
				Object entry = enumClass.getEnumConstants()[type];
				r += " " + entry;
			} else {
				r += " " + statement.getType();
			}
		}
		return r;
	}

	@Override
	public void close() {
		if (statement != null) {
			statement.listeners.remove(statementChangeListener);
		}
	}
}