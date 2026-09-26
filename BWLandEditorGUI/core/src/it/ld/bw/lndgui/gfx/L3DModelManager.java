package it.ld.bw.lndgui.gfx;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

import it.ld.bw.g3d.AllMeshes;
import it.ld.bw.g3d.G3DTexture;
import it.ld.bw.g3d.PackFile;
import it.ld.bw.info.AbodeNumber;
import it.ld.bw.info.GAbodeInfo;
import it.ld.bw.info.GAnimalInfo;
import it.ld.bw.info.GAnimatedStaticInfo;
import it.ld.bw.info.GBigForestInfo;
import it.ld.bw.info.GFeatureInfo;
import it.ld.bw.info.GFieldTypeInfo;
import it.ld.bw.info.GFishFarmInfo;
import it.ld.bw.info.GMobileObjectInfo;
import it.ld.bw.info.GMobileStaticInfo;
import it.ld.bw.info.GPotInfo;
import it.ld.bw.info.GScaffoldInfo;
import it.ld.bw.info.GSpellSeedInfo;
import it.ld.bw.info.GTownInfo;
import it.ld.bw.info.GTreeInfo;
import it.ld.bw.info.GVillagerInfo;
import it.ld.bw.info.InfoConstants;
import it.ld.bw.info.LHXVillagerInfo;
import it.ld.bw.info.PotType;
import it.ld.bw.info.TribeType;
import it.ld.bw.l3d.L3DBone;
import it.ld.bw.l3d.L3DFile;
import it.ld.bw.l3d.L3DFootprintEntry;
import it.ld.bw.l3d.L3DFootprintTriangle;
import it.ld.bw.l3d.L3DPrimitiveHeader;
import it.ld.bw.l3d.L3DSubmeshHeader;
import it.ld.bw.l3d.L3DTexture;
import it.ld.bw.l3d.L3DTriangle;
import it.ld.bw.l3d.L3DVec3;
import it.ld.bw.l3d.L3DVertex;
import it.ld.bw.l3d.L3DVertexGroup;
import it.ld.bw.lhx.Command;
import it.ld.bw.lhx.Command.ArgType;
import it.ld.bw.lhx.Command.Argument;
import it.ld.bw.lndgui.InfoLookup;
import it.ld.bw.lndgui.gfx.MorphableShader.MorphableAttribute;
import it.ld.bw.spell.ParticleMistCreator;
import it.ld.bw.spell.ParticleSpriteCreator;
import it.ld.bw.spell.SpellFile;
import it.ld.dds.DDSTexture;
import it.ld.libgdx.utils.Utils;

public class L3DModelManager implements Disposable {
	public static final Object VIRTUAL_TYPE = new Object();
	
	private static final Matrix4 FLIP_Z = new Matrix4().scale(1f, 1f, -1f);
	
	private static final MaterialTypeAttr[] materialTypeAttrs = new MaterialTypeAttr[] {
		new MaterialTypeAttr(true, false, BlendMode.Disabled, false, false),  // Smooth
		new MaterialTypeAttr(true, false, BlendMode.Standard, false, false),  // SmoothAlpha
		new MaterialTypeAttr(true, false, BlendMode.Disabled, false, false),  // Textured
		new MaterialTypeAttr(true, false, BlendMode.Standard, true, false),   // TexturedAlpha
		new MaterialTypeAttr(true, false, BlendMode.Standard, false, false),  // AlphaTextured
		new MaterialTypeAttr(true, false, BlendMode.Standard, true, false),   // AlphaTexturedAlpha
		new MaterialTypeAttr(false, false, BlendMode.Standard, true, false),  // AlphaTexturedAlphaNz
		new MaterialTypeAttr(false, false, BlendMode.Standard, false, false), // SmoothAlphaNz
		new MaterialTypeAttr(false, false, BlendMode.Standard, true, false),  // TexturedAlphaNz
		new MaterialTypeAttr(true, true, BlendMode.Standard, false, true),    // TexturedChroma
		new MaterialTypeAttr(true, true, BlendMode.Additive, true, true),     // AlphaTexturedAlphaAdditiveChroma
		new MaterialTypeAttr(false, true, BlendMode.Additive, true, true),    // AlphaTexturedAlphaAdditiveChromaNz
		new MaterialTypeAttr(true, false, BlendMode.Additive, true, false),   // AlphaTexturedAlphaAdditive
		new MaterialTypeAttr(false, false, BlendMode.Additive, true, false),  // AlphaTexturedAlphaAdditiveNz
		new MaterialTypeAttr(false, false, BlendMode.Disabled, false, false), // 0xe
		new MaterialTypeAttr(true, true, BlendMode.Standard, true, true),     // TexturedChromaAlpha
		new MaterialTypeAttr(false, true, BlendMode.Standard, true, true),    // TexturedChromaAlphaNz
		new MaterialTypeAttr(false, false, BlendMode.Disabled, false, false), // 0x11
		new MaterialTypeAttr(true, true, BlendMode.Standard, false, true),    // ChromaJustZ
	};
	
	public static boolean addBoneDebugShapes = false;
	
	private final File gameDir;
	private final InfoConstants infos;
	private final InfoLookup infoLookup;
	private final L3DFile[] meshes;
	
	private final L3DFile cameraL3D = new L3DFile();
	private final L3DFile townL3D = new L3DFile();
	private final L3DFile townCongregationL3D = new L3DFile();
	private final L3DFile flockL3D = new L3DFile();
	private final L3DFile forestL3D = new L3DFile();
	private final L3DFile fishfarmL3D = new L3DFile();
	private final L3DFile drinkL3D = new L3DFile();
	private final L3DFile streamL3D = new L3DFile();
	private final L3DFile streamPointL3D = new L3DFile();
	private BillboardQuadAsset cameraAsset;
	private BillboardQuadAsset townAsset;
	private BillboardQuadAsset townCongregationAsset;
	private BillboardQuadAsset flockAsset;
	private BillboardQuadAsset forestAsset;
	private BillboardQuadAsset fishfarmAsset;
	private BillboardQuadAsset drinkAsset;
	private BillboardQuadAsset streamAsset;
	private BillboardQuadAsset streamPointAsset;
	
