package dev.hugame.desktop.gl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.joml.Vector3f;
import org.lwjgl.opengl.GLDebugMessageCallbackI;

import dev.hugame.core.Renderer;
import dev.hugame.desktop.gl.buffer.DirectionalLightBuffer;
import dev.hugame.desktop.gl.buffer.MaterialBuffer;
import dev.hugame.desktop.gl.buffer.PointLightBuffer;
import dev.hugame.desktop.gl.buffer.SpotLightBuffer;
import dev.hugame.environment.DirectionalLight;
import dev.hugame.environment.Environment;
import dev.hugame.environment.PointLight;
import dev.hugame.environment.SpotLight;
import dev.hugame.graphics.GLUtils;
import dev.hugame.graphics.InstanceData;
import dev.hugame.graphics.PerspectiveCamera;
import dev.hugame.graphics.Shader;
import dev.hugame.graphics.Shaders;
import dev.hugame.graphics.material.Material;
import dev.hugame.graphics.material.Materials;
import dev.hugame.graphics.model.Model;
import dev.hugame.util.Files;
import dev.hugame.util.Transform;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL43.*;

/** Renderer master class for rendering all different components */
public class GLRenderer implements Renderer {

	private Map<Model, List<InstanceData>> modelInstanceData;
	private PerspectiveCamera camera;
	private Shader instanceShader;
	private Shader batchShader;

	private MaterialBuffer matBuffer;
	private PointLightBuffer pointLightBuffer;
	private SpotLightBuffer spotLightBuffer;
	private DirectionalLightBuffer directionalLightBuffer;
	
	private boolean initialized = false;
	
	public GLRenderer() {
		modelInstanceData = new HashMap<>();
		camera = new PerspectiveCamera(new Vector3f(200f, 200f, 200f));
		camera.lookAt(new Vector3f(0, 0, 0));
		camera.update();
		instanceShader = Shaders.get(Files.read("/shaders/instance_vertex_shader.glsl").get(),
				Files.read("/shaders/instance_fragment_shader.glsl").get()).get();
		batchShader = Shaders.get(Files.read("/shaders/batch_vertex_shader.glsl").get(),
				Files.read("/shaders/batch_fragment_shader.glsl").get()).get();

		pointLightBuffer = PointLightBuffer.allocateNew(10);
		spotLightBuffer = SpotLightBuffer.allocateNew(10);
		directionalLightBuffer = DirectionalLightBuffer.allocateNew(1);

		// TODO: Extract into separate class for convenience
		glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> {
			var buffer = MemoryUtil.memByteBuffer(message, length);
			var messageString = MemoryUtil.memUTF8(buffer);

			System.out.println("OpenGL error: " + messageString);
		}, 0);
		glEnable(GL_DEBUG_OUTPUT);

