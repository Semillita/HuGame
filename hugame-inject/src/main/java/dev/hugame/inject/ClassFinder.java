package dev.hugame.inject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassFinder {
	private static Map<String, Set<Class<?>>> packageClasses;
	
	static {
		packageClasses = new HashMap<>();
	}
	
	public static Set<Class<? extends Object>> getClassesInPackage(String packageName) {
		return (packageClasses.containsKey(packageName)) ? packageClasses.get(packageName) : 
			getResourcesInPackage(packageName)
				.stream()
				.flatMap(resourceName -> (resourceName.endsWith(".class") ? Stream.of(getClass(resourceName, packageName)) : 
					getClassesInPackage(packageName + "." + resourceName).stream()))
				.collect(Collectors.toSet());
	}
	
	public static Set<Method> getMethodsWithAnnotation(String packageName, Class<? extends Annotation> annotatonClass) {
		return getClassesInPackage(packageName)
				.stream()
				.peek(c -> System.out.println("Found class " + c))
				.flatMap(c -> Arrays.asList(c.getDeclaredMethods()).stream())
				.filter(method -> method.isAnnotationPresent(annotatonClass))
				.collect(Collectors.toSet());
	}
	
	private static Set<String> getResourcesInPackage(String packageName) {
		var stream = ClassLoader.getSystemClassLoader()
				.getResourceAsStream(packageName.replaceAll("[.]", "/"));
		return new BufferedReader(new InputStreamReader(stream))
				.lines()
				.collect(Collectors.toSet());
	}
	
	public static Class<? extends Object> getClass(String className, String packageName) {
		try {
			return Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.')));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<Class<?>> getClassesInClasspath(Predicate<String> classNameCheck) {
		try {
			var file = new File(ClassFinder.class.getProtectionDomain().getCodeSource().getLocation().toURI())
					.getPath();
			
			var jar = new JarFile(file);
			var entries = jar.entries();
			
			var classes = new ArrayList<Class<?>>();
			while (entries.hasMoreElements()) {
				var fileName = entries.nextElement().getName();
				if (fileName.endsWith(".class") && !fileName.startsWith("META-INF") && classNameCheck.test(fileName)) {
					var className = fileName
							.substring(0, fileName.length() - 6)
							.replace('/', '.');
					
					try {
						var c = Class.forName(className);
						classes.add(c);
					} catch (ClassNotFoundException | NoClassDefFoundError e) {
						// No worries
					}
				}
			}
			jar.close();
			
			return classes;
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}

		return List.of();
	}

}
