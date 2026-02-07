package dev.hugame.desktop.gl;

import static org.lwjgl.opengl.GL43.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import dev.hugame.desktop.gl.shader.ShaderFactory;
import org.lwjgl.BufferUtils;

import dev.hugame.graphics.Batch;
import dev.hugame.graphics.Camera2D;
import dev.hugame.graphics.Shader;
import dev.hugame.graphics.Texture;
import dev.hugame.util.Files;

/** OpenGL implementation of batched 2D render calls. */
public class GLBatch implements Batch {

	private static final int POSITION_SIZE = 3;
	private static final int TEX_COORDS_SIZE = 2;
	private static final int TEX_ARRAY_ID_SIZE = 1;
	private static final int TEX_ARRAY_INDEX_SIZE = 1;

	private static final int POSITION_OFFSET = 0;
	private static final int TEX_COORDS_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES;
	private static final int TEX_ARRAY_ID_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES;
	private static final int TEX_ARRAY_INDEX_OFFSET = TEX_ARRAY_ID_OFFSET + TEX_ARRAY_ID_SIZE * Float.BYTES;

	private static final int VERTEX_SIZE = POSITION_SIZE + TEX_COORDS_SIZE + TEX_ARRAY_ID_SIZE
			+ TEX_ARRAY_INDEX_SIZE;
	private static final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    private static final float Z_LAYER = 0;
    private static final int MAX_QUAD_COUNT = 1_000;
    private static final int MAX_VERTEX_COUNT = MAX_QUAD_COUNT * 4;

	public static Shader getDefaultShader() {
		var shaderFactory = new ShaderFactory();
		var vertexSource = Files.read("/shaders/opengl_quad_vertex_shader.glsl").orElseThrow();
		var fragmentSource = Files.read("/shaders/opengl_quad_fragment_shader.glsl").orElseThrow();

		return shaderFactory.createShader(vertexSource, fragmentSource).orElseThrow();
	}

	private final int textureSlotAmount;

	private int vaoID;
	private int vboID;
    private final OpenglIndexBuffer indexBuffer;

	private float[] vertices;
    private ByteBuffer vertexData;
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

		vertices = new float[MAX_VERTEX_COUNT * VERTEX_SIZE];
        vertexData = BufferUtils.createByteBuffer(MAX_VERTEX_COUNT * VERTEX_SIZE_BYTES);

		vaoID = createVAO();
		vboID = createVBO();
		this.indexBuffer = createEBO();

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

    public ByteBuffer getVertexData() {
        return vertexData;
    }

	/** Returns the texture list used in this batch. */
	public List<GLTextureArray> getTextures() {
		return textureArrays;
	}

	@Override
	public Camera2D getCamera() {
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

	@Override
	public void end() {
		flush();
	}

	@Override
	public void draw(Texture texture, int x, int y, int width, int height) {
		if (idx / (4 * VERTEX_SIZE) >= MAX_QUAD_COUNT || textureArrays.size() >= textureSlotAmount - 1) {
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
		
		// Top left
        vertexData.putFloat(x);
        vertexData.putFloat(y);
        vertexData.putFloat(Z_LAYER);

        vertexData.putFloat(u1);
        vertexData.putFloat(v1);

        vertexData.putInt(textureSlot);

        vertexData.putInt(glTexture.getLayer());

		idx += VERTEX_SIZE;

		// Bottom left
        vertexData.putFloat(x);
        vertexData.putFloat(y + height);
        vertexData.putFloat(Z_LAYER);

        vertexData.putFloat(u1);
        vertexData.putFloat(v2);

        vertexData.putInt(textureSlot);

        vertexData.putInt(glTexture.getLayer());

		idx += VERTEX_SIZE;

		// Bottom right
        vertexData.putFloat(x + width);
        vertexData.putFloat(y + height);
        vertexData.putFloat(Z_LAYER);

        vertexData.putFloat(u2);
        vertexData.putFloat(v2);

        vertexData.putInt(textureSlot);

        vertexData.putInt(glTexture.getLayer());

		idx += VERTEX_SIZE;

		// Top right
        vertexData.putFloat(x + width);
        vertexData.putFloat(y);
        vertexData.putFloat(Z_LAYER);

        vertexData.putFloat(u2);
        vertexData.putFloat(v1);

        vertexData.putInt(textureSlot);

        vertexData.putInt(glTexture.getLayer());

		idx += VERTEX_SIZE;
	}

	@Override
	public void setCamera(Camera2D camera) {
		this.camera = camera;
	}

	@Override
	public void flush() {
		renderer.renderBatch(this);

		textureArrays.clear();
		idx = 0;
	}

	public int getQuadCount() {
		return idx / (4 * VERTEX_SIZE);
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
        vertexData.rewind();
		glBufferData(GL_ARRAY_BUFFER, MAX_VERTEX_COUNT * VERTEX_SIZE_BYTES, GL_DYNAMIC_DRAW);
		return vboID;
	}

	/**
	 * Creates and binds a new gl element buffer object, and fills it with indices.
	 */
	private OpenglIndexBuffer createEBO() {
		int indexBufferHandle = glGenBuffers();
		int[] indices = generateAllQuadIndices();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferHandle);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        return new OpenglIndexBuffer(indexBufferHandle);
	}

	/**
	 * Creates an array of quad indices matching the max quad count in this batch.
	 * 
	 * @return the index array
	 */
	private int[] generateAllQuadIndices() {
		return IntStream.range(0, MAX_QUAD_COUNT).flatMap(offset -> Arrays.stream(getQuadIndices(offset * 4))).toArray();
	}

	private int[] getQuadIndices(int quadOffset) {
		return Stream.of(3, 2, 0, 0, 2, 1).mapToInt(index -> index + quadOffset).toArray();
	}

	private void setVertexAttribPointers() {
		glVertexAttribPointer(0, POSITION_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POSITION_OFFSET);
		glEnableVertexAttribArray(0);

		glVertexAttribPointer(1, TEX_COORDS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
		glEnableVertexAttribArray(1);

		glVertexAttribPointer(2, TEX_ARRAY_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ARRAY_ID_OFFSET);
		glEnableVertexAttribArray(2);
		
		glVertexAttribPointer(3, TEX_ARRAY_INDEX_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ARRAY_INDEX_OFFSET);
		glEnableVertexAttribArray(3);
	}

}
