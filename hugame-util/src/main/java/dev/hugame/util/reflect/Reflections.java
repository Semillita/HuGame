package dev.hugame.util.reflect;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class Reflections {

	private static Map<String, Class<?>> loadedClasses;
	private static boolean runningInJar;
	
	static {
		loadedClasses = new HashMap<>();
		runningInJar = Reflections.class
				.getProtectionDomain()
				.getCodeSource()
				.getLocation()
				.toString()
				.endsWith(".jar");
	}
	
	private final JarInspector jarInspector;
	
	public Reflections() {
		jarInspector = new JarInspector();
	}
	
	public List<Class<?>> getClassesPresent(Predicate<String> classNameFilter) {
		System.out.println(runningInJar);
		return runningInJar ? jarInspector.getClassesPresent(getJarPath(), classNameFilter)
				: getClassesPresentExternal(classNameFilter);
	}
	
	private List<Class<?>> getClassesPresentExternal(Predicate<String> classNameFilter) {
		return Arrays.asList(System.getProperty("java.class.path").split(File.pathSeparator))
				.stream()
				.flatMap(path -> getClassesInFile(path, classNameFilter).stream())
				.toList();
	}
	
	private List<Class<?>> getClassesInFile(String filePath, Predicate<String> classNameFilter) {
		if (filePath.endsWith(".jar")) {
			return jarInspector.getClassesPresent(filePath, classNameFilter);
		}
		
		var sourceDir = new File(filePath);
		var sourceDirLength = sourceDir.getPath().length();
		
		return getFilesInDirRecursive(sourceDir)
				.stream()
				.map(File::getPath)
				.map(path -> path.substring(sourceDirLength + 1, path.length()))
				.map(name -> name.replace('\\', '.'))
				.filter(name -> name.endsWith(".class"))
				.map(name -> name.substring(0, name.length() - 6))
				.map(this::getClass)
				.flatMap(Optional::stream)
				.toList();
	}
	
	private List<File> getFilesInDirRecursive(File dir) {
		return Arrays.stream(dir.listFiles())
				.flatMap(child -> child.isFile() ? Stream.of(child) : getFilesInDirRecursive(child).stream())
				.toList();
	}
	
	private String getJarPath() {
		try {
			return new File(Reflections.class.getProtectionDomain().getCodeSource().getLocation().toURI())
					.getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return "";
		}
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