	private final Map<String, L3DFile> particles3D = new HashMap<>();
	
	private final Map<String, Texture> rawTextures = new HashMap<>();
	private final Map<Integer, Texture> textures = new HashMap<>();
	private final Map<L3DFile, Map<Integer, Texture>> l3dsTextures = new HashMap<>();
	private final Map<L3DFile, Model> models = new HashMap<>();
	private final Map<ModelKey, ModelInfo> modelsInfo = new HashMap<>();
	private ModelInfo templeModel;
	
	private final ModelBuilder modelBuilder = new ModelBuilder();
	
	public L3DModelManager(File gameDir, Skin skin) throws Exception {
		this.gameDir = gameDir;
		File infoDatFile = new File(gameDir, "Scripts/info.dat");
		PackFile infoDat = PackFile.load(infoDatFile);
		this.infos = new InfoConstants((int) infoDatFile.length());
		this.infos.read(infoDat.getBlock("Info"));
		this.infoLookup = new InfoLookup(infos);
		//
		File packFile = new File(gameDir, "Data/AllMeshes.g3d");
		PackFile pack = PackFile.load(packFile);
		//
		int meshId = 0;
		List<byte[]> meshesData = pack.getMeshes();
		this.meshes = new L3DFile[meshesData.size()];
		for (byte[] data : meshesData) {
			L3DFile l3d = new L3DFile();
			l3d.read(data);
			l3d.setFile(packFile);
			l3d.setIndex(meshId);
			l3d.setName(AllMeshes.names[meshId]);
			meshes[meshId] = l3d;
			meshId++;
		}
		//
		for (Entry<String, G3DTexture> e : pack.getTextures().entrySet()) {
			G3DTexture g3dTxt = e.getValue();
			int id = g3dTxt.getHeader().getId();
			DDSTexture dds = g3dTxt.getTexture();
			Texture texture = Utils.toTexture(Utils.abgrToARGB(dds.getPixels()), dds.header.width, dds.header.height, false);
			texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
			texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			textures.put(id, texture);
		}
		//
		File templeFile = new File(gameDir, "Data/Citadel/OutsideMeshes/b_first_temple_l3d.zzz");
		L3DFile l3dTemple = L3DFile.load(templeFile);
		this.templeModel = getModelInfo(l3dTemple, true, null, Command.CREATE_CITADEL, null);
		//
		cameraAsset = createIconAsset(skin.getRegion("cinema"), true, 12);
		townAsset = createIconAsset(skin.getRegion("town"), false, 18);
		townCongregationAsset = createIconAsset(skin.getRegion("assembly-point"), false, 12);
		flockAsset = createIconAsset(skin.getRegion("flock"), false, 15);
		forestAsset = createIconAsset(skin.getRegion("sound-forest"), true, 18);
		fishfarmAsset = createIconAsset(skin.getRegion("fish"), true, 12);
		drinkAsset = createIconAsset(skin.getRegion("drink"), true, 12);
		streamAsset = createIconAsset(skin.getRegion("sound-river"), true, 15);
		streamPointAsset = createIconAsset(skin.getRegion("circle"), false, 1);
		models.put(cameraL3D, cameraAsset.model);
		models.put(townL3D, townAsset.model);
		models.put(townCongregationL3D, townCongregationAsset.model);
		models.put(flockL3D, flockAsset.model);
		models.put(forestL3D, forestAsset.model);
		models.put(fishfarmL3D, fishfarmAsset.model);
		models.put(drinkL3D, drinkAsset.model);
		models.put(streamL3D, streamAsset.model);
		models.put(streamPointL3D, streamPointAsset.model);
	}
	
	private BillboardQuadAsset createIconAsset(TextureRegion region, boolean vertical, float size) {
		BillboardQuadAsset asset = new BillboardQuadAsset(region, size, size, vertical, vertical ? 0 : 0.5f, true);
		asset.model.nodes.get(0).id = "status_0";
		return asset;
	}
	
	public InfoConstants getInfoConstants() {
		return infos;
	}
	
	public InfoLookup getInfoLookupTable() {
		return infoLookup;
	}
	
	private ArrayList<ModelInfo> catalog = null;
	
	public ArrayList<ModelInfo> getCatalog() throws Exception {
		if (catalog == null) {
			catalog = new ArrayList<ModelInfo>(1024);
			for (String type : infoLookup.getAbodeTypes()) {
				catalog.add(getAbode(type));
			}
			for (String type : infoLookup.getSpellDispenserTypes()) {
				catalog.add(getSpellDispenser(type));
			}
			for (String type : infoLookup.getTownCentreTypes()) {
				catalog.add(getTownCentre(type));
			}
			for (int type = 0; type < infos.animal.length; type++) {
				ModelInfo info = getAnimal(type);
				info.age = 5;
				catalog.add(info);
			}
			for (String type : infoLookup.getAnimatedStaticTypes()) {
				catalog.add(getAnimatedStatic(type));
			}
			for (int type = 0; type < infos.bigForest.length; type++) {
				catalog.add(getBigForest(type));
			}
			for (String type : infoLookup.getFeatureTypes()) {
				catalog.add(getFeature(type));
			}
			for (int type = 0; type < infos.mobileObject.length; type++) {
				catalog.add(getMobileObject(type));
			}
			for (int type = 0; type < infos.mobileStatic.length; type++) {
				if (type == 7 || type == 59) continue;	//These are for lanterns
				catalog.add(getMobileStatic(type));
			}
			for (int type = 0; type < infos.pot.length; type++) {
				catalog.add(getPot(type));
			}
			for (int type = 0; type < infos.tree.length; type++) {
				catalog.add(getTree(type));
			}
			for (String type : infoLookup.getVillagerTypes()) {
				ModelInfo info = getVillager(type, 20);
				info.age = 20;
				catalog.add(info);
			}
			for (String type : infoLookup.getVillagerTypes()) {
				ModelInfo info = getVillager(type, 8);
				info.age = 8;
				catalog.add(info);
			}
			for (int type = 63; type <= 83; type++) {
				ModelInfo info = getSpecialVillager(type);
				info.age = 20;
				catalog.add(info);
			}
			for (String type : infoLookup.getSpellSeedTypes()) {
				ModelInfo info = getTownSpell(type);
				if (((GSpellSeedInfo)info.info).canBeCast) {
					catalog.add(info);
				}
			}
			{
				catalog.add(getCamera());
				catalog.add(getTemple());
				catalog.add(getTown());
				catalog.add(getTownCongregation());
				catalog.add(getFlock());
				catalog.add(getForest());
				catalog.add(getStreetLantern(7));	//7 or 59
				catalog.add(getBonfire());
				catalog.add(getField(0));
				catalog.add(getFishFarm());
				catalog.add(getScaffold());
				catalog.add(getDrinkWaypoint());
			}
		}
		return catalog;
	}
	
