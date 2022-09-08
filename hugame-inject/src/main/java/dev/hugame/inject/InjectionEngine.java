package dev.hugame.inject;

public class InjectionEngine {

	public void start() {
		System.out.println(ClassFinder.getClassesInClasspath(this::testClassName));
	}

	private boolean testClassName(String className) {
		if (className.startsWith("org/lwjgl")) {
			System.out.println("Failed");
			return false;
		}
		
		return true;
	}
	
}
