package dev.hugame.inject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import dev.hugame.util.reflect.Reflections;

public class InjectionEngine {

	private final Map<Class<?>, Object> dependencyObjects;
	
	public InjectionEngine() {
		dependencyObjects = new HashMap<>();
	}
	
	public void start() {
		Reflections reflections = new Reflections();
		var classes = reflections.getClassesPresent(this::testClassName);
		
		var globalClasses = classes
				.stream()
				.filter(c -> c.isAnnotationPresent(Global.class))
				.toList();
		
		System.out.println(globalClasses);
		
		for (var globalClass : globalClasses) {
			var maybeInstance = instantiate(globalClass);
			maybeInstance.ifPresent(instance -> dependencyObjects.put(globalClass, instance));
		}
		
		System.out.println(dependencyObjects);
	}
	
	public void injectIntoObject(Object object) {
		System.out.println(object);
		
		var fields = Arrays.asList(object.getClass().getDeclaredFields());
		System.out.println("Object fields:");
		System.out.println(fields);
		
		var injectFields = fields
				.stream()
				.filter(field -> field.isAnnotationPresent(Inject.class))
				.toList();
		
		System.out.println("Inject fields:");
		System.out.println(injectFields);
		
		for (var field : injectFields) {
			var c = field.getType();
			var instance = dependencyObjects.get(c);
			System.out.println("Instance:");
			System.out.println(instance);
			
			try {
				field.setAccessible(true);
				field.set(object, instance);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	private <T> Optional<T> instantiate(Class<T> type) {
		try {
			return Optional.ofNullable(type.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
	
	private boolean testClassName(String className) {
		return !className.startsWith("org/lwjgl");
	}
	
}