	public ModelInfo getCamera() throws Exception {
		ModelInfo r = getModelInfo(cameraL3D, false, null, Command.START_CAMERA_POS, VIRTUAL_TYPE);
		r.isSprite = true;
		return r;
	}
	
	public ModelInfo getTown() throws Exception {
		GTownInfo info = infos.town;
		return getModelInfo(townL3D, false, info, Command.CREATE_TOWN, VIRTUAL_TYPE);
	}
	
	public ModelInfo getTownCongregation() throws Exception {
		return getModelInfo(townCongregationL3D, false, null, Command.SET_TOWN_CONGREGATION_POS, VIRTUAL_TYPE);
	}
	
	public ModelInfo getFlock() throws Exception {
		return getModelInfo(flockL3D, false, null, Command.CREATE_FLOCK, VIRTUAL_TYPE);
	}
	
	public ModelInfo getForest() throws Exception {
		ModelInfo r = getModelInfo(forestL3D, false, null, Command.CREATE_FOREST, VIRTUAL_TYPE);
		r.isSprite = true;
		return r;
	}
	
	public ModelInfo getDrinkWaypoint() throws Exception {
		ModelInfo r = getModelInfo(drinkL3D, false, null, Command.CREATE_DRINK_WAYPOINT, VIRTUAL_TYPE);
		r.isSprite = true;
		return r;
	}
	
	public ModelInfo getStream() throws Exception {
		ModelInfo r = getModelInfo(streamL3D, false, null, Command.CREATE_STREAM_POINT, VIRTUAL_TYPE);
		r.isSprite = true;
		return r;
	}
	
	public ModelInfo getStreamPoint() throws Exception {
		return getModelInfo(streamPointL3D, false, null, Command.CREATE_STREAM_POINT, VIRTUAL_TYPE);
	}
	
	public ModelInfo getAbode(String type) throws Exception {
		GAbodeInfo info = infoLookup.getAbode(type);
		if (info == null) info = infoLookup.getTownCentre(type);
		if (info == null) info = infoLookup.getSpellDispenser(type);
		if (info == null) throw new IllegalArgumentException("Unknown aboode type: " + type);
		AbodeNumber n = info.abodeNumber;
		boolean morphable = n == AbodeNumber.GRAVEYARD || n == AbodeNumber.STORAGE_PIT ||
				n == AbodeNumber.WORKSHOP || n == AbodeNumber.CRECHE ||
				n == AbodeNumber.FOOTBALL_PITCH || n == AbodeNumber.TOWN_CENTRE ||
				n == AbodeNumber.FIELD ||
				(n == AbodeNumber.WONDER && (
					info.tribeType == TribeType.CELTIC || info.tribeType == TribeType.JAPANESE ||
					info.tribeType == TribeType.INDIAN || info.tribeType == TribeType.NORSE ||
					info.tribeType == TribeType.TIBETAN
				));
		return getModelInfo(info.meshId, morphable, info, Command.CREATE_ABODE, type);
	}
	
	public ModelInfo getSpellDispenser(String type) throws Exception {
		GAbodeInfo info = infoLookup.getSpellDispenser(type);
		return getModelInfo(info.meshId, false, info, Command.CREATE_SPELL_DISPENSER, type);
	}
	
	public ModelInfo getTownCentre(String type) throws Exception {
		GAbodeInfo info = infoLookup.getTownCentre(type);
		return getModelInfo(info.meshId, false, info, Command.CREATE_TOWN_CENTRE, type);
	}
	
	public ModelInfo getAnimal(int type) throws Exception {
		GAnimalInfo info = infos.animal[type];
		return getModelInfo(info.std, false, info, Command.CREATE_NEW_ANIMAL, type);
	}
	
	public ModelInfo getAnimatedStatic(String type) throws Exception {
		GAnimatedStaticInfo info = infoLookup.getAnimatedStatic(type);
		return getModelInfo(info.meshId, false, info, Command.CREATE_ANIMATED_STATIC, type);
	}
	
	public ModelInfo getBigForest(int type) throws Exception {
		GBigForestInfo info = infos.bigForest[type];
		return getModelInfo(info.meshId, true, info, Command.CREATE_NEW_BIG_FOREST, type);
	}
	
	public ModelInfo getDeadTree(int type) throws Exception {
		GTreeInfo info = infos.tree[type];
		return getModelInfo(info.burning, false, info, Command.CREATE_DEAD_TREE, type);
	}
	
	public ModelInfo getFeature(String type) throws Exception {
		GFeatureInfo info = infoLookup.getFeature(type);
		return getModelInfo(info.meshId, false, info, Command.CREATE_NEW_FEATURE, type);
	}
	
	public ModelInfo getField(int type) throws Exception {
		GFieldTypeInfo info = infos.fieldType[type];
		return getModelInfo(594, false, info, Command.CREATE_NEW_TOWN_FIELD, type);
	}
	
	public ModelInfo getMobileObject(int type) throws Exception {
		GMobileObjectInfo info = infoLookup.getMobileObject(type);
		return getModelInfo(info.meshId, false, info, Command.CREATE_MOBILEOBJECT, type);
	}
	
	public ModelInfo getMobileStatic(int type) throws Exception {
		GMobileStaticInfo info = infoLookup.getMobileStatic(type);
		return getModelInfo(info.meshId, false, info, Command.CREATE_MOBILE_STATIC, type);
	}
	
	public ModelInfo getPot(int type) throws Exception {
		GPotInfo info = infos.pot[type];
		boolean morphable = info.potType == PotType.PILE_FOOD;
		return getModelInfo(info.meshId, morphable, info, Command.CREATE_POT, type);
	}
	
