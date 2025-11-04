package dev.hugame.graphics.text;

import dev.hugame.io.FileHandle;

import java.util.Optional;

public interface FontLoader {
	Optional<ResolvedFont> load(FileHandle file);
}
