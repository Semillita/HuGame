package dev.hugame.core;

import dev.hugame.core.graphics.Model;
import dev.hugame.environment.Environment;
import dev.hugame.graphics.GLBatch;
import dev.hugame.graphics.PerspectiveCamera;
import dev.hugame.graphics.material.Material;
import dev.hugame.util.Transform;

public interface Renderer {

	public void create();
	
	public void draw(Model model, Transform transform, Material material);
	
	public void flush();
	
	public PerspectiveCamera getCamera();
	
	public void updateEnvironment(Environment environment);
	
	public void renderBatch(GLBatch batch);
}
