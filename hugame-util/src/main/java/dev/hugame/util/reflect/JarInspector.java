package dev.hugame.util.reflect;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.jar.JarFile;

public class JarInspector {

	private static final Predicate<String> classFileFilter = s -> s.endsWith(".class");
	private static final Predicate<String> metaInfFilter = s -> !s.startsWith("META-INF");
	private static final Predicate<String> lwjglFilter = s -> !s.startsWith("org.lwjgl");
	
	private final Map<String, List<Class<?>>> classesInJars;
	private final Map<String, Class<?>> loadedClasses;
	
	public JarInspector() {
		classesInJars = new HashMap<>();
		loadedClasses = new HashMap<>();
	}
	
	public List<Class<?>> getClassesPresent(String jarPath) {
		return getClassesPresent(jarPath, s -> true);
	}
	
	public List<Class<?>> getClassesPresent(String jarPath, Predicate<String> classNameFilter) {
		if (classesInJars.get(jarPath) != null) {
			return classesInJars.get(jarPath);
		}
		
		try {
			var jarFile = new JarFile(jarPath);
			var classes = Collections.list(jarFile.entries())
					.stream()
					.map(entry -> entry.getName())
					.map(fileName -> fileName.replace('/', '.'))
					.filter(classFileFilter.and(metaInfFilter).and(lwjglFilter).and(classNameFilter))
					.map(fileName -> fileName.substring(0, fileName.length() - 6))
					.filter(className -> className.contains("."))
					.map(this::getClass)
					.flatMap(Optional::stream)
					.toList();
				
			jarFile.close();
			return classes;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return List.of();
	}
	
	private Optional<Class<?>> getClass(String className) {
		var cached = loadedClasses.get(className);
		if (cached != null) return Optional.ofNullable(cached);
		try {
			var c = Class.forName(className);
			loadedClasses.put(className, c);
			return Optional.ofNullable(c);
		} catch (ClassNotFoundException e) {
			return Optional.empty();
		}
	}
	
}
