package dev.hugame.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Utility class representing a model transformation */
public class Transform {
	public Vector3f position;
	public Vector3f rotation;
	public Vector3f scale;
	
	private Matrix4f matrix;
	
	/** Creates a transform out of the given position, rotation and scale. */
	public Transform(Vector3f position, Vector3f rotation, Vector3f scale) {
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
		
		this.matrix = Maths.createTransformationMatrix(position, rotation, scale);
	}
	
	/** Updates this transform's matrix. */
	public void update() {
		matrix = Maths.createTransformationMatrix(position, rotation, scale);
	}
	
	/** Returns the matrix of this transform. */
	public Matrix4f getMatrix() {
		return matrix;
	}
	
}
