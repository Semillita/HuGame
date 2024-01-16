package dev.hugame.assimp;

import dev.hugame.core.HuGame;
import dev.hugame.graphics.Texture;
import dev.hugame.io.FileHandle;
import dev.hugame.io.FileLocation;
import dev.hugame.model.spec.*;
import dev.hugame.util.Files;
import dev.hugame.util.ImageLoader;
import dev.hugame.util.TextureLoader;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.*;
import java.util.stream.IntStream;

import static org.lwjgl.assimp.Assimp.*;

public class AssimpModelLoader extends ModelLoader {

	private final TextureLoader textureLoader;
	private final AIFileIO fileSystem;

	private final Map<String, ByteBuffer> fileContentByName;
	private final Stack<LoadedFile> fileStack;

	public AssimpModelLoader(TextureLoader textureLoader) {
		this.textureLoader = textureLoader;
		this.fileContentByName = new HashMap<>();
		this.fileStack = new Stack<>();
		var start = System.nanoTime();
		this.fileSystem = createFileSystem();
		var elapsed = System.nanoTime() - start;
	}

	@Override
	public boolean supports(String modelFormat, FileLocation fileLocation) {
		return true;
	}

	@Override
	public Optional<ResolvedModel> load(FileHandle file) {
		return switch (file.location()) {
			case INTERNAL -> Optional.ofNullable(load(file.path()));
			case EXTERNAL -> Optional.ofNullable(loadExternal(file.path()));
		};
	}

	/** Does not work for gltf and glb files */
	public ResolvedModel load(String filepath) {
		return load(getInternalSceneLoader(), filepath);
	}
	
	public ResolvedModel loadSingleFile(String filePath) {
		return load(getSingleFileInternalSceneLoader(), filePath);
	}

	public ResolvedModel loadExternal(String filePath) {
		return load(getExternalFileSceneLoader(), filePath);
	}
	
