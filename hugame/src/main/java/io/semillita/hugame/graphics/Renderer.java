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

import io.semillita.hugame.graphics.material.Materials;
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
	
	private Map<Model, List<Transform>> modelDrawQueue;
	private Camera camera;
	private Shader instanceShader;
	private Shader batchShader;
	
	private float[] transformArray;
	
	private MaterialBuffer matBuffer;
	
//	private FrameBuffer fb;
	
	public Renderer() {
		System.out.println("New Renderer");
		
		modelDrawQueue = new HashMap<>();
		camera = new Camera(new Vector3f(0, 20, 20));
		instanceShader = new Shader("/shaders/instance_shader.glsl");
		instanceShader.compile();
		batchShader = new Shader("/shaders/batch_shader.glsl");
		batchShader.compile();
		
		System.out.println("Vertex attributes defined");
	
		transformArray = new float[MAX_INSTANCES * INSTANCE_SIZE];
		
		// Material buffer test
		matBuffer = new MaterialBuffer(Materials.collect());
		
		glDebugMessageCallback(new GLDebugMessageCallbackI() {
			
			@Override
			public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
				System.err.println("OpenGL error");
			}
		}, 0);
		
//		fb = new FrameBuffer();
	}

	public void draw(Model model, Transform transform) {
		if (modelDrawQueue.containsKey(model)) {
			modelDrawQueue.get(model).add(transform);
		} else {
			modelDrawQueue.put(model, new ArrayList<>(Arrays.asList(transform)));
		}
	}
	
	public void renderModels() {
//		System.out.println("renderModels()");
		
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_MULTISAMPLE);
			
		for (var entry : modelDrawQueue.entrySet()) {
			var model = entry.getKey();
			var transforms = entry.getValue();
			
			// Array of floats containing the values of the transform matrices
			var transformArray = createTransformArray(transforms);
			
			// VAO of the model
			var vaoID = model.getVAO();
			
			// Per-instance VBO of the model
			var i_vboID = model.getInstaceVBO();
			
			var eboID = model.getEBO();
			
			var textures = model.getTextures();
			
			// Fills the per-instance VBO with the transformation matrices
			GLUtils.fillVBO(i_vboID, transformArray);
			
			instanceShader.use();
			
			// Uploads projection and view matrices to the shader
			GLUtils.uploadMatricesToShader(camera, instanceShader);
			activateAndBindTextures(textures);
			uploadTexturesToShader(getTextureSlotArray(textures.size()), instanceShader);
			
			glBindVertexArray(vaoID);
			enableVertexAttribArrays(0, 1, 2, 3, 4, 5, 6, 7);
			
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
			
//			glPushDebugGroup(GL_DEBUG_SOURCE_APPLICATION, 0, "Test");
			//System.out.println("Binding material buffer");
			matBuffer.bind();
//			glPopDebugGroup();
			
			glDrawElementsInstanced(GL_TRIANGLES, model.getIndexAmount(), GL_UNSIGNED_INT, 0, transforms.size());
			
			disableVertexAttribArrays(0, 1, 2, 3, 4, 5, 6, 7);
			glBindVertexArray(0);
			unbindTextures(model.getTextures());
			
			instanceShader.detach();
			
			transforms.clear();
			
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
	
	private float[] createTransformArray(List<Transform> transforms) {
		transformArray = new float[transforms.size() * 16];
		for (int i = 0; i < transforms.size(); i++) {
			var transform = transforms.get(i);
			transform.getMatrix().get(transformArray, i * 16);
		}
		return transformArray;
	}

}