	public ModelInfo getStreetLantern(int type) throws Exception {
		return getModelInfo(398, false, null, Command.CREATE_STREET_LANTERN, type);
	}
	
	public ModelInfo getBonfire() throws Exception {
		return getModelInfo(148, false, null, Command.CREATE_BONFIRE, null);
	}
	
	public ModelInfo getScaffold() throws Exception {
		GScaffoldInfo info = infos.scaffold;
		return getModelInfo(info.meshId, false, info, Command.CREATE_SCAFFOLD, null);
	}
	
	public ModelInfo getTemple() throws Exception {
		return templeModel;
	}
	
	public ModelInfo getTree(int type) throws Exception {
		GTreeInfo info = infos.tree[type];
		return getModelInfo(info.normal, false, info, Command.CREATE_NEW_TREE, type);
	}
	
	public ModelInfo getVillager(String type, int age) throws Exception {
		GVillagerInfo info = infoLookup.getVillager(type);
		int meshId = age < info.grownUpAge ? info.childMeshHigh : info.highDetail;
		return getModelInfo(meshId, false, info, Command.CREATE_VILLAGER_POS, type);
	}
	
	public ModelInfo getTownVillager(LHXVillagerInfo type, int age) throws Exception {
		GVillagerInfo info = infoLookup.getVillager(type.name());
		int meshId = age < info.grownUpAge ? info.childMeshHigh : info.highDetail;
		return getModelInfo(meshId, false, info, Command.CREATE_TOWN_VILLAGER, type);
	}
	
	public ModelInfo getSpecialVillager(int type) throws Exception {
		GVillagerInfo info = infos.villager[type];
		return getModelInfo(info.highDetail, false, info, Command.CREATE_SPECIAL_TOWN_VILLAGER, type);
	}
	
	public ModelInfo getFishFarm() throws Exception {
		GFishFarmInfo info = infos.fishFarm;
		ModelInfo r = getModelInfo(fishfarmL3D, false, info, Command.CREATE_TOWN_FISH_FARM, VIRTUAL_TYPE);
		r.isSprite = true;
		return r;
	}
	
	public ModelInfo getTownSpell(String type) throws Exception {
		GSpellSeedInfo info = infoLookup.getSpellSeed(type);
		L3DFile l3d = meshes[info.meshId];
		boolean isNew = false;
		int spriteOffset = 0;
		int frameCount = 0;
		float frameRate = 0;
		if (!info.useMesh) {
			l3d = particles3D.get(type);
			if (l3d == null) {
				l3d = new L3DFile();
				isNew = true;
				String filename = "Data/Spells/ZSpellFiles/SF_" + snakeToCamel(info.particleType.name()) + "_txt.zzz";
				File file = new File(gameDir, filename);
				SpellFile spellFile = SpellFile.load(file);
				ParticleSpriteCreator spriteCreator = spellFile.getObject(ParticleSpriteCreator.class);
				if (spriteCreator != null) {
					Texture colorTexture = getColorTexture(spriteCreator.textureFileName);
					Texture alphaTexture = getAlphaTexture(spriteCreator.textureFileName);
					spriteOffset = spriteCreator.fileOffset;
					frameCount = spriteCreator.numFrames;
					frameRate = spriteCreator.frameRate;
					int spriteSize = 256 / 8;
					int ix0 = spriteCreator.fileOffset % 8;
					int iy0 = spriteCreator.fileOffset / 8;
					TextureRegion colorRegion = new TextureRegion(colorTexture, ix0 * spriteSize, iy0 * spriteSize, spriteSize, spriteSize);
					TextureRegion alphaRegion = new TextureRegion(alphaTexture, ix0 * spriteSize, iy0 * spriteSize, spriteSize, spriteSize);
					float r = (float)spriteCreator.colorR / 255f;
					float g = (float)spriteCreator.colorG / 255f;
					float b = (float)spriteCreator.colorB / 255f;
					float a = (float)spriteCreator.colorA / 255f;
					Model model = createSprite(colorRegion, alphaRegion, new Color(r, g, b, a), 2, 2);
					models.put(l3d, model);
				} else {
					ParticleMistCreator mistCreator = spellFile.getObject(ParticleMistCreator.class);
					if (mistCreator != null) {
						String textureName = mistCreator.textureFileName != null ? mistCreator.textureFileName : "Data/Textures/S_SpriteSheet3.raw";
						Texture colorTexture = getColorTexture(textureName);
						Texture alphaTexture = getAlphaTexture(textureName);
						int spriteSize = 256 / 8;
						int ix0 = 34 % 8;
						int iy0 = 34 / 8;
						TextureRegion colorRegion = new TextureRegion(colorTexture, ix0 * spriteSize, iy0 * spriteSize, spriteSize, spriteSize);
						TextureRegion alphaRegion = new TextureRegion(alphaTexture, ix0 * spriteSize, iy0 * spriteSize, spriteSize, spriteSize);
						float r = (float)mistCreator.colorR / 255f;
						float g = (float)mistCreator.colorG / 255f;
						float b = (float)mistCreator.colorB / 255f;
						float a = (float)mistCreator.colorA / 255f;
						Model model = createSprite(colorRegion, alphaRegion, new Color(r, g, b, a), 2, 2);
						models.put(l3d, model);
					} else {
						System.err.println("SpriteCreator not found for " + type + " in " + filename);
					}
				}
				particles3D.put(type, l3d);
			}
		}
		ModelInfo modelInfo = getModelInfo(l3d, false, info, Command.CREATE_NEW_TOWN_SPELL, type);
		if (isNew) {
			modelInfo.isSprite = !info.useMesh;
			modelInfo.spriteOffset = spriteOffset;
			modelInfo.frameCount = frameCount;
			modelInfo.frameInterval = 1f / frameRate;
		}
		return modelInfo;
	}
	
	public L3DFile getL3D(int meshId) {
		return meshes[meshId];
	}
	
	public ModelInfo getModelInfo(int meshId, boolean morphable, @Null Object info, @Null Command command, @Null Object type) throws Exception {
		L3DFile l3dFile = meshes[meshId];
		return getModelInfo(l3dFile, morphable, info, command, type);
	}
	
