package io.semillita.hugame.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

public class Files {

	public static Optional<String> read(String filepath) {
		try {
			byte[] bytes = Files.class.getResourceAsStream(filepath).readAllBytes();
			return Optional.of(new String(bytes, "ISO-8859-1"));
		} catch (IOException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
	
}
