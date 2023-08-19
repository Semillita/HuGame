package dev.hugame.model.spec;

import dev.hugame.io.FileHandle;
import dev.hugame.io.FileLocation;

import java.util.Optional;

public abstract class ModelLoader {
	public abstract boolean supports(String modelFormat, FileLocation fileLocation);

	public abstract Optional<ResolvedModel> load(FileHandle file);
}
