package dev.hugame.graphics;

import static org.lwjgl.opengl.GL43.*;

import java.nio.IntBuffer;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.lwjgl.BufferUtils;

import dev.hugame.core.Renderer;
import dev.hugame.inject.Inject;
import dev.hugame.util.Files;

/** OpenGL implementation of batched 2D render calls. */
public class GLBatch {

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

	@Inject
	private Renderer renderer;

	public GLBatch() {
		this(getDefaultShader());
	}

	public GLBatch(Shader shader) {
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

	/** Returns the ID of this batch's vao. */
	public int getVaoID() {
		return vaoID;
	}

	/** Returns the ID of this batch's vbo. */
	public int getVboID() {
		return vboID;
	}

	/** Returns the array containg the vertex data in this batch. */
	public float[] getVertices() {
		return vertices;
	}

	/** Returns the texture list used in this batch. */
	public List<Texture> getTextures() {
		return textures;
	}

	/** Returns the camera used to draw this batch. */
	public Camera getCamera() {
		return camera;
	}

	/** Returns the shader used to draw this batch. */
	public Shader getShader() {
		return shader;
	}

	/** Prepares this batch for accepting draw calls. */
	public void begin() {
		idx = 0;
		textures = new ArrayList<>();
	}

	/**
	 * Flushes this batch to the renderer.
	 * 
	 * @see GLBatch#flush()
	 */
	public void end() {
		flush();
	}

	/**
	 * Adds a texture to this batch's draw queue.
	 * 
	 * @param texture the texutre to use
	 * @param x       the x-coordinate of the bottom-left corner
	 * @param y       the y-coordinate of the bottom-left corner
	 * @param width   the distance between left and right edge
	 * @param height  the distance between bottom and top edge
	 */
	public void draw(Texture texture, int x, int y, int width, int height) {
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

	/** Sets the camera to be used to draw this batch. */
	public void setCamera(Camera2D camera) {
		this.camera = camera;
	}

	/** Sets the shader to be used to draw this batch. */
	public void setShader(Shader shader) {
		this.shader = (Shader) shader;
	}

	/** Flushes this batch to the renderer. */
	public void flush() {
		renderer.renderBatch(this);

		textures.clear();
		idx = 0;
	}

	/**
	 * Creates a gl vertex array object.
	 * 
	 * @return a pointer to the gl object
	 */
	private int createVAO() {
		var vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);
		return vaoID;
	}

	/**
	 * Creates a gl vertex buffer object.
	 * 
	 * @return a pointer to the gl object
	 */
	private int createVBO() {
		var vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, vertices.length * VERTEX_SIZE_BYTES, GL_DYNAMIC_DRAW);
		return vboID;
	}

	/**
	 * Creates and binds a new gl element buffer object, and fills it with indices.
	 */
	private void createEBO() {
		int eboID = glGenBuffers();
		int[] indices = generateAllQuadIndices();
		for (int index : indices) {
		}
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
	}

	/**
	 * Creates an array of quad indices matching the max quad count in this batch.
	 * 
	 * @return the index array
	 */
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
