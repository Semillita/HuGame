package dev.hugame.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {

	public static float[] toFloatArray(List<Float> list) {
		float[] array = new float[list.size()];
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
		return array;
	}
	
	public static byte[] toByteArray(Number... numbers) {
		List<Byte> list = new ArrayList<>();
		for (var num : numbers) {
			if (num instanceof Integer) {
				
			}
		}
		return null;
	}

	public static String getCallingMethod() {
		var stackTraceElements = Thread.currentThread().getStackTrace();
		var element = stackTraceElements[3];
		var fullClassName = element.getClassName();
		var simpleClassName = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);

		return "%s#%s:%d".formatted(simpleClassName, element.getMethodName(), element.getLineNumber());
	}
}
