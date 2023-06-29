package io.semillita.hugame.editor.config;

import java.util.List;

public record ConfigFile(List<Project> projects) {
  public static record Project(String title, String location, String createdAt) {}
}
