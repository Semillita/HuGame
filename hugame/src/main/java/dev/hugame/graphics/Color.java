package dev.hugame.graphics;

import org.joml.Vector3f;

public final record Color(float x, float y, float z) {

	public static final Color BLACK = new Color(0, 0, 0);
	public static final Color RED = new Color(1, 0, 0);
	public static final Color GREEN = new Color(0, 1, 0);
	public static final Color BLUE = new Color(0, 0, 1);

	public Color(Vector3f xyz) {
		this(xyz.x, xyz.y, xyz.z);
	}

}