	public ModelInfo getModelInfo(File file, boolean morphable, @Null Object info, @Null Command command, @Null Object type) throws Exception {
		L3DFile l3d = L3DFile.load(file);
    	return getModelInfo(l3d, false, info, command, type);
	}
	
	public ModelInfo getModelInfo(L3DFile l3d, boolean morphable, @Null Object info, @Null Command command, @Null Object type) throws Exception {
		ModelKey key = new ModelKey(command, type, l3d);
		ModelInfo modelInfo = modelsInfo.get(key);
		if (modelInfo == null) {
			Model model = models.get(l3d);
			if (model == null) {
				model = generate(l3d, morphable);
				models.put(l3d, model);
			}
			modelInfo = new ModelInfo(l3d, model, info, command, type);
			modelsInfo.put(key, modelInfo);
		}
		return modelInfo;
	}
	
	private Map<Integer, Texture> getL3DTextures(L3DFile l3d) {
		if (l3dsTextures.containsKey(l3d)) {
			return l3dsTextures.get(l3d);
		} else {
			Map<Integer, Texture> l3dTextures = new HashMap<>();
			if (!l3d.skinTextures.isEmpty()) {
				for (L3DTexture l3dTxt : l3d.skinTextures) {
					Texture texture = Utils.toTexture(Utils.abgrToARGB(l3dTxt.getPixels()), L3DTexture.WIDTH, L3DTexture.HEIGHT, false);
					texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
					texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
					l3dTextures.put(l3dTxt.id, texture);
				}
			}
			if (l3d.footprint != null && l3d.footprint.header.count > 0) {
				for (int i = 0; i < l3d.footprint.header.count; i++) {
					L3DFootprintEntry entry = l3d.footprint.entries[i];
					Texture texture = Utils.toTexture(Utils.abgrToARGB(entry.getPixels()), entry.width, entry.height, false);
					texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
					texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
					l3dTextures.put(1000 + i, texture);
				}
			}
			if (l3dTextures.isEmpty()) {
				l3dTextures = null;
			}
			l3dsTextures.put(l3d, l3dTextures);
			return l3dTextures;
		}
	}
	
	private Texture getCombinedTexture(String filename) throws Exception {
		Texture texture = rawTextures.get(filename);
		if (texture == null) {
			File filec = new File(gameDir, filename);
			File filea = new File(gameDir, filename.replaceFirst("\\.raw$", "a.raw"));
			byte[] colorData = Files.readAllBytes(filec.toPath());
			byte[] alphaData = Files.readAllBytes(filea.toPath());
			byte[] pixels = new byte[alphaData.length * 4];
			for (int ci = 0, ai = 0, di = 0; ai < alphaData.length;) {
				pixels[di++] = colorData[ci++];
				pixels[di++] = colorData[ci++];
				pixels[di++] = colorData[ci++];
				pixels[di++] = alphaData[ai++];
			}
			texture = Utils.toTexture(pixels, 256, 256, true);
			rawTextures.put(filename, texture);
		}
		return texture;
	}
	
	private Texture getColorTexture(String filename) throws Exception {
		Texture texture = rawTextures.get(filename);
		if (texture == null) {
			File filec = new File(gameDir, filename);
			byte[] colorData = Files.readAllBytes(filec.toPath());
			texture = Utils.toTexture(colorData, 256, 256, true);
			rawTextures.put(filename, texture);
		}
		return texture;
	}
	
	private Texture getAlphaTexture(String filename) throws Exception {
		filename = filename.replaceFirst("\\.raw$", "a.raw");
		Texture texture = rawTextures.get(filename);
		if (texture == null) {
			File filec = new File(gameDir, filename);
			byte[] alphaData = Files.readAllBytes(filec.toPath());
			byte[] pixels = new byte[alphaData.length * 4];
			for (int si = 0, di = 0; si < alphaData.length; si++) {
				pixels[di++] = 0;
				pixels[di++] = 0;
				pixels[di++] = 0;
				pixels[di++] = alphaData[si];
			}
			texture = Utils.toTexture(pixels, 256, 256, true);
			rawTextures.put(filename, texture);
		}
		return texture;
	}
	
	private Texture getTexture(int skinID, Map<Integer, Texture> l3dTextures) throws Exception {
		Texture texture = null;
		if (l3dTextures != null) {
			texture = l3dTextures.get(skinID);
		}
		if (texture == null) {
			texture = textures.get(skinID);
		}
		if (texture == null) {
			throw new Exception("Cannot find a texture with id " + skinID);
		}
		return texture;
	}
	
	private final Vector3 tmpPos = new Vector3();
	private final Vector3 tmpNor = new Vector3();
	
