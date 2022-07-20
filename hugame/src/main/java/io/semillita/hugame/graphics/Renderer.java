package io.semillita.hugame.graphics;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GLDebugMessageCallbackI;

import io.semillita.hugame.environment.PointLight;
import io.semillita.hugame.graphics.material.Material;
import io.semillita.hugame.graphics.material.Materials;
import io.semillita.hugame.graphics.opengl.LightBuffer;
import io.semillita.hugame.util.Transform;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL43.*;

/** Renderer master class for rendering all different components */
public class Renderer {

	private static final int MAX_INSTANCES = 1000;
	private static final int TRANSFORM_SIZE = 16;
	private static final int TRANSFORM_OFFSET = 0;
	private static final int INSTANCE_SIZE = TRANSFORM_SIZE;
	private static final int INSTANCE_SIZE_BYTES = INSTANCE_SIZE * Float.BYTES;
	
	private Map<Model, List<InstanceData>> modelInstanceData;
	private Camera camera;
	private Shader instanceShader;
	private Shader batchShader;
	
	private MaterialBuffer matBuffer;
	private LightBuffer lightBuffer;
	
	public Renderer() {
		modelInstanceData = new HashMap<>();
		camera = new Camera(new Vector3f(0, 20, 20));
		instanceShader = new Shader("/shaders/instance_shader.glsl");
		instanceShader.compile();
		batchShader = new Shader("/shaders/batch_shader.glsl");
		batchShader.compile();
		
		// Material buffer test
		matBuffer = new MaterialBuffer(Materials.collect());
		//lightBuffer = new LightBuffer(new PointLight(new Vector3f(20, 20, 20)));
		
		glDebugMessageCallback(new GLDebugMessageCallbackI() {
			
			@Override
			public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
				System.err.println("OpenGL error");
			}
		}, 0);
		
//		fb = new FrameBuffer();
	}

	public void draw(Model model, Transform transform, Material material) {
		var instanceData = new InstanceData(transform, material);
		
		var instanceDataList = modelInstanceData.get(model);
		if (instanceDataList == null) {
			instanceDataList = new ArrayList<>();
			modelInstanceData.put(model, instanceDataList);
		}
		instanceDataList.add(instanceData);
	}
	
	public void renderModels() {
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_MULTISAMPLE);
			
		for (var entry : modelInstanceData.entrySet()) {
			var model = entry.getKey();
			var instanceDataList = entry.getValue();
			
			// Array of floats containing the values of the transform matrices
			var instanceDataArray = createInstanceDataArray(instanceDataList);
			
			// VAO of the model
			var vaoID = model.getVAO();
			
			// Per-instance VBO of the model
			var i_vboID = model.getInstaceVBO();
			
			var eboID = model.getEBO();
			
			var textures = model.getTextures();
			
			// Fills the per-instance VBO with the transformation matrices
			GLUtils.fillVBO(i_vboID, instanceDataArray);
			
			instanceShader.use();
			
			// Uploads projection and view matrices to the shader
			GLUtils.uploadMatricesToShader(camera, instanceShader);
			activateAndBindTextures(textures);
			uploadTexturesToShader(getTextureSlotArray(textures.size()), instanceShader);
			
			glBindVertexArray(vaoID);
			enableVertexAttribArrays(0, 1, 2, 3, 4, 5, 6, 7, 8);
			
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
			
//			glPushDebugGroup(GL_DEBUG_SOURCE_APPLICATION, 0, "Test");
			matBuffer.bind();
//			glPopDebugGroup();
			
			glDrawElementsInstanced(GL_TRIANGLES, model.getIndexAmount(), GL_UNSIGNED_INT, 0, instanceDataList.size());
			
			disableVertexAttribArrays(0, 1, 2, 3, 4, 5, 6, 7, 8);
			glBindVertexArray(0);
			unbindTextures(model.getTextures());
			
			instanceShader.detach();
			
			instanceDataList.clear();
			
			camera.adjustProjection();
		}
		
	}
	
	public Camera getCamera() {
		return camera;
	}
	
	void renderBatch(Batch batch) {
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

		unbindTextures(textures);
		batchShader.detach();
	}
	
	private void activateAndBindTextures(List<Texture> textures) {
		for (int textureIndex = 0; textureIndex < textures.size(); textureIndex++) {
			glActiveTexture(GL_TEXTURE0 + textureIndex);
			textures.get(textureIndex).bind();
		}
	}
	
	private void uploadTexturesToShader(int[] textureSlots, Shader shader) {
		shader.uploadTextureArray("uTextures", textureSlots);
	}
	
	private void unbindTextures(List<Texture> textures) {
		for (var texture : textures) {
			texture.unbind();
		}
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
		var instanceDataArray = new float[instanceData.size() * 17];
		for (int i = 0; i < instanceData.size(); i++) {
			var instance = instanceData.get(i);
			instance.transform().getMatrix().get(instanceDataArray, i * 17);
			instanceDataArray[i * 17 + 16] = (float) instance.material().getIndex();
		}
		return instanceDataArray;
	}

}
