package io.semillita.hugame.graphics;

import static org.lwjgl.opengl.GL43.*;

import java.nio.IntBuffer;
import java.util.List;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.lwjgl.BufferUtils;

import io.semillita.hugame.core.HuGame;
import io.semillita.hugame.util.Files;

public class Batch {

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

	public static Shader getDefaultShader() {
		return Shaders.get(Files.read("/shaders/batch_vertex_shader.glsl").get(),
				Files.read("/shaders/batch_fragment_shader.glsl").get()).get();
	}

	private final int textureSlotAmount;

	private int vaoID;
	private int vboID;

	private int maxQuadCount = 1000;
	private float[] vertices;
	private Camera2D camera;
	private Shader shader;
	private List<Texture> textures;

	private int idx;

	public Batch() {
		this(getDefaultShader());
	}
	
	public Batch(Shader shader) {
		this.shader = shader;
		IntBuffer units = BufferUtils.createIntBuffer(1);
		glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, units);
		textureSlotAmount = units.get(0);

		vertices = new float[maxQuadCount * 4 * VERTEX_SIZE];

		vaoID = createVAO();
		vboID = createVBO();
		createEBO();

		setVertexAttribPointers();
	}

	public int getVaoID() {
		return vaoID;
	}

	public int getVboID() {
		return vboID;
	}

	public float[] getVertices() {
		return vertices;
	}

	public List<Texture> getTextures() {
		return textures;
	}

	public Camera getCamera() {
		return camera;
	}

	public Shader getShader() {
		return shader;
	}

	public void begin() {
		idx = 0;
		textures = new ArrayList<>();
	}

	public void end() {
		flush();
	}

	public void drawQuad(Texture texture, int x, int y, int width, int height) {
		if (idx / 40 >= maxQuadCount || textures.size() >= textureSlotAmount - 1) {
			flush();
		}

		final float u1 = 0, v1 = 1, u2 = 1, v2 = 0;
		final int slot = textures.size();

		textures.add(texture);

		// <Top left>

		// Position
		vertices[idx] = x;
		vertices[idx + 1] = y;
		vertices[idx + 2] = 0;
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
		vertices[idx + 2] = 0;
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
		vertices[idx + 2] = 0;
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
		vertices[idx + 2] = 0;
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

	public void setCamera(Camera2D camera) {
		this.camera = camera;
	}

	public void setShader(Shader shader) {
		this.shader = (Shader) shader;
	}

	public void flush() {
		HuGame.getRenderer().renderBatch(this);

		textures.clear();
		idx = 0;
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
		for (int index : indices) {
		}
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
	}

	private int[] generateAllQuadIndices() {
		return IntStream.range(0, maxQuadCount).flatMap(offset -> Arrays.stream(getQuadIndices(offset * 4))).toArray();
	}

	private int[] getQuadIndices(int quadOffset) {
		return Arrays.asList(3, 2, 0, 0, 2, 1).stream().mapToInt(index -> index + quadOffset).toArray();
	}

	private void setVertexAttribPointers() {
		glVertexAttribPointer(0, POSITION_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POSITION_OFFSET);
		glEnableVertexAttribArray(0);

		glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
		glEnableVertexAttribArray(1);

		glVertexAttribPointer(2, TEX_COORDS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
		glEnableVertexAttribArray(2);

		glVertexAttribPointer(3, TEX_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ID_OFFSET);
		glEnableVertexAttribArray(3);
	}

}
