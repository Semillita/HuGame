package dev.hugame.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
	
	private static Class<? extends Object> getClass(String className, String packageName) {
		try {
			return Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.')));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
