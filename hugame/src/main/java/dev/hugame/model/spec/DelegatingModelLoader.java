package dev.hugame.model.spec;

import dev.hugame.io.FileHandle;
import dev.hugame.io.FileLocation;

import java.util.List;
import java.util.Optional;

public class DelegatingModelLoader extends ModelLoader {
	private final List<ModelLoader> delegates;

	public DelegatingModelLoader(List<ModelLoader> delegates) {
		this.delegates = delegates;
	}


	@Override
	public boolean supports(String modelFormat, FileLocation fileLocation) {
		return delegates.stream().anyMatch(delegate -> delegate.supports(modelFormat, fileLocation));
	}

	@Override
	public Optional<ResolvedModel> load(FileHandle file) {
		for (var delegate : delegates) {
			var maybeLoadedModel = delegate.load(file);

			if (maybeLoadedModel.isPresent()) {
				return maybeLoadedModel;
			}
		}

		return Optional.empty();
	}
}
