package dev.hugame.graphics.spec.buffer;

import dev.hugame.util.Bufferable;

import java.util.List;

public interface ShaderStorageBuffer<T extends Bufferable> {
	int getMaxItems();

	void allocate(int maxItems);

	default void fill(List<T> items) {
		fill(items, items.size());
	}

	void fill(List<T> items, int maxItems);

	void refill(List<T> items);
}
