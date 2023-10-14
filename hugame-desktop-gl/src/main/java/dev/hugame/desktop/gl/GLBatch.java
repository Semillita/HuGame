package dev.hugame.desktop.gl;

import static org.lwjgl.opengl.GL43.*;

import java.nio.IntBuffer;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import dev.hugame.desktop.gl.shader.ShaderFactory;
import org.lwjgl.BufferUtils;

import dev.hugame.core.HuGame;
import dev.hugame.graphics.Batch;
import dev.hugame.graphics.Camera;
import dev.hugame.graphics.Camera2D;
import dev.hugame.graphics.Shader;
import dev.hugame.graphics.Texture;
import dev.hugame.util.Files;

/** OpenGL implementation of batched 2D render calls. */
public class GLBatch implements Batch {

	private static final int POSITION_SIZE = 3;
	private static final int COLOR_SIZE = 4;
	private static final int TEX_COORDS_SIZE = 2;
	private static final int TEX_ARRAY_ID_SIZE = 1;
	private static final int TEX_ARRAY_INDEX_SIZE = 1;

	private static final int POSITION_OFFSET = 0;
	private static final int COLOR_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES;
	private static final int TEX_COORDS_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;
	private static final int TEX_ARRAY_ID_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES;
	private static final int TEX_ARRAY_INDEX_OFFSET = TEX_ARRAY_ID_OFFSET + TEX_ARRAY_ID_SIZE * Float.BYTES;

	private static final int VERTEX_SIZE = POSITION_SIZE + COLOR_SIZE + TEX_COORDS_SIZE + TEX_ARRAY_ID_SIZE
			+ TEX_ARRAY_INDEX_SIZE;
	private static final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

	public static Shader getDefaultShader() {
		var shaderFactory = new ShaderFactory();
		var vertexSource = Files.read("/shaders/batch_vertex_shader.glsl").orElseThrow();
		var fragmentSource = Files.read("/shaders/batch_fragment_shader.glsl").orElseThrow();

		return shaderFactory.createShader(vertexSource, fragmentSource).orElseThrow();
	}

	private final int textureSlotAmount;

	private int vaoID;
	private int vboID;

	private int maxQuadCount = 1000;
	private float[] vertices;
	private Camera2D camera;
	private Shader shader;
	
	private List<GLTextureArray> textureArrays;

	private int idx;

	private GLRenderer renderer;

	public GLBatch(GLRenderer renderer) {
		this(renderer, getDefaultShader());
	}

	public GLBatch(GLRenderer renderer, Shader shader) {
		this.shader = shader;
		IntBuffer units = BufferUtils.createIntBuffer(1);
		glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, units);
		textureSlotAmount = units.get(0);

		vertices = new float[maxQuadCount * 4 * VERTEX_SIZE];

		vaoID = createVAO();
		vboID = createVBO();
		createEBO();

		setVertexAttribPointers();

		this.renderer = renderer;

		textureArrays = new ArrayList<>();
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
	public List<GLTextureArray> getTextures() {
		return textureArrays;
	}

	/** Returns the camera used to draw this batch. */
	public Camera getCamera() {
		return camera;
	}

	/** Returns the shader used to draw this batch. */
	public Shader getShader() {
		return shader;
	}

	public void begin() {
		idx = 0;
		textureArrays.clear();
	}

	public void end() {
		flush();
	}

	public void draw(Texture texture, int x, int y, int width, int height) {
		if (idx / 40 >= maxQuadCount || textureArrays.size() >= textureSlotAmount - 1) {
			flush();
		}
		
		if (!(texture instanceof GLTexture glTexture)) {
			throw new RuntimeException("Wrong API implementation of Texture used for GLBatch");
		}

		var textureArray = glTexture.getTextureArray();

		final float u1 = 0, v1 = 1, u2 = 1, v2 = 0;

		var textureSlot = textureArrays.indexOf(textureArray);
		if (textureSlot == -1) {
			textureArrays.add(textureArray);
			textureSlot = textureArrays.size() - 1;
		}
		
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
		vertices[idx + 9] = textureSlot;
		// Tex index
		vertices[idx + 10] = glTexture.getArrayIndex();

		// </Top Left>

		idx += VERTEX_SIZE;

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
		vertices[idx + 9] = textureSlot;
		// Tex index
		vertices[idx + 10] = glTexture.getArrayIndex();

		// </Bottom left>

		idx += VERTEX_SIZE;

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
		vertices[idx + 9] = textureSlot;
		// Tex index
		vertices[idx + 10] = glTexture.getArrayIndex();

		// </Bottom right>

		idx += VERTEX_SIZE;

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
		vertices[idx + 9] = textureSlot;
		// Tex index
		vertices[idx + 10] = glTexture.getArrayIndex();

		// </Top right>

		idx += VERTEX_SIZE;
	}

	public void setCamera(Camera2D camera) {
		this.camera = camera;
	}

	public void setShader(Shader shader) {
		this.shader = shader;
	}

	public void flush() {
		renderer.renderBatch(this);

		textureArrays.clear();
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
		return Stream.of(3, 2, 0, 0, 2, 1).mapToInt(index -> index + quadOffset).toArray();
	}

	private void setVertexAttribPointers() {
		glVertexAttribPointer(0, POSITION_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POSITION_OFFSET);
		glEnableVertexAttribArray(0);

		glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
		glEnableVertexAttribArray(1);

		glVertexAttribPointer(2, TEX_COORDS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
		glEnableVertexAttribArray(2);

		glVertexAttribPointer(3, TEX_ARRAY_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ARRAY_ID_OFFSET);
		glEnableVertexAttribArray(3);
		
		glVertexAttribPointer(4, TEX_ARRAY_INDEX_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ARRAY_INDEX_OFFSET);
		glEnableVertexAttribArray(4);
	}

}
