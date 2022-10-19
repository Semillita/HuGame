package dev.hugame.graphics.model;

import java.util.List;

public record AssimpMesh(List<AssimpVertex> vertices, List<Integer> indices, int materialIndex) {}
