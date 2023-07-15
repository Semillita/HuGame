package dev.hugame.desktop.gl.backend;

public record VertexAttributePointerLayout(
		int size,
		ValueType type,
		boolean normalized,
		int stride,
		long offset,
		int divisor) {}