		glEnable(GL_BLEND);
	}
	
	@Override
	public void create() {
		if (initialized) {
			return;	
		}
		
		initialized = true;
		
		var materials = Materials.collect();
		System.out.println("Collected " + materials.size() + " materials");
		matBuffer = MaterialBuffer.createFrom(materials);
	}

	@Override
	@Deprecated
	public void draw(Model model, Transform transform, /*Unused*/Material material) {
//		var instanceData = new InstanceData(transform, material);
		draw(model, transform);
	}
	
	// We should actually support drawing a model with some random material, like wood
	@Override
	public void draw(Model model, Transform transform) {
//		var instanceData = new InstanceData(transform, null);
		var instanceData = new InstanceData(transform);

		var instanceDataList = modelInstanceData.get(model);
		if (instanceDataList == null) {
			instanceDataList = new ArrayList<>();
			modelInstanceData.put(model, instanceDataList);
		}
		instanceDataList.add(instanceData);
	}

	@Override
	public void flush() {
		System.out.println("Flushing renderer");
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_MULTISAMPLE);

		for (var entry : modelInstanceData.entrySet()) {
			System.out.println("Rendering model");
			var model = entry.getKey();
			var instanceDataList = entry.getValue();

			var instanceDataArray = createInstanceDataArray(instanceDataList);
			var vaoID = model.getVAO();
			var vboID = model.getInstaceVBO();
			var eboID = model.getEBO();
			// TODO: Add casting to model::getTextures<T>
			var textures = model.getTextures()
					.stream()
					.map(GLTexture.class::cast)
					.map(GLTexture::getTextureArray)
					.toList();

			GLUtils.fillVBO(vboID, instanceDataArray);

			instanceShader.uploadVec3("cameraPosition", camera.getPosition());

			GLUtils.uploadMatricesToShader(camera, instanceShader);
			
			activateAndBindTextures(textures);
			uploadTexturesToShader(getTextureSlotArray(textures.size()), instanceShader);

			glBindVertexArray(vaoID);
			enableVertexAttribArrays(0, 1, 2, 3, 4, 5, 6, 7, 8);

			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);

			matBuffer.bindBase(0);
			pointLightBuffer.bindBase(1);
			spotLightBuffer.bindBase(2);
			directionalLightBuffer.bindBase(3);
			System.out.println("glDrawElementsInstanced start");
			glDrawElementsInstanced(GL_TRIANGLES, model.getIndexAmount(), GL_UNSIGNED_INT, 0, instanceDataList.size());
			System.out.println("glDrawElementsInstanced stop");
			disableVertexAttribArrays(0, 1, 2, 3, 4, 5, 6, 7, 8);
			glBindVertexArray(0);
			unbindTextureArrays();

			instanceShader.detach();

			instanceDataList.clear();

			camera.update();
		}
		System.out.println("Done flushing renderer");
	}

	@Override
	public PerspectiveCamera getCamera() {
		return camera;
	}

	@Override
	public void updateEnvironment(Environment environment) {
		System.out.println("- Point light");
		fillPointLightBuffer(environment.getPointLights());
		System.out.println("- Spot light");
		fillSpotLightBuffer(environment.getSpotLights());
		System.out.println("- Directional light");
		fillDirectionalLightBuffer(environment.getDirectionalLights());
		System.out.println("Done");
	}
	
	public void renderBatch(GLBatch batch) {
		glDisable(GL_DEPTH_TEST);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		var textures = batch.getTextures();
		
		GLUtils.fillVBO(batch.getVboID(), batch.getVertices());
		batchShader.use();
		GLUtils.uploadMatricesToShader(batch.getCamera(), batchShader);
		
		activateAndBindTextures(batch.getTextures());
		uploadTexturesToShader(getTextureSlotArray(textures.size() + 1), batchShader);

		glBindVertexArray(batch.getVaoID());
		enableVertexAttribArrays(0, 1);

		glDrawElements(GL_TRIANGLES, textures.size() * 6, GL_UNSIGNED_INT, 0);

		disableVertexAttribArrays(0, 1);

		glBindVertexArray(0);

		unbindTextureArrays();
		batchShader.detach();
	}

	private void activateAndBindTextures(List<GLTextureArray> textures) {
		// TODO: Should this change based on switching to texture arrays?
		for (int textureIndex = 0; textureIndex < textures.size(); textureIndex++) {
			glActiveTexture(GL_TEXTURE0 + textureIndex);
			textures.get(textureIndex).bind();
		}
	}

	private void fillPointLightBuffer(List<PointLight> lights) {
		if (pointLightBuffer.getMaxItems() >= lights.size()) {
			pointLightBuffer.refill(lights);
		} else {
			pointLightBuffer.fill(lights);
		}
		
		instanceShader.uploadInt("pointLightAmount", lights.size());
	}
	
	private void fillSpotLightBuffer(List<SpotLight> lights) {
		if (spotLightBuffer.getMaxItems() >= lights.size()) {
			spotLightBuffer.refill(lights);
		} else {
			spotLightBuffer.fill(lights);
		}
		
		instanceShader.uploadInt("spotLightAmount", lights.size());
	}
	
	private void fillDirectionalLightBuffer(List<DirectionalLight> lights) {
		if (directionalLightBuffer.getMaxItems() >= lights.size()) {
			directionalLightBuffer.refill(lights);
		} else {
			directionalLightBuffer.fill(lights);
		}
		
		instanceShader.uploadInt("directionalLightAmount", lights.size());
	}

	private void uploadTexturesToShader(int[] textureSlots, Shader shader) {
		shader.uploadTextureArray("uTextures", textureSlots);
	}

	private void unbindTextureArrays() {
		glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
	}

	private void enableVertexAttribArrays(int... arrays) {
		for (var i : arrays) {
			glEnableVertexAttribArray(i);
		}
	}

	private void disableVertexAttribArrays(int... arrays) {
		for (var i : arrays) {
			glDisableVertexAttribArray(i);
		}
	}

	private int[] getTextureSlotArray(int size) {
		return IntStream.range(0, size).toArray();
	}

	private float[] createInstanceDataArray(List<InstanceData> instanceData) {
		var instanceDataArray = new float[instanceData.size() * 16];
		for (int i = 0; i < instanceData.size(); i++) {
			var instance = instanceData.get(i);
			instance.transform().getMatrix().get(instanceDataArray, i * 16);
		}
		return instanceDataArray;
	}

}
