package io.semillita.hugame.graphics;

import static org.lwjgl.opengl.GL40.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public class Graphics {

	private static final int POSITION_SIZE = 3;
	private static final int COLOR_SIZE = 4;
	private static final int TEX_COORDS_SIZE = 2;
	private static final int TEX_ID_SIZE = 1;

	private static final int POSITION_OFFSET = 0;
	private static final int COLOR_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES;
	private static final int TEX_COORDS_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;
	private static final int TEX_ID_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES;

	private static final int VERTEX_SIZE = POSITION_SIZE + COLOR_SIZE + TEX_COORDS_SIZE + TEX_ID_SIZE;
	private static final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

	private int vaoID;
	private int vboID;

	private int maxQuadCount = 1000;
	// private Vertex[] vertices;
	private float[] vertices;
	private Camera camera;
	private Shader shader;
	private List<Texture> textures;

	private int idx;

	private Texture face;

	public Graphics() {
		IntBuffer units = BufferUtils.createIntBuffer(1);
		glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, units);

		vertices = new float[maxQuadCount * 4 * VERTEX_SIZE];

		vaoID = createVAO();

		vboID = createVBO();

		createEBO();

		glVertexAttribPointer(0, POSITION_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POSITION_OFFSET);
		glEnableVertexAttribArray(0);

		glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
		glEnableVertexAttribArray(1);

		glVertexAttribPointer(2, TEX_COORDS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
		glEnableVertexAttribArray(2);

		glVertexAttribPointer(3, TEX_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ID_OFFSET);
		glEnableVertexAttribArray(3);

		try {
			var faceContent = new String(
					Files.readAllBytes(Paths.get(this.getClass().getResource("/face.png").toURI())), "ISO-8859-1");
			face = new Texture(faceContent);
			System.out.println("Face created");
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		
		camera = new Camera(new Vector3f(0, 0, 2));
//		shader = new Shader("/shaders/shader.glsl");
		shader.compile();
	}

	public void update() {
		begin();

		//drawQuad(face, 0.5f, 0, 0.5f, 0.25f);
		//drawQuad(face, -0.5f, -0.25f, 0.25f, 0.5f);
		
		for(int i = 0; i < 5; i++) {
			//drawQuad(face, 0 + i * 100, 0 + i * 100, 100, 100);
		}
		
		drawQuad(face, 0, 0, 100, 100);
		
		end();
	}

	public void begin() {
		idx = 0;
		textures = new ArrayList<>();
	}

	public void end() {
		flush();
	}

	public void drawQuad(Texture texture, float x, float y, float width, float height) {
//		if (idx > (maxQuadCount - 1) * 4 * VERTEX_SIZE) {
//			flush();
//			idx = 0;
//			textures.clear();
//		}

		final float u1 = 0, v1 = 1, u2 = 1, v2 = 0;
		final int slot = textures.size() + 1;

		textures.add(texture);

		// <Top left>

		// Position
		vertices[idx] = x;
		vertices[idx + 1] = y;
		vertices[idx + 2] = 5;
		// Color
		vertices[idx + 3] = 1;
		vertices[idx + 4] = 1;
		vertices[idx + 5] = 1;
		vertices[idx + 6] = 1;
		// Tex coords
		vertices[idx + 7] = u1;
		vertices[idx + 8] = v1;
		// Tex ID
		vertices[idx + 9] = slot;

		// </Top Left>

		idx += 10;

		// <Bottom left>

		// Position
		vertices[idx] = x;
		vertices[idx + 1] = y + height;
		vertices[idx + 2] = 5;
		// Color
		vertices[idx + 3] = 1;
		vertices[idx + 4] = 1;
		vertices[idx + 5] = 1;
		vertices[idx + 6] = 1;
		// Tex coords
		vertices[idx + 7] = u1;
		vertices[idx + 8] = v2;
		// Tex ID
		vertices[idx + 9] = slot;

		// </Bottom left>

		idx += 10;

		// <Bottom right>

		// Position
		vertices[idx] = x + width;
		vertices[idx + 1] = y + height;
		vertices[idx + 2] = 5;
		// Color
		vertices[idx + 3] = 1;
		vertices[idx + 4] = 1;
		vertices[idx + 5] = 1;
		vertices[idx + 6] = 1;
		// Tex coords
		vertices[idx + 7] = u2;
		vertices[idx + 8] = v2;
		// Tex ID
		vertices[idx + 9] = slot;

		// </Bottom right>

		idx += 10;

		// <Top right>

		// Position
		vertices[idx] = x + width;
		vertices[idx + 1] = y;
		vertices[idx + 2] = 5;
		// Color
		vertices[idx + 3] = 1;
		vertices[idx + 4] = 1;
		vertices[idx + 5] = 1;
		vertices[idx + 6] = 1;
		// Tex coords
		vertices[idx + 7] = u2;
		vertices[idx + 8] = v1;
		// Tex ID
		vertices[idx + 9] = slot;

		// </Top right>

		idx += 10;
	}

	private void flush() {
		// Flush till OpenGL
		fillVertexBuffer(vboID, vertices);
		useShader(shader);
		uploadMatricesToShader(camera, shader);
		activateAndBindTextures(textures);
		uploadTexturesToShader(getTextureSlotArray(textures.size() + 1));

		glBindVertexArray(vaoID);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);

		glDrawElements(GL_TRIANGLES, textures.size() * 6, GL_UNSIGNED_INT, 0);

		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);

		glBindVertexArray(0);

		unbindTextures(textures);

		shader.detach();
	}

	private void useShader(Shader shader) {
		shader.use();
	}

	private void fillVertexBuffer(int vboID, float[] vertices) {
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
	}

	private void uploadMatricesToShader(Camera camera, Shader shader) {
		shader.uploadMat4f("uProjection", camera.getProjectionMatrix());
		shader.uploadMat4f("uView", camera.getViewMatrix());
	}

	private void activateAndBindTextures(List<Texture> textures) {
		for (int textureIndex = 0; textureIndex < textures.size(); textureIndex++) {
			glActiveTexture(GL_TEXTURE0 + textureIndex + 1);
			// glActiveTexture(GL_TEXTURE0 + textureIndex);
			textures.get(textureIndex).bind();
		}
	}

	private void uploadTexturesToShader(int[] textureSlots) {
		shader.uploadTextureArray("uTextures", textureSlots);
	}

	private void unbindTextures(List<Texture> textures) {
		for (var texture : textures) {
			texture.unbind();
		}
	}

	private int[] getTextureSlotArray(int size) {
		int[] textureSlots = new int[size];
		for (int i = 0; i < size; i++) {
			textureSlots[i] = i;
		}
		return textureSlots;
	}

	private int createVAO() {
		var vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);
		return vaoID;
	}

	private int createVBO() {
		var vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, vertices.length * VERTEX_SIZE_BYTES, GL_DYNAMIC_DRAW);
		return vboID;
	}

	private void createEBO() {
		int eboID = glGenBuffers();
		int[] indices = generateAllQuadIndices();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
	}

	private int[] generateAllQuadIndices() {
		return IntStream.range(0, maxQuadCount).flatMap(offset -> Arrays.stream(getQuadIndices(offset * 4))).toArray();
	}

	private int[] getQuadIndices(int quadOffset) {
		return Arrays.asList(3, 2, 0, 0, 2, 1).stream().mapToInt(index -> index + quadOffset).toArray();
	}

}
