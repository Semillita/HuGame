package dev.hugame.util;

public class Logger {
	private static int depth;

	public static void pushScope(String message) {
		log(message);
		depth++;
	}

	public static void popScope() {
		depth--;
	}

	public static void error(String message) {
		log("[Error] " + message);
	}

	public static void log(String message) {
		if (depth >= 1) {
			System.out.print("|   ".repeat(depth - 1));
			System.out.print("|   ");
		}
		System.out.println(message);
	}
}
