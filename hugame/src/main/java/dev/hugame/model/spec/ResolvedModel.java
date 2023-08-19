package dev.hugame.model.spec;

import java.util.List;

public class ResolvedModel {
	private final List<ResolvedMesh> meshes;

	private final List<ResolvedMaterial> materials;

	public ResolvedModel(List<ResolvedMesh> meshes, List<ResolvedMaterial> materials) {
		this.meshes = meshes;
		this.materials = materials;
	}

	public List<ResolvedMesh> getMeshes() {
		return meshes;
	}

	public List<ResolvedMaterial> getMaterials() {
		return materials;
	}
}
