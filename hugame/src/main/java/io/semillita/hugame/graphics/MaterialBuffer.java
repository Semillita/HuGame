package io.semillita.hugame.graphics;

import org.joml.Vector3f;
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
		System.out.println("---- Materialbuffer with " + materials.size() + " materials ----");
		handle = createBuffer();
		fillBuffer(materials);
		
//		try(MemoryStack mem = MemoryStack.stackPush()) {
//			  FloatBuffer buffer = mem.floats(1.0f);
//			  buffer.flip();
//			  glBufferData(handle, buffer, GL_SHADER_STORAGE_BUFFER);
//			  glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, handle);
//			}
	}
	
	public void bind() {
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, handle);
	}
	
	private int createBuffer() {
		var id = glGenBuffers();
		return id;
	}
	
	private void fillBuffer(List<Material> materials) {
		System.out.println("Filling material buffer");
		try(MemoryStack stack = MemoryStack.stackPush()) {
			var buffer = stack.malloc(materials.size() * Material.SIZE_IN_BYTES);
			int index = 0;
			for (int i = 0; i < materials.size(); i++) {
				var mat = materials.get(i);
				buffer.put(index, mat.getBytes());
				index += Material.SIZE_IN_BYTES;
			}
			buffer.flip();
			
			System.out.println("Bind buffer");
			
			glBindBuffer(GL_SHADER_STORAGE_BUFFER, handle);
			
			System.out.println("Buffer data");
			
			glBufferData(GL_SHADER_STORAGE_BUFFER, buffer, GL_STATIC_DRAW);
			
			System.out.println("Done with buffer data");
		}
		
	}
	
}
