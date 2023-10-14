package dev.hugame.application;

import dev.hugame.core.Graphics;
import dev.hugame.core.Input;
import dev.hugame.core.Window;
import dev.hugame.model.spec.ModelLoader;

import java.util.List;

public record SimpleApplicationConfiguration(
		Graphics graphics,
		Window window,
		Input input,
		List<ModelLoader> modelLoaders) {}
