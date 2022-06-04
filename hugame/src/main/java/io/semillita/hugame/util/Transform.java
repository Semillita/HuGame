package io.semillita.hugame.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transform {

	public Vector3f position;
	public Vector3f rotation;
	public Vector3f scale;
	
	private Matrix4f matrix;
	
	public Transform(Vector3f position, Vector3f rotation, Vector3f scale) {
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
		
		this.matrix = Maths.createTransformationMatrix(position, rotation, scale);
	}
	
	public void update() {
		matrix = Maths.createTransformationMatrix(position, rotation, scale);
	}
	
	public Matrix4f getMatrix() {
		return matrix;
	}
	
}
