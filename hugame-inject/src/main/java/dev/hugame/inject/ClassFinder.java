package dev.hugame.inject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

	public static List<Class<?>> getClassesInClasspath() {
		System.out.println("Calling getClassesInClasspath");
		System.out.println("Java class path:");
		var stuffs = Arrays.asList(System.getProperty("java.class.path").split(File.pathSeparator));
		
		List<File> files = new ArrayList<>();
		
		for (var s : stuffs) {
			var file = new File(s);
			System.out.println("---");
			System.out.println(file.getName());
			System.out.println(file.isDirectory());
			
			if (file.isDirectory()) {
				files.addAll(getFilesRecursive(file));
			} else {
				files.add(file);
			}
		}
		
		System.out.println(files);
		
		
		System.out.println(stuffs);
		System.out.println("Then:");
		
		ClassLoader classLoader = ClassFinder.class.getClassLoader();
		System.out.println(classLoader instanceof URLClassLoader);
		URLClassLoader myUcl = (URLClassLoader) classLoader;
		for (URL url : myUcl.getURLs()) {
		    System.out.println(url.toString());
		}
		
//		try {
//			var stuff = classLoader.getResources("");
//			List<URL> urls = new ArrayList<>();
//			
//			while(stuff.hasMoreElements()) {
//				urls.add(stuff.nextElement());
//			}
//			
//			for (var url : urls) {
//				File root = new File(url.getPath());
//				System.out.println("Root: " + root.getName());
//				if (!root.isDirectory()) continue;
////				System.out.println(getSubPackages(root));
//				
//				var classes = getFilesRecursive(root)
//						.stream()
//						.filter(f -> f.getName().endsWith(".class"))
//						.toList();
//				
//				System.out.println(classes);
//			}
//			
//			return null;
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		return null;
	}
	
	public static List<File> getSubPackages(File dir) {
		return Arrays.asList(dir.listFiles())
				.stream()
				.filter(f -> f.isDirectory())
				.toList();
	}
	
	private static List<File> getFilesRecursive(File dir) {
		return Arrays.asList(dir.listFiles())
				.stream()
				.flatMap(f -> f.isFile() ? Stream.of(f) :
					getFilesRecursive(f).stream()).toList();
	}
}
