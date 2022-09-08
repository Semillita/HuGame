package dev.hugame.inject;

public class InjectionEngine {

	public void start() {
//		var globalMethods = ClassFinder.getMethodsWithAnnotation(".", Global.class);
//		System.out.println(globalMethods);
		System.out.println(ClassFinder.getClassesInClasspath());
	}
	
}
