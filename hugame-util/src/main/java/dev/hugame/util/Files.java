package dev.hugame.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Files {
	private static final Map<String, byte[]> bytesInFiles = new HashMap<>();;
	
	public static Optional<String> read(String filepath) {
		try {
			if (bytesInFiles.containsKey(filepath)) {
				return Optional.of(new String(bytesInFiles.get(filepath), "ISO-8859-1"));
			} else {
				byte[] bytes = Files.class.getResourceAsStream(filepath).readAllBytes();
				bytesInFiles.put(filepath, bytes);
				return Optional.of(new String(bytes, "ISO-8859-1"));
			}
		} catch (IOException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
	
	public static Optional<byte[]> readBytes(String filepath) {
		try {
			if (bytesInFiles.containsKey(filepath)) {
				return Optional.of(bytesInFiles.get(filepath));
			} else {
				var resourceStream = Files.class.getResourceAsStream(filepath);
				if (resourceStream == null) {
					throw new IllegalArgumentException("Invalid filepath: [" + filepath + "], " + 
				"try [/" + filepath + "] instead");
				}
				byte[] bytes = resourceStream.readAllBytes();
				resourceStream.close();
				bytesInFiles.put(filepath, bytes);
				return Optional.of(bytes);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
}