	private Model generate(L3DFile l3d, boolean morphable) throws Exception {
		Map<Integer, Texture> l3dTextures = getL3DTextures(l3d);
		Model model = null;
		Node[] boneNodes = null;
		Array<NodePart> skinnedParts = new Array<>();
		modelBuilder.begin();
		try {
			if (!l3d.bones.isEmpty()) {
				boneNodes = createNodes(modelBuilder, l3d.bones, addBoneDebugShapes);
			}
			
			int vertexBase = 0;
			if (l3d.footprint != null && l3d.footprint.entries.length > 0) {
				Node node = modelBuilder.node();
				node.id = "footprint";
				int partId = 0;
				for (L3DFootprintEntry entry : l3d.footprint.entries) {
					Texture texture = l3dTextures.get(1000 + partId);
					VertexAttributes vertexAttributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute.TexCoords(0));
					Material material = new Material(TextureAttribute.createDiffuse(texture),
							new BlendingAttribute(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f),
							IntAttribute.createCullFace(0));
					MeshPartBuilder meshBuilder = modelBuilder.part("part"+partId, GL20.GL_TRIANGLES, vertexAttributes, material);
					for (L3DFootprintTriangle tri : entry.triangles) {
						VertexInfo a = new VertexInfo();
						VertexInfo b = new VertexInfo();
						VertexInfo c = new VertexInfo();
						a.setPos(tri.world[0].x, 0, -tri.world[0].y);
						b.setPos(tri.world[1].x, 0, -tri.world[1].y);
						c.setPos(tri.world[2].x, 0, -tri.world[2].y);
						final float scale = 0.008f;	//TODO this is crap
						a.setUV(tri.uv[0].x * scale, tri.uv[0].y * scale);
						b.setUV(tri.uv[1].x * scale, tri.uv[1].y * scale);
						c.setUV(tri.uv[2].x * scale, tri.uv[2].y * scale);
						meshBuilder.triangle(a, b, c);
					}
					partId++;
				}
			}
			
			for (L3DSubmeshHeader submesh : l3d.submeshHeaders) {
				if (!submesh.isPhysics() && (submesh.getLOD() & 1) != 1) {
					continue;
				}
				
				final boolean hasBones = submesh.hasBones();
				Node node = modelBuilder.node();
				node.id = submesh.isPhysics() ? "collider" : ("status_" + submesh.getStatus());
				int partId = 0;
				for (L3DPrimitiveHeader primitive : submesh.primitives) {
					Material material = createMaterial(primitive, l3dTextures, morphable);
					VertexAttributes vertexAttributes = createVertexAttributes(hasBones);
					MeshPartBuilder meshBuilder = modelBuilder.part("part"+partId, GL20.GL_TRIANGLES, vertexAttributes, material);
					if (hasBones) {
						NodePart nodePart = node.parts.get(node.parts.size - 1);
					    skinnedParts.add(nodePart);
					}
					//Add vertices
					int verticesInGroup = Integer.MAX_VALUE;
					int currentGroup = 0;
					int boneIndex = -1;
					if (hasBones) {
						L3DVertexGroup group = primitive.vertexGroups.get(currentGroup);
						verticesInGroup = group.vertexCount;
						boneIndex = group.boneIndex;
					}
					for (L3DVertex vertex : primitive.vertices) {
						if (verticesInGroup == 0) {
							currentGroup++;
							L3DVertexGroup group = primitive.vertexGroups.get(currentGroup);
							verticesInGroup = group.vertexCount;
							boneIndex = group.boneIndex;
						}
						addVertex(meshBuilder, vertex, boneIndex, boneNodes);
						verticesInGroup--;
					}
					//Add triangles
					for (L3DTriangle triangle : primitive.triangles) {
						meshBuilder.triangle((short)(vertexBase + triangle.index1), (short)(vertexBase + triangle.index2), (short)(vertexBase + triangle.index3));
					}
					vertexBase += primitive.vertices.size();
					partId++;
				}
			}
		} finally {
			model = modelBuilder.end();
		}
		
