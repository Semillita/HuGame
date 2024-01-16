package dev.hugame.desktop.gl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import dev.hugame.desktop.gl.model.OpenGLModel;
import dev.hugame.desktop.gl.shader.OpenGLShader;
import dev.hugame.desktop.gl.shader.ShaderFactory;
import org.joml.Vector3f;

import dev.hugame.core.Renderer;
import dev.hugame.desktop.gl.buffer.DirectionalLightBuffer;
import dev.hugame.desktop.gl.buffer.MaterialBuffer;
import dev.hugame.desktop.gl.buffer.PointLightBuffer;
import dev.hugame.desktop.gl.buffer.SpotLightBuffer;
import dev.hugame.environment.DirectionalLight;
import dev.hugame.environment.Environment;
import dev.hugame.environment.PointLight;
import dev.hugame.environment.SpotLight;
import dev.hugame.graphics.InstanceData;
import dev.hugame.graphics.PerspectiveCamera;
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
	private final GLGraphics graphics;
	private final Map<Model, List<InstanceData>> modelInstanceData;
	private final PerspectiveCamera camera;

	// TODO: Implement support for custom shaders
	private final OpenGLShader modelShader;
	private final OpenGLShader quadShader;

	private MaterialBuffer matBuffer;
	private PointLightBuffer pointLightBuffer;
	private SpotLightBuffer spotLightBuffer;
	private DirectionalLightBuffer directionalLightBuffer;
	
	private boolean initialized = false;
	
	public GLRenderer(GLGraphics graphics) {
		this.graphics = graphics;

		modelInstanceData = new HashMap<>();
		camera = new PerspectiveCamera(new Vector3f(200f, 200f, 200f));
		camera.lookAt(new Vector3f(0, 0, 0));
		camera.update();

		var shaderFactory = new ShaderFactory();
		var modelVertexSource = Files.read("/shaders/opengl_model_vertex_shader.glsl").orElseThrow();
		var modelFragmentSource = Files.read("/shaders/opengl_model_fragment_shader.glsl").orElseThrow();
		var quadVertexSource = Files.read("/shaders/opengl_quad_vertex_shader.glsl").orElseThrow();
		var quadFragmentSource = Files.read("/shaders/opengl_quad_fragment_shader.glsl").orElseThrow();

		modelShader = shaderFactory.createShader(modelVertexSource, modelFragmentSource).orElseThrow();
		quadShader = shaderFactory.createShader(quadVertexSource, quadFragmentSource).orElseThrow();

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
		matBuffer = MaterialBuffer.createFrom(materials);
	}

	@Override
	public void beginFrame() {
		var clearColor = graphics.getClearColor();
		glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}

	@Override
	public void endFrame() {
		// No need to do anything
	}

	@Override
	@Deprecated
	public void draw(Model model, Transform transform, /*Unused*/Material material) {
		draw(model, transform);
	}
	
	// We should actually support drawing a model with some random material, like wood
	@Override
	public void draw(Model model, Transform transform) {
		var instanceData = new InstanceData(transform);

		var instanceDataList = modelInstanceData.computeIfAbsent(model, ignored -> new ArrayList<>());
		instanceDataList.add(instanceData);
	}

	@Override
	public void flush() {
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_MULTISAMPLE);

		for (var entry : modelInstanceData.entrySet()) {
			var model = entry.getKey();
			var instanceDataList = entry.getValue();

			if (!(model instanceof OpenGLModel glModel)) {
				// TODO: Make a utility service for getting
				throw new RuntimeException("Invalid type of Model: " + model.getClass());
			}

			var instanceDataArray = createInstanceDataArray(instanceDataList);
			var vaoID = glModel.getVAO();
			var vboID = glModel.getInstaceVBO();
			var eboID = glModel.getEBO();
			// TODO: Add casting to model::getTextures<T>
			var textures = glModel.getTextures()
					.stream()
					.map(GLTexture.class::cast)
					.map(GLTexture::getTextureArray)
					.toList();

			GLUtils.fillVBO(vboID, instanceDataArray);

			modelShader.uploadVec3("cameraPosition", camera.getPosition());

			GLUtils.uploadMatricesToShader(camera, modelShader);
			
			activateAndBindTextures(textures);
			uploadTexturesToShader(getTextureSlotArray(textures.size()), modelShader);

			glBindVertexArray(vaoID);
			enableVertexAttribArrays(0, 1, 2, 3, 4, 5, 6, 7, 8);

			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);

			matBuffer.bindBase(0);
			pointLightBuffer.bindBase(1);
			spotLightBuffer.bindBase(2);
			directionalLightBuffer.bindBase(3);
			glDrawElementsInstanced(GL_TRIANGLES, model.getIndexCount(), GL_UNSIGNED_INT, 0, instanceDataList.size());
			disableVertexAttribArrays(0, 1, 2, 3, 4, 5, 6, 7, 8);
			glBindVertexArray(0);
			unbindTextureArrays();

			modelShader.detach();

			instanceDataList.clear();

			camera.update();
		}

		modelInstanceData.clear();
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
		quadShader.use();
		GLUtils.uploadMatricesToShader(batch.getCamera(), quadShader);
		
		activateAndBindTextures(textures);
		uploadTexturesToShader(getTextureSlotArray(textures.size() + 1), quadShader);

		glBindVertexArray(batch.getVaoID());
		enableVertexAttribArrays(0, 1);

		glDrawElements(GL_TRIANGLES, batch.getQuadCount() * 6, GL_UNSIGNED_INT, 0);

		disableVertexAttribArrays(0, 1);

		glBindVertexArray(0);

		unbindTextureArrays();
		quadShader.detach();
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
		
		modelShader.uploadInt("pointLightAmount", lights.size());
	}
	
	private void fillSpotLightBuffer(List<SpotLight> lights) {
		if (spotLightBuffer.getMaxItems() >= lights.size()) {
			spotLightBuffer.refill(lights);
		} else {
			spotLightBuffer.fill(lights);
		}
		
		modelShader.uploadInt("spotLightAmount", lights.size());
	}
	
	private void fillDirectionalLightBuffer(List<DirectionalLight> lights) {
		if (directionalLightBuffer.getMaxItems() >= lights.size()) {
			directionalLightBuffer.refill(lights);
		} else {
			directionalLightBuffer.fill(lights);
		}
		
		modelShader.uploadInt("directionalLightAmount", lights.size());
	}

	private void uploadTexturesToShader(int[] textureSlots, OpenGLShader shader) {
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
