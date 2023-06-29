package io.semillita.hugame.editor.launcher;

import io.semillita.hugame.editor.config.ConfigFile;
import io.semillita.hugame.editor.config.ConfigFileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;

public class ProjectGenerator {
  private static final String HUGAME_DEPENDENCY = "io.semillita.hugame:hugame:0.0.1";
  private static final String PROJECT_VERSION = "0.0.1";
  private static final String JAVA_VERSION = "21";

  public static void createProject(
      String title, String parentDirectory, String artifactId, String groupId) {
    System.out.println(
        "Creating project "
            + title
            + " as "
            + groupId
            + ":"
            + artifactId
            + " in "
            + parentDirectory);

    var projectRoot = parentDirectory + "\\" + artifactId;
    var projectRootPath = Path.of(projectRoot);
    if (Files.exists(projectRootPath)) {
      throw new RuntimeException();
    }

    createDirectory(projectRoot);
    createDirectory(projectRoot, "src");
    createDirectory(projectRoot, "src", "main");
    createDirectory(projectRoot, "src", "main", "java");
    createDirectory(projectRoot, "src", "main", "resources");

    var buildGradle = createFile(projectRoot, "build.gradle");
    buildGradle.write(
        """
                plugins {
                    id 'java-library'
                    id 'maven-publish'
                }

                repositories {
                    mavenLocal()
                    mavenCentral()
                }

                dependencies {
                    implementation "%s"
                }

                group = '%s'
                version = '%s'
                description = 'HuGame project'
                java.sourceCompatibility = JavaVersion.VERSION_%s

                publishing {
                    publications {
                        maven(MavenPublication) {
                            from(components.java)
                        }
                    }
                }
                """
            .formatted(HUGAME_DEPENDENCY, groupId, PROJECT_VERSION, JAVA_VERSION));

    var configFile = ConfigFileUtils.readOrCreate();
    configFile.projects().add(new ConfigFile.Project(title, projectRoot, getCurrentDate()));
    ConfigFileUtils.save(configFile);
  }

  private static String getCurrentDate() {
    var now = ZonedDateTime.now();
    var year = String.valueOf(now.getYear());
    var monthValue = now.getMonthValue();
    var month = (monthValue < 10) ? "0" + monthValue : String.valueOf(monthValue);
    var dayValue = now.getDayOfMonth();
    var day = (dayValue < 10) ? "0" + dayValue : String.valueOf(dayValue);

    return String.format("%s-%s-%s".formatted(year, month, day));
  }

  private static void createDirectory(String first, String... rest) {
    var path = Path.of(first, rest);

    try {
      Files.createDirectory(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static CreatedFile createFile(String first, String... rest) {
    var path = Path.of(first, rest);

    try {
      Files.createFile(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return new CreatedFile(path);
  }

  private static class CreatedFile {
    private final Path path;

    public CreatedFile(Path path) {
      this.path = path;
    }

    private void write(String content) {
      try {
        Files.writeString(path, content);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
