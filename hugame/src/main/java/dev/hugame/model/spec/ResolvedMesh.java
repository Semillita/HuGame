package dev.hugame.model.spec;

import java.util.List;

public class ResolvedMesh {
	private final List<ResolvedVertex> vertices;

	private final List<Integer> indices;

	private final int materialIndex;

	public ResolvedMesh(List<ResolvedVertex> vertices, List<Integer> indices, int materialIndex) {
		this.vertices = vertices;
		this.indices = indices;
		this.materialIndex = materialIndex;
	}

	public List<ResolvedVertex> getVertices() {
		return vertices;
	}

	public List<Integer> getIndices() {
		return indices;
	}

	public int getMaterialIndex() {
		return materialIndex;
	}
}
