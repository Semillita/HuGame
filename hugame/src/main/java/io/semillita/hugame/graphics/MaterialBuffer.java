package io.semillita.hugame.graphics;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import io.semillita.hugame.graphics.material.Material;
import io.semillita.hugame.util.Util;

import static org.lwjgl.opengl.GL43.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MaterialBuffer {

	private final int handle;
	
	public MaterialBuffer(List<Material> materials) {
		handle = createBuffer();
		fillBuffer(materials);
	}
	
	public void bind() {
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, handle);
	}
	
	private int createBuffer() {
		var id = glGenBuffers();
		return id;
	}
	
	private void fillBuffer(List<Material> materials) {
//		try(MemoryStack stack = MemoryStack.stackPush()) {
//			int index = 0;
//			byte[] bytes = new byte[materials.size() * Material.SIZE_IN_BYTES];
//			System.out.println("Mat bytes");
//			for (int i = 0; i < materials.size(); i++) {
//				var mat = materials.get(i);
//				var matBytes = mat.getBytes();
//				for (int j = 0; j < matBytes.length; j++) {
//					bytes[index] = matBytes[j];
//					System.out.println(matBytes[j]);
//					index++;
//				}
//			}
//			var buffer = stack.bytes(bytes);
//			
//			System.out.println("Array");
//			for (var b : bytes) {
//				System.out.println(b);
//			}
//
//			System.out.println("Buffer");
//			for (int i = 0; i < 12; i++) {
//				System.out.println(buffer.get(i));
//			}
//			
//			buffer.flip();
//			
//			glBindBuffer(GL_SHADER_STORAGE_BUFFER, handle);
//			glBufferData(GL_SHADER_STORAGE_BUFFER, buffer, GL_STATIC_DRAW);
//		}
		
		final var bufferSize = materials.size() * Material.SIZE_IN_BYTES;
		final var buffer = MemoryStack.stackPush().malloc(bufferSize);
		
		for (var mat : materials) {
			for (var b : mat.getBytes()) {
				buffer.put(b);
			}
		}
		
		buffer.flip();
		
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, handle);
		glBufferData(GL_SHADER_STORAGE_BUFFER, buffer, GL_STATIC_DRAW);
	}
	
}
