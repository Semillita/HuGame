package dev.hugame.graphics.model;

import static org.lwjgl.assimp.Assimp.*;

import java.awt.Dimension;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFile;
import org.lwjgl.assimp.AIFileIO;
import org.lwjgl.assimp.AIFileOpenProcI;
import org.lwjgl.assimp.AIFileSeekI;
import org.lwjgl.assimp.AILogStreamCallback;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AITexture;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.system.MemoryUtil;

import dev.hugame.core.HuGame;
import dev.hugame.graphics.ImageData;
import dev.hugame.graphics.Texture;
import dev.hugame.graphics.Textures;
import dev.hugame.util.Files;
import dev.hugame.util.ImageLoader;

public class AssimpModelLoader {

	private final AIFileIO fileSystem;

	private ByteBuffer currentFileBuffer;
	private Map<String, ByteBuffer> fileContentByName;
	private String currentFileName;
	private Stack<LoadedFile> fileStack;

	public AssimpModelLoader() {
		this.fileContentByName = new HashMap<>();
		this.fileStack = new Stack<>();
		var start = System.nanoTime();
		this.fileSystem = createFileSystem();
		var elapsed = System.nanoTime() - start;
		System.out.println("TIME TO CREATE FILE SYSTEM: " + elapsed / 1_000_000_000d + " seconds");
	}

	/** Does not work for gltf and glb files */
	public AssimpModel load(String filepath) {
//		 Only works for real files, not ones zipped into a jar
//		var absoluteFilepath = new File(AssimpModelLoader.class.getClassLoader().getResource(filepath).getFile()).getAbsolutePath();
//		var scene = aiImportFile(absoluteFilepath, aiProcess_Triangulate | aiProcess_FlipUVs);
		return load(getInternalSceneLoader(), filepath);
	}
	
	public AssimpModel loadSingleFile(String filePath) {
		return load(getSingleFileInternalSceneLoader(), filePath);
	}

	public AssimpModel loadExternal(String filePath) {
		return load(getExternalFileSceneLoader(), filePath);
	}
	
	private AssimpModel load(SceneLoader sceneLoader, String filePath) {
		var scene = sceneLoader.load(filePath);

		if (scene == null) {
			System.err.println("Failed to load scene");
			System.err.println(aiGetErrorString());
			return null;
		}

		var meshesInScene = getMeshesInScene(scene);

		var assimpMeshes = getMeshesInNode(scene.mRootNode(), meshesInScene, 0, Optional.empty());
		var embeddedTextures = getEmbeddedTextures(scene);
		var texturesInScene = new ArrayList<Texture>();
		var assimpMaterials = getMaterials(scene, embeddedTextures, texturesInScene);
//		var meshesInScene = getMeshesInScene(scene);
		System.out.println("Amount of materials: " + assimpMaterials.size());
		
		return new AssimpModel(assimpMeshes, assimpMaterials, List.of());
	}
	
	private List<Texture> getEmbeddedTextures(AIScene scene) {
		var texturePointers = scene.mTextures();
		if (texturePointers == null) {
			return List.of();
		}

		var textures = new ArrayList<Texture>();
		var textureAmount = texturePointers.limit();
		for (int i = 0; i < textureAmount; i++) {
			var aiTexture = AITexture.create(texturePointers.get(i));

			var width = aiTexture.mWidth();
			var height = aiTexture.mHeight();
			var filename = aiTexture.mFilename().dataString();
			var pcData = aiTexture.pcData();

			if (height <= 0) {
				var address = pcData.address();
				var dataBuffer = MemoryUtil.memByteBuffer(address, width);
				var image = ImageLoader.read(dataBuffer, 4);

				var fileBytes = new byte[dataBuffer.limit()];
				dataBuffer.get(fileBytes);
				MemoryUtil.memFree(dataBuffer);
				var texture = HuGame.getGraphics().getTexture(fileBytes);
				textures.add(texture);
				System.out.println(filename + ":");
				System.out.println(image);
			} else {
				throw new RuntimeException("Should not be happening, look into adding support!");
			}
		}
		
		Map<Dimension, Integer> textureSizes = new HashMap<>();
		for (var texture : textures) {
			var dim = new Dimension(texture.getWidth(), texture.getHeight());
			var amount = textureSizes.get(dim);

			if (amount != null) {
				textureSizes.put(dim, amount + 1);
			} else {
				textureSizes.put(dim, 1);
			}
		}

		System.out.println(textureSizes);

		return textures;
	}

