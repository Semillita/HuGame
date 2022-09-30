package dev.hugame.inject;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;

import dev.hugame.util.reflect.Reflections;

public class InjectionEngine {

	private final Map<Class<?>, Object> dependencyObjects;
	private boolean started = false;
	
	public InjectionEngine() {
		dependencyObjects = new HashMap<>();
	}
	
	public void setDefaultInstance(Object instance) {
		setDefaultInstance(instance, instance.getClass());
	}
	
	public void setDefaultInstance(Object instance, Class<?> clazz) {
		if (instance == null) {
			throw new IllegalArgumentException("Instance cannot be null");
		}
		
		if (started) {
			return;
		}
		
		dependencyObjects.put(clazz, instance);
	}
	
	public void start() {
		started = true;
		Reflections reflections = new Reflections();
		var classes = reflections.getClassesPresent(this::testClassName);
		
		var globalClasses = classes
				.stream()
				.filter(c -> c.isAnnotationPresent(Global.class))
				.filter(Predicate.not(dependencyObjects::containsKey))
				.toList();
		
		for (var globalClass : globalClasses) {
			var maybeInstance = instantiate(globalClass);
			maybeInstance.ifPresent(instance -> dependencyObjects.put(globalClass, instance));
		}
		
		dependencyObjects
			.entrySet()
			.stream()
			.map(Entry::getValue)
			.peek(this::injectIntoObject)
			.forEach(this::initializeDependency);
	}
	
	public void injectIntoObject(Object object) {
		var fields = Arrays.asList(object.getClass().getDeclaredFields());
		
		var injectFields = fields
				.stream()
				.filter(field -> field.isAnnotationPresent(Inject.class))
				.toList();
		
		for (var field : injectFields) {
			var c = field.getType();
			var instance = dependencyObjects.get(c);
			try {
				field.setAccessible(true);
				field.set(object, instance);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void initializeDependency(Object object) {
		var maybeInitMethod = Arrays.stream(object.getClass().getDeclaredMethods())
				.filter(method -> method.isAnnotationPresent(Init.class))
				.filter(method -> method.getParameters().length == 0)
				.findFirst();
		
		maybeInitMethod.ifPresent(initMethod -> {
			try {
				initMethod.setAccessible(true);
				initMethod.invoke(object);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		});
	}
	
	private <T> Optional<T> instantiate(Class<T> type) {
		try {
			var constructor = type.getDeclaredConstructor();
			return Optional.ofNullable(constructor.newInstance());
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
	
	private boolean testClassName(String className) {
		return !className.startsWith("org/lwjgl");
	}
	
}
