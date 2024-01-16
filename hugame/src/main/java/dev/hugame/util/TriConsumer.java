package dev.hugame.util;

@FunctionalInterface
public interface TriConsumer<S, U, V> {
	void accept(S t, U u, V r);
	
}