	private List<AIMesh> getMeshesInScene(AIScene scene) {
		if (scene.mNumMeshes() <= 0) {
			return List.of();
		}

		var meshPointerBuffer = scene.mMeshes();
		var meshes = new ArrayList<AIMesh>();
		for (int i = 0; i < meshPointerBuffer.limit(); i++) {
			var mesh = AIMesh.create(meshPointerBuffer.get());
			meshes.add(mesh);
		}

		return meshes;
	}
	
	private List<AssimpMesh> getMeshesInNode(AINode node, List<AIMesh> meshesInScene, int indent, Optional<Matrix4f> maybeParentTransform) {
		var ind = "";
		for (int i = 0; i < indent; i++) {
			ind += "	";
		}
		
		System.out.println(ind + "Node");
		var aiTransform = node.mTransformation();
		var transform = toJomlMatrix(aiTransform);
		var parentTransform = maybeParentTransform.orElse(new Matrix4f().identity());
		transform.mul(parentTransform);
		
		var meshes = new ArrayList<AssimpMesh>();
		var meshesIndices = node.mMeshes();
		for (int i = 0; i < node.mNumMeshes(); i++) {
			System.out.println(ind + "	Mesh");
			var meshIndex = meshesIndices.get(i);
			
			var aiMesh = meshesInScene.get(meshIndex);
			var assimpMesh = createAssimpMesh(aiMesh, transform);
			
			meshes.add(assimpMesh);
		}
		
		var childrenPointers = node.mChildren();
		for (int i = 0; i < node.mNumChildren(); i++) {
			var childNode = AINode.create(childrenPointers.get(i));
			meshes.addAll(getMeshesInNode(childNode, meshesInScene, indent + 1, Optional.of(transform)));
		}
		
		return meshes;
	}
	
	private Matrix4f toJomlMatrix(AIMatrix4x4 aiMatrix) {
		var address = aiMatrix.address();
		var buffer = MemoryUtil.memFloatBuffer(address, 16);
		var matrix = new Matrix4f(buffer);
		matrix.transpose();
		
		return matrix;
		// TODO: Make this return new Materix4f(buffer).transpose();
	}

	private AssimpMesh createAssimpMesh(AIMesh mesh, Matrix4f transformation) {
		final var positions = new ArrayList<Vector3f>();
		final var normals = new ArrayList<Vector3f>();
		final var textureCoords = new ArrayList<Vector3f>();

//		System.out.println("(Creating assimp mesh)");
		
		var nPositions = mesh.mVertices();
		for (int i = 0; i < nPositions.limit(); i++) {
			var aiPosition = nPositions.get(i);
			var position = new Vector4f(aiPosition.x(), aiPosition.y(), aiPosition.z(), 1);
			position.mul(transformation);
			positions.add(new Vector3f(position.x, position.y, position.z));
		}

		// Vertex normals
		var nNormals = mesh.mNormals();
		for (int i = 0; i < nNormals.limit(); i++) {
			var normal = nNormals.get(i);
			normals.add(new Vector3f(normal.x(), normal.y(), normal.z()));
		}

		// Vertex texture coordinates
		var nTextureCoords = mesh.mTextureCoords(0);
		for (int i = 0; i < nTextureCoords.limit(); i++) {
			var coords = nTextureCoords.get(i);
			textureCoords.add(new Vector3f(coords.x(), coords.y(), coords.z()));
		}

		// Vertices
		final var vertices = new ArrayList<AssimpVertex>();
		for (int i = 0; i < positions.size(); i++) {
			vertices.add(new AssimpVertex(positions.get(i), normals.get(i), textureCoords.get(i)));
		}

		// Indices
		final var nFaces = mesh.mFaces();
		final var indices = new ArrayList<Integer>();
		for (int i = 0; i < mesh.mNumFaces(); i++) {
			var face = nFaces.get(i);

			for (int j = 0; j < face.mNumIndices(); j++) {
				indices.add(face.mIndices().get(j));
			}
		}
		
		return new AssimpMesh(vertices, indices, mesh.mMaterialIndex());
	}
	
	private List<AssimpMaterial> getMaterials(AIScene scene, List<Texture> embeddedTextures,
			List<Texture> texturesInScene) {
		var materials = new ArrayList<AssimpMaterial>();
		
		var numMaterials = scene.mNumMaterials();
		var nMaterials = scene.mMaterials();
		
		for (int i = 0; i < numMaterials; i++) {
			var aiMaterial = AIMaterial.create(nMaterials.get(i));
//			var diffuse = getDiffuseColor(aiMaterial);
			var material = getMaterial(scene, aiMaterial, embeddedTextures, texturesInScene);

			materials.add(material);
		}

		return materials;
	}