	private ResolvedModel load(SceneLoader sceneLoader, String filePath) {
		var scene = sceneLoader.load(filePath);

		if (scene == null) {
			System.err.println("Failed to load scene");
			System.err.println(aiGetErrorString());
			return null;
		}

		var aiMeshesInScene = getMeshesInScene(scene);

		var meshes = getMeshesInNode(scene.mRootNode(), aiMeshesInScene, 0, Optional.empty());
		var embeddedTextures = getEmbeddedTextures(scene);
		var texturesInScene = new ArrayList<Texture>();
		var materials = getMaterialsInScene(scene, embeddedTextures, texturesInScene);
//		var meshesInScene = getMeshesInScene(scene);
		
		return new ResolvedModel(meshes, materials);
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

				var fileBytes = new byte[dataBuffer.limit()];
				dataBuffer.get(fileBytes);
				MemoryUtil.memFree(dataBuffer);
				var texture = textureLoader.get(fileBytes);
				textures.add(texture);
			} else {
				throw new RuntimeException("Should not be happening, look into adding support!");
			}
		}

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
	
	private List<ResolvedMesh> getMeshesInNode(AINode node, List<AIMesh> meshesInScene, int indent, Optional<Matrix4f> maybeParentTransform) {
		var ind = "";
		for (int i = 0; i < indent; i++) {
			ind += "	";
		}
		
		var aiTransform = node.mTransformation();
		var transform = toJomlMatrix(aiTransform);
		var parentTransform = maybeParentTransform.orElse(new Matrix4f().identity());
		transform.mul(parentTransform);
		
		var meshes = new ArrayList<ResolvedMesh>();
		var meshesIndices = node.mMeshes();
		for (int i = 0; i < node.mNumMeshes(); i++) {
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

		return new Matrix4f(buffer).transpose();
	}

	private Vector3f copy(Vector3f source) {
		return new Vector3f(source.x, source.y, source.z);
	}

	private ResolvedMesh createAssimpMesh(AIMesh mesh, Matrix4f transformation) {
		final var positions = new ArrayList<Vector3f>();
		final var normals = new ArrayList<Vector3f>();
		final var textureCoords = new ArrayList<Vector2f>();

		var nPositions = mesh.mVertices();
		for (int i = 0; i < nPositions.limit(); i++) {
			var aiPosition = nPositions.get(i);
			var position = new Vector4f(aiPosition.x(), aiPosition.y(), aiPosition.z(), 1);
			position.mul(transformation);
			positions.add(new Vector3f(position.x, position.y, position.z));
		}

		// Vertex normals
		var nNormals = mesh.mNormals();
		if (nNormals != null) {
			for (int i = 0; i < nNormals.limit(); i++) {
				var normal = nNormals.get(i);
				normals.add(new Vector3f(normal.x(), normal.y(), normal.z()));
			}
		} else {
			for (int i = 0; i < (nPositions.limit() / 3); i++) {
				var p0 = positions.get(i * 3);
				var p1 = positions.get(i * 3 + 1);
				var p2 = positions.get(i * 3 + 2);

				var position0 = copy(p0);
				var position1 = copy(p1);
				var position2 = copy(p2);

				var a = position1.sub(position0);
				var b = position2.sub(position0);

				var normal = a.cross(b);

				normals.add(normal);
				normals.add(normal);
				normals.add(normal);
			}
		}

		// Vertex texture coordinates
		var nTextureCoords = mesh.mTextureCoords(0);
		for (int i = 0; i < nTextureCoords.limit(); i++) {
			var coords = nTextureCoords.get(i);
			textureCoords.add(new Vector2f(coords.x(), coords.y()));
		}

		// Vertices
		final var vertices = new ArrayList<ResolvedVertex>();
		for (int i = 0; i < positions.size(); i++) {
			vertices.add(new ResolvedVertex(positions.get(i), normals.get(i), textureCoords.get(i)));
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
		
		return new ResolvedMesh(vertices, indices, mesh.mMaterialIndex());
	}
	
	private List<ResolvedMaterial> getMaterialsInScene(AIScene scene, List<Texture> embeddedTextures,
													   List<Texture> texturesInScene) {
		var materials = new ArrayList<ResolvedMaterial>();
		
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

	private ResolvedMaterial getMaterial(AIScene aiScene, AIMaterial aiMaterial, List<Texture> embeddedTextures,
			List<Texture> texturesInScene) {
		var albedoColor = getAlbedoColor(aiMaterial);

		var maybeAlbedoTextureIndex = getMaterialTextureIndex(aiScene, aiMaterial, TextureType.ALBEDO, embeddedTextures,
				texturesInScene);
		// TODO: Stop using embeddedTextures::get, should probably be some combined list
		//  of those and external textures in texturesInScene
		var maybeAlbedoMap = maybeAlbedoTextureIndex.map(texturesInScene::get);

		var maybeNormalTextureIndex = getMaterialTextureIndex(aiScene, aiMaterial, TextureType.NORMAL, embeddedTextures,
				texturesInScene);
		var maybeNormalMap = maybeNormalTextureIndex.map(texturesInScene::get);

		var maybeSpecularTextureIndex = getMaterialTextureIndex(aiScene, aiMaterial, TextureType.SPECULAR,
				embeddedTextures, texturesInScene);
		var maybeSpecularMap = maybeSpecularTextureIndex.map(texturesInScene::get);

		var material = new ResolvedMaterial(Optional.ofNullable(albedoColor), maybeAlbedoMap, maybeNormalMap,
				maybeSpecularMap);

		return material;
	}

	// Should this method really be called that? What loades disk textures?
	private Optional<Integer> getMaterialTextureIndex(AIScene aiScene, AIMaterial aiMaterial, TextureType type,
			List<Texture> embeddedTextures, List<Texture> texturesInScene) {
		final var textureAmount = aiGetMaterialTextureCount(aiMaterial, type.getID());

		if (textureAmount == 0) {
			return Optional.empty();
		} else if (textureAmount > 1) {
			return Optional.empty();
		}

		var nPath = AIString.create();
		aiGetMaterialTexture(aiMaterial, type.getID(), 0, nPath, (int[]) null, null, null, null, null, null);
		var path = nPath.dataString();

		if (path.startsWith("*")) {
			// This texture is embedded and has already been read
			var index = Integer.parseInt(path.substring(1));
			var texture = embeddedTextures.get(index);

			var globalIndex = texturesInScene.size();
			texturesInScene.add(texture);
			return Optional.of(globalIndex);
		} else {
			// This texture is external
			var texture = textureLoader.get("/" + path);

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
		var file = getFile(fileName);
		fileStack.push(file);

		var data = file.fileContent();

		return AIFile.malloc().ReadProc((ignored, pBuffer, size, count) -> readProc(pBuffer, size, count, data))
				.SeekProc((ignored, offset, origin) -> seekProc(offset, origin, data))
				.FileSizeProc(ignored -> data.limit()).address();
	}

	private void closeProc(long pFileIO, long pFile) {
		var file = fileStack.pop();
		var errorString = aiGetErrorString();
		if (errorString != null && !errorString.isEmpty()) {
			System.out.println("Error: " + errorString);
		}
		var aiFile = AIFile.create(pFile);
		aiFile.ReadProc().free();
		aiFile.SeekProc().free();
		aiFile.FileSizeProc().free();
		aiFile.free();
	}

	private long readProc(long pBuffer, long size, long count, ByteBuffer data) {
		var amount = Math.min(data.remaining(), size * count);
		MemoryUtil.memCopy(MemoryUtil.memAddress(data), pBuffer, amount);
		data.position(data.position() + (int) (amount * size));
		return amount;
	}

	private int seekProc(long offset, long origin, ByteBuffer data) {
		switch ((int) origin) {
		case Assimp.aiOrigin_CUR -> data.position(data.position() + (int) offset);
		case Assimp.aiOrigin_SET -> data.position((int) offset);
		case Assimp.aiOrigin_END -> data.position(data.limit() + (int) offset);
		}

		return 0;
	}

	private LoadedFile getFile(String fileName) {
		if (fileContentByName.containsKey(fileName)) {
			return new LoadedFile(fileName, fileContentByName.get(fileName));
		}

		var maybeFileBytes = Files.readBytes("/" + fileName);
		if (maybeFileBytes.isEmpty()) {
			System.out.println("ERROR: File not found: [/" + fileName + "]");
		}
		var fileBytes = maybeFileBytes.orElseThrow();
		var fileContent = MemoryUtil.memCalloc(fileBytes.length);
		fileContent.put(fileBytes);
		fileContent.rewind();
		fileContentByName.put(fileName, fileContent);

		return new LoadedFile(fileName, fileContent);
	}

	private Optional<AIScene> loadScene(String fileName, AIFileIO fileSystem) {
		var scene = aiImportFileEx(fileName, aiProcess_Triangulate | aiProcess_FlipUVs, fileSystem);

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

	private record LoadedFile(String fileName, ByteBuffer fileContent) {
	}
	
	private interface SceneLoader {
		AIScene load(String filePath);
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