		//Finalize model
		if (boneNodes != null && boneNodes.length > 0) {
			//Remove child nodes from model.nodes
			for (Node bone : boneNodes) {
		        if (bone.hasParent()) {
		            model.nodes.removeValue(bone, true);
		        }
		    }
			//Bind nodeParts to inv bone matrices
		    for (NodePart sp : skinnedParts) {
		    	setupSkinning(sp, boneNodes);
		    }
		    //model.getNode("bone_1").localTransform.rotate(Vector3.Y, 30);	//just for debug
		    model.calculateTransforms();
		}
		return model;
	}
	
	private final float[] vertexValues8 = new float[8];
	private final float[] vertexValues10 = new float[10];
	
	private void addVertex(MeshPartBuilder meshBuilder, L3DVertex vertex, int boneIndex, Node[] boneNodes) {
		float[] vertexValues = boneIndex < 0 ? vertexValues8 : vertexValues10;
		tmpPos.set(vertex.position.x, vertex.position.y, -vertex.position.z);
		tmpNor.set(vertex.normal.x, vertex.normal.y, -vertex.normal.z);
		if (boneIndex >= 0) {
			tmpPos.mul(boneNodes[boneIndex].globalTransform);
			tmpNor.rot(boneNodes[boneIndex].globalTransform).nor();
		}

		vertexValues[0] = tmpPos.x;
		vertexValues[1] = tmpPos.y;
		vertexValues[2] = tmpPos.z;
		vertexValues[3] = tmpNor.x;
		vertexValues[4] = tmpNor.y;
		vertexValues[5] = tmpNor.z;
		vertexValues[6] = vertex.texCoords.x;
		vertexValues[7] = vertex.texCoords.y;
		if (boneIndex >= 0) {
			vertexValues[8] = boneIndex;
			vertexValues[9] = 1f;
		}
		meshBuilder.vertex(vertexValues);
	}
	
	private void setupSkinning(NodePart part, Node[] boneNodes) {
	    part.invBoneBindTransforms = new ArrayMap<Node, Matrix4>(boneNodes.length);
	    for (int i = 0; i < boneNodes.length; i++) {
	        int boneIndex = i;
	        Node boneNode = boneNodes[boneIndex];
	        Matrix4 invBind = new Matrix4(boneNode.globalTransform).inv();
	        part.invBoneBindTransforms.put(boneNode, invBind);
	    }
	}
	
	private static VertexAttributes createVertexAttributes(boolean hasBones) {
		List<VertexAttribute> vertexAttributesList = new ArrayList<>();
		vertexAttributesList.add(VertexAttribute.Position());
		vertexAttributesList.add(VertexAttribute.Normal());
		vertexAttributesList.add(VertexAttribute.TexCoords(0));
		if (hasBones) {
			vertexAttributesList.add(VertexAttribute.BoneWeight(0));
		}
		return new VertexAttributes(vertexAttributesList.toArray(new VertexAttribute[0]));
	}
	
	private Material createMaterial(L3DPrimitiveHeader primitive, Map<Integer, Texture> l3dTextures, boolean morphable) throws Exception {
		MaterialTypeAttr mta = materialTypeAttrs[primitive.material.type.ordinal()];
		Array<Attribute> materialAttributes = new Array<>();
		if (primitive.material.skinID != -1) {
			Texture texture = getTexture(primitive.material.skinID, l3dTextures);
			materialAttributes.add(TextureAttribute.createDiffuse(texture));
		} else {
			Color color = new Color(primitive.material.color);
			materialAttributes.add(ColorAttribute.createDiffuse(color));
		}
		if (mta.alphaTest && mta.thresholdAlpha) {
			materialAttributes.add(FloatAttribute.createAlphaTest(0.5f));
		}
		if (mta.blendMode.blend) {
			materialAttributes.add(new BlendingAttribute(true, mta.blendMode.srcFunc, mta.blendMode.dstFunc, 1f));
		}
		if (!mta.depthWrite) {
			materialAttributes.add(new DepthTestAttribute(GL20.GL_LEQUAL, mta.depthWrite));
		}
		//materialAttributes.add(IntAttribute.createCullFace(0));
		if (primitive.material.cullMode == 0) {
			materialAttributes.add(IntAttribute.createCullFace(GL20.GL_FRONT));
		} else if (primitive.material.cullMode == 1) {	//Used only by colliders
			materialAttributes.add(IntAttribute.createCullFace(0));
		} else if (primitive.material.cullMode == 4) {
			materialAttributes.add(IntAttribute.createCullFace(GL20.GL_FRONT));
		} else if (primitive.material.cullMode == 5) {
			materialAttributes.add(IntAttribute.createCullFace(0));
		} else if (primitive.material.cullMode == 20) {	//Used by town spell icons
			materialAttributes.add(IntAttribute.createCullFace(0));
		} else if (primitive.material.cullMode == 68) {	//Used by town spell icons
			materialAttributes.add(IntAttribute.createCullFace(0));
		} else if (primitive.material.cullMode == 69) {
			materialAttributes.add(IntAttribute.createCullFace(0));
		} else if (primitive.material.cullMode == 84) {	//Used by town spell icons
			materialAttributes.add(IntAttribute.createCullFace(0));
		} else {
			System.err.println("Unexpected cull mode: " + primitive.material.cullMode);
		}
		if (morphable) {
			materialAttributes.add(new MorphableAttribute());
		}
		return new Material(materialAttributes);
	}
	
	private static Matrix4[] calculateTransforms(List<L3DBone> bones) {
		Matrix4[] transforms = new Matrix4[bones.size()];
		Matrix4 original = new Matrix4();
		for (int i = 0; i < bones.size(); i++) {
			L3DBone bone = bones.get(i);
			L3DVec3 position = bone.position;
			float[] orientation = bone.orientation;
			
			original.val[Matrix4.M00] = orientation[0];
			original.val[Matrix4.M01] = orientation[3];
			original.val[Matrix4.M02] = orientation[6];
			original.val[Matrix4.M03] = position.x;

			original.val[Matrix4.M10] = orientation[1];
			original.val[Matrix4.M11] = orientation[4];
			original.val[Matrix4.M12] = orientation[7];
			original.val[Matrix4.M13] = position.y;

			original.val[Matrix4.M20] = orientation[2];
			original.val[Matrix4.M21] = orientation[5];
			original.val[Matrix4.M22] = orientation[8];
			original.val[Matrix4.M23] = position.z;

			original.val[Matrix4.M30] = 0;
			original.val[Matrix4.M31] = 0;
			original.val[Matrix4.M32] = 0;
			original.val[Matrix4.M33] = 1;
			
			transforms[i] = new Matrix4(FLIP_Z).mul(original).mul(FLIP_Z);
		}
		return transforms;
	}
	
	private static Node[] createNodes(ModelBuilder modelBuilder, List<L3DBone> bones, boolean addDebugShapes) {
		Matrix4[] transforms = calculateTransforms(bones);
		
		//Build children lookup table
		List<List<Integer>> childrenLUT = new ArrayList<>(bones.size());
		for (int i = 0; i < bones.size(); i++) {
			childrenLUT.add(new ArrayList<>());
		}
		for (int i = 0; i < bones.size(); i++) {
			L3DBone bone = bones.get(i);
			if (bone.parent != -1) {
				List<Integer> parent = childrenLUT.get(bone.parent);
				parent.add(i);
			}
		}
		
		//Build nodes
		Node[] nodes = new Node[bones.size()];
		for (int i = 0; i < bones.size(); i++) {
			L3DBone bone = bones.get(i);
			Matrix4 transform = transforms[i];
			Node node = modelBuilder.node();
			//Node node = bone.parent == -1 ? modelBuilder.node() : new Node();
			node.id = bone.parent == -1 ? "root" : ("bone_" + i);
			node.isAnimated = true;
			node.localTransform.set(transform);
			
			if (addDebugShapes) {
				MeshPartBuilder joints = modelBuilder.part(
				    "joint_" + node.id,
				    GL20.GL_TRIANGLES,
				    VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
				    new Material(ColorAttribute.createDiffuse(Color.RED))
				);
				BoxShapeBuilder.build(joints, 0.1f, 0.1f, 0.1f);
				
				List<Integer> children = childrenLUT.get(i);
				if (!children.isEmpty()) {
					Vector3 p1 = new Vector3();
					MeshPartBuilder skeleton = modelBuilder.part(
						"skeleton_" + i, GL20.GL_LINES,
						VertexAttributes.Usage.Position,
						new Material(ColorAttribute.createDiffuse(Color.YELLOW))
					);
					for (Integer childIndex : children) {
						Matrix4 child = transforms[childIndex];
			            Vector3 p2 = new Vector3();
			            child.getTranslation(p2);
			            skeleton.line(p1, p2);
					}
				}
			}
			
			nodes[i] = node;
		}
		
		//Set node hierarchy
		Node root = null;
		for (int i = 0; i < bones.size(); i++) {
			L3DBone bone = bones.get(i);
			Node node = nodes[i];
			if (bone.parent == -1) {
				root = node;
			} else {
				Node parent = nodes[bone.parent];
				parent.addChild(node);
			}
		}
		
		if (root != null) {
			calculateGlobalTransforms(root);
		}
		
		return nodes;
	}
	
	private static void calculateGlobalTransforms(Node node) {
		if (node.hasParent()) {
			Node parent = node.getParent();
			Matrix4 global = new Matrix4(parent.globalTransform);
			global.mul(node.localTransform);
			node.globalTransform.set(global);
		} else {
			node.globalTransform.set(node.localTransform);
		}
		for (Node child : node.getChildren()) {
			calculateGlobalTransforms(child);
		}
	}
	
	@Override
	public void dispose() {
		/*for (BillboardQuadAsset asset : spriteAssets.values()) {
			asset.dispose();
		}
		spriteAssets.clear();*/
		//
		for (Model model : models.values()) {
			model.dispose();
		}
		models.clear();
		modelsInfo.clear();
		//
		for (Texture texture : rawTextures.values()) {
			texture.dispose();
		}
		rawTextures.clear();
		//
		for (Texture texture : textures.values()) {
			texture.dispose();
		}
		textures.clear();
		//
		for (Map<Integer, Texture> l3dTextures : l3dsTextures.values()) {
			if (l3dTextures != null) {
				for (Texture texture : l3dTextures.values()) {
					texture.dispose();
				}
				l3dTextures.clear();
			}
		}
		l3dsTextures.clear();
	}
	
	
	private static class MaterialTypeAttr {
		public final boolean depthWrite;
		public final boolean alphaTest;
		public final BlendMode blendMode;
		public final boolean modulateAlpha;
		public final boolean thresholdAlpha;
		
		public MaterialTypeAttr(boolean depthWrite, boolean alphaTest, BlendMode blendMode, boolean modulateAlpha, boolean thresholdAlpha) {
			this.depthWrite = depthWrite;
			this.alphaTest = alphaTest;
			this.blendMode = blendMode;
			this.modulateAlpha = modulateAlpha;
			this.thresholdAlpha = thresholdAlpha;
		}
	}
	
	
	private static enum BlendMode {
		Disabled(false, 0, 0),
		Standard(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA),
		Additive(true, GL20.GL_SRC_ALPHA, GL20.GL_DST_ALPHA);	// is this correct?
		
		public final boolean blend;
		public final int srcFunc;
		public final int dstFunc;
		
		private BlendMode(boolean blend, int srcFunc, int dstFunc) {
			this.blend = blend;
			this.srcFunc = srcFunc;
			this.dstFunc = dstFunc;
		}
	}
	
	
	private static class ModelKey {
		private final Command command;
		public final Object type;
		private final L3DFile l3d;
		
		public ModelKey(Command command, @Null Object type, @Null L3DFile l3d) {
			this.command = command;
			this.type = type;
			this.l3d = l3d;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(command, l3d);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ModelKey)) return false;
			ModelKey other = (ModelKey)obj;
			if (this.command != other.command) return false;
			if (!Objects.equals(this.l3d, other.l3d)) return false;
			if (!Objects.equals(this.type, other.type)) return false;
			return true;
		}
		
		@Override
		public String toString() {
			return command.name() + " " + type + " " + l3d;
		}
	}
	
	
	public static class ModelInfo {
		public final String name;
		public final L3DFile l3d;
		public final Model model;
		private boolean isSprite;
		public final Object info;
		public final Command command;
		public final Object type;
		
		private int age;
		
		public int spriteOffset;
		public int frameCount;
		public float frameInterval;
		
		public ModelInfo(L3DFile l3d, Model model, @Null Object info, @Null Command command, @Null Object type) {
			this.l3d = l3d;
			this.model = model;
			this.info = info;
			this.command = command;
			this.type = type;
			this.name = getName(l3d, command, type);
		}
		
		public boolean isVirtual() {
			return type == VIRTUAL_TYPE;
		}
		
		public boolean isSprite() {
			return isSprite;
		}
		
		public int getAge() {
			return age;
		}
		
		public boolean isAdult() {
			return age >= ((GVillagerInfo)info).grownUpAge;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(command, l3d, type);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ModelInfo)) return false;
			ModelInfo other = (ModelInfo)obj;
			if (this.command != other.command) return false;
			if (!Objects.equals(this.l3d, other.l3d)) return false;
			if (!Objects.equals(this.type, other.type)) return false;
			return true;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		private static String getName(L3DFile l3d, Command command, Object type) {
			if (command == Command.CREATE_NEW_TOWN_FIELD) {
				return "Field";
			} else if (command != null && (type == null || type == VIRTUAL_TYPE)) {
				return command.objectDisplayName;
			} else if (command != null && command.type >= 0) {
				Argument arg = command.args[command.type];
				Class<?> enumClass = arg.getEffectiveType().enumClass;
				if (enumClass != null) {
					String s;
					if (arg.type == ArgType.INT) {
						s = enumClass.getEnumConstants()[(Integer)type].toString();
					} else {
						s = type.toString();
					}
					return command.objectDisplayName + " " + s.replaceAll("_", " ").toLowerCase();
				}
			}
			String s = l3d.toString()
				    .replaceAll("([a-z])([A-Z])", "$1 $2")
				    .replaceAll("([A-Za-z])(\\d)", "$1 $2")
				    .replaceAll("(\\d)([A-Za-z])", "$1 $2")
				    .replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2");
			return s;
		}
	}
	
	
	private static String snakeToCamel(String snake) {
		String[] parts = snake.toLowerCase().split("_");
	    StringBuilder result = new StringBuilder(parts[0]);
	    for (int i = 1; i < parts.length; i++) {
	        if (!parts[i].isEmpty()) {
	            result.append(Character.toUpperCase(parts[i].charAt(0)))
	                  .append(parts[i].substring(1));
	        }
	    }
	    return result.toString();
	}
	
	
	private static Model createSprite(TextureRegion colorTexture, TextureRegion alphaTexture, Color color, float width, float height) {
        ModelBuilder builder = new ModelBuilder();

        Material mat = new Material();
        mat.set(TextureAttribute.createDiffuse(alphaTexture));
        mat.set(TextureAttribute.createEmissive(colorTexture));
        mat.set(ColorAttribute.createEmissive(color.r, color.g, color.b, 0.5f));
        mat.set(IntAttribute.createCullFace(0));
        mat.set(new BlendingAttribute(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, color.a));
        
        DepthTestAttribute depth = new DepthTestAttribute(GL20.GL_LEQUAL);
        depth.depthMask = false;
        mat.set(depth);
        
    	Model model = builder.createRect(
            -width / 2f, -height / 2f, 0f,
             width / 2f, -height / 2f, 0f,
             width / 2f,  height / 2f, 0f,
            -width / 2f,  height / 2f, 0f,
            0f, 0f, 1f,
            mat,
            VertexAttributes.Usage.Position |
            VertexAttributes.Usage.Normal |
            VertexAttributes.Usage.TextureCoordinates
    	);
    	model.nodes.get(0).id = "status_0";
    	return model;
    }
}