	private AssimpMaterial getMaterial(AIScene aiScene, AIMaterial aiMaterial, List<Texture> embeddedTextures,
			List<Texture> texturesInScene) {
		System.out.println("  getMaterial()");
		var albedoColor = getAlbedoColor(aiMaterial);

		var maybeAlbedoTextureIndex = getMaterialTextureIndex(aiScene, aiMaterial, TextureType.ALBEDO, embeddedTextures,
				texturesInScene);
		System.out.println("  -maybeAlbedoTextureIndex: " + maybeAlbedoTextureIndex);
		// TODO: Stop using embeddedTextures::get, should probably be some combined list
		// of those
		// and external textures in texturesInScene
		var maybeAlbedoMap = maybeAlbedoTextureIndex.map(texturesInScene::get);

		var maybeNormalTextureIndex = getMaterialTextureIndex(aiScene, aiMaterial, TextureType.NORMAL, embeddedTextures,
				texturesInScene);
		var maybeNormalMap = maybeNormalTextureIndex.map(texturesInScene::get);

		var maybeSpecularTextureIndex = getMaterialTextureIndex(aiScene, aiMaterial, TextureType.SPECULAR,
				embeddedTextures, texturesInScene);
		var maybeSpecularMap = maybeSpecularTextureIndex.map(texturesInScene::get);

		var material = new AssimpMaterial(Optional.ofNullable(albedoColor), maybeAlbedoMap, maybeNormalMap,
				maybeSpecularMap);

		return material;
	}

	// Should this method really be called that? What loades disk textures?
	private Optional<Integer> getMaterialTextureIndex(AIScene aiScene, AIMaterial aiMaterial, TextureType type,
			List<Texture> embeddedTextures, List<Texture> texturesInScene) {
		System.out.println("    getMaterialTextureIndices()");
		final var textureAmount = aiGetMaterialTextureCount(aiMaterial, type.getID());
		System.out.println("    -textureAmount: " + textureAmount);

		if (textureAmount == 0) {
			return Optional.empty();
		} else if (textureAmount > 1) {
			System.err.println("???????????????????? More than 1 texture of specific type in material??");
			return Optional.empty();
		}

		var nPath = AIString.create();
		aiGetMaterialTexture(aiMaterial, type.getID(), 0, nPath, (int[]) null, null, null, null, null, null);
		var path = nPath.dataString();

		if (path.startsWith("*")) {
			// This texture is embedded and has already been read
			var index = Integer.parseInt(path.substring(1));
			var texture = embeddedTextures.get(index);
			System.out.println("    |-Embedded texture with index " + index);

			var globalIndex = texturesInScene.size();
			texturesInScene.add(texture);
			return Optional.of(globalIndex);
		} else {
			// This texture is external
			System.out.println("    |-Load texture with path " + path);
			var texture = Textures.get("/" + path);
			System.out.println("    -texture: " + texture);

			var globalIndex = texturesInScene.size();
			texturesInScene.add(texture);
			return Optional.of(globalIndex);
		}

	}

	private AIFileIO createFileSystem() {
		return AIFileIO.create().OpenProc(this::openProc).CloseProc(this::closeProc);
	}

	private long openProc(long pFileIO, long pFileName, long openMode) {
		var fileName = MemoryUtil.memUTF8(pFileName);
		System.out.println(String.format("Opening [%s] with openMode %d", fileName, openMode));

		var file = getFile(fileName);
		fileStack.push(file);

		var data = file.fileContent();

		return AIFile.malloc().ReadProc((ignored, pBuffer, size, count) -> readProc(pBuffer, size, count, data))
				.SeekProc((ignored, offset, origin) -> seekProc(offset, origin, data))
				.FileSizeProc(ignored -> data.limit()).address();
	}

	private void closeProc(long pFileIO, long pFile) {
		var file = fileStack.pop();
		System.out.println("Closing " + file.fileName());
		System.out.println(aiGetErrorString());
		var aiFile = AIFile.create(pFile);
		aiFile.ReadProc().free();
		aiFile.SeekProc().free();
		aiFile.FileSizeProc().free();
		aiFile.free();
	}

