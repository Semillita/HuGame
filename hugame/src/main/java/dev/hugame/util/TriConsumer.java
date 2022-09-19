package dev.hugame.util;

@FunctionalInterface
public interface TriConsumer<T, U, R> {
	
	public void accept(T t, U u, R r);
	
}
