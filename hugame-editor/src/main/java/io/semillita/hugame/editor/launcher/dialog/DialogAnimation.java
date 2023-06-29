package io.semillita.hugame.editor.launcher.dialog;

public record DialogAnimation(Type type, long startTimeNano) {
  private static final double ANIMATION_TIME = 0.2;

  public enum Type {
    ENABLING,
    DISABLING
  }

  public DialogAnimation(Type type) {
    this(type, System.nanoTime());
  }

  public boolean isFinished() {
    return getProgress() >= 1;
  }

  public double getProgress() {
    return getElapsedTime() / ANIMATION_TIME;
  }

  private double getElapsedTime() {
    return (System.nanoTime() - startTimeNano) / 1_000_000_000d;
  }
}
