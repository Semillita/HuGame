package io.semillita.hugame.editor.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import tools.jackson.databind.ObjectMapper;

public class ConfigFileUtils {
  private static final String DIRECTORY_PATH = System.getProperty("user.home") + "\\.hugame";
  private static final String FILE_PATH = DIRECTORY_PATH + "\\config.json";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static ConfigFile readOrCreate() {
    var directoryPath = Path.of(DIRECTORY_PATH);
    if (!Files.exists(directoryPath)) {
      try {
        Files.createDirectory(directoryPath);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    var path = Path.of(FILE_PATH);

    if (Files.exists(path)) {
      return OBJECT_MAPPER.readValue(path, ConfigFile.class);
    }

    try {
      Files.createFile(path);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    var configFile = new ConfigFile(Collections.emptyList());
    OBJECT_MAPPER.writeValue(path, configFile);
    return configFile;
  }

  public static void save(ConfigFile configFile) {
    OBJECT_MAPPER.writeValue(Path.of(FILE_PATH), configFile);
  }
}
