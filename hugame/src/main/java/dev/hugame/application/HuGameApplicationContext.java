package dev.hugame.application;

import dev.hugame.core.Graphics;
import dev.hugame.core.Input;
import dev.hugame.core.Window;
import dev.hugame.model.spec.ModelLoader;

public interface HuGameApplicationContext {
	Window getWindow();
	Graphics getGraphics();
	Input getInput();
	ModelLoader getModelLoader();
}
