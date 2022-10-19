package dev.hugame.graphics.model;

import java.util.List;

import dev.hugame.graphics.Texture;

public record AssimpModel(List<AssimpMesh> meshes, List<AssimpMaterial> materials, List<Texture> textures) {}