	private long readProc(long pBuffer, long size, long count, ByteBuffer data) {
		System.out.println("    Reading " + fileStack.peek().fileName() + ": size = " + size + ", count = " + count);
		System.out.println("        Remaining: " + data.remaining() + ", size * count: " + size * count);
		System.out.println("        Position: " + data.position());
//		var amount = Math.min(data.remaining(), size * count);
		var amount = Math.min(data.remaining() / size, count);
		MemoryUtil.memCopy(MemoryUtil.memAddress(data), pBuffer, amount);
		data.position(data.position() + (int) (amount * size));
		return amount;
	}

	private int seekProc(long offset, long origin, ByteBuffer data) {
		System.out.println("    Seeking " + fileStack.peek().fileName());
		switch ((int) origin) {
		case Assimp.aiOrigin_CUR -> data.position(data.position() + (int) offset);
		case Assimp.aiOrigin_SET -> data.position((int) offset);
		case Assimp.aiOrigin_END -> data.position(data.limit() + (int) offset);
		}

		return 0;
	}

	private LoadedFile getFile(String fileName) {
		// TODO: Remove
		currentFileName = fileName;

		if (fileContentByName.containsKey(fileName)) {
			System.out.println("Using cached buffer for [" + fileName + "]");
			return new LoadedFile(fileName, fileContentByName.get(fileName));
		}

		System.out.println("Loading buffer for [" + fileName + "]");
		var maybeFileBytes = Files.readBytes("/" + fileName);
		if (maybeFileBytes.isEmpty()) {
			System.out.println("ERROR: File not found: [/" + fileName + "]");
		}
		var fileBytes = maybeFileBytes.orElseThrow();
		var fileContent = MemoryUtil.memCalloc(fileBytes.length);
		System.out.println("File size: " + fileContent.capacity());
		fileContent.put(fileBytes);
		fileContent.rewind();
		fileContentByName.put(fileName, fileContent);

		return new LoadedFile(fileName, fileContent);
	}

	private Optional<AIScene> loadScene(String fileName, AIFileIO fileSystem) {
		var scene = aiImportFileEx(fileName, aiProcess_Triangulate | aiProcess_FlipUVs, fileSystem);
		System.out.println("Generated scene: " + scene);
		// Shouldn't close these, as they might be used later for another model
		// fileSystem.OpenProc().free();
		// fileSystem.CloseProc().free();

		fileContentByName.values().forEach(MemoryUtil::memFree);
		fileContentByName.clear();

		return Optional.ofNullable(scene);
	}

	private Vector3f getAlbedoColor(AIMaterial aiMaterial) {
		var pKey = AI_MATKEY_COLOR_DIFFUSE;
		var type = aiTextureType_NONE;
		var aiDiffuse = AIColor4D.create();
		aiGetMaterialColor(aiMaterial, pKey, type, 0, aiDiffuse);
		
		return new Vector3f(aiDiffuse.r(), aiDiffuse.g(), aiDiffuse.b());
	}

	private static enum TextureType {
		ALBEDO(aiTextureType_DIFFUSE), NORMAL(aiTextureType_NORMALS), SPECULAR(aiTextureType_SPECULAR);

		private final int id;

		TextureType(int id) {
			this.id = id;
		}

		public int getID() {
			return id;
		}
	}

	private static record LoadedFile(String fileName, ByteBuffer fileContent) {
	}
	
	private static interface SceneLoader {
		public AIScene load(String filePath);
	}
	
	
	private SceneLoader getInternalSceneLoader() {
		return filePath -> loadScene(filePath, fileSystem).orElse(null);
	}
	
	private SceneLoader getSingleFileInternalSceneLoader() {
		return filePath -> aiImportFileFromMemory(getFileContentBuffer(filePath), aiProcess_Triangulate | aiProcess_FlipUVs, filePath.substring(filePath.lastIndexOf(".") + 1));
	}
	
	private SceneLoader getExternalFileSceneLoader() {
		return filePath -> aiImportFile(getAbsoluteFilePath(filePath), aiProcess_Triangulate | aiProcess_FlipUVs);
	}
	
	private String getAbsoluteFilePath(String filePath) {
		return new File(AssimpModelLoader.class.getClassLoader().getResource(filePath).getFile())
				.getAbsolutePath();
	}
	
	private static ByteBuffer getFileContentBuffer(String filePath) {
		var fileBytes = Files.readBytes(filePath).orElseThrow();
		var buffer = BufferUtils.createByteBuffer(fileBytes.length);
		buffer.put(fileBytes);
		buffer.flip();
		
		return buffer;
	}
}
