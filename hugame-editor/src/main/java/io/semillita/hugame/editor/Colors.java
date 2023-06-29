package io.semillita.hugame.editor;

import java.awt.Color;
import java.util.Random;

public class Colors {

  public static final Color WHITE = Color.WHITE;
  public static final Color GRADIENT_1 = new Color(20, 20, 20);
  public static final Color GRADIENT_2 = new Color(30, 30, 30);
  public static final Color GRADIENT_3 = new Color(30, 30, 30);
  public static final Color GRADIENT_4 = new Color(120, 120, 120);

  public static final Color BACKGROUND = new Color(29, 33, 37);
  public static final Color FOREGROUND = new Color(36, 41, 46);

  public static final Color BRIGHT_FOREGROUND = new Color(47, 54, 61);
  public static final Color BRIGHT_FOREGROUND_2 = new Color(54, 61, 68);

  public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

  public static class Launcher {
    public static final Color GRADIENT_2 = new Color(19, 19, 19);
    public static final Color BACKGROUND_DARK = new Color(12, 12, 12);
    public static final Color BACKGROUND_LIGHT = new Color(22, 22, 22);
    public static final Color OUTLINES = new Color(58, 58, 58);
    public static final Color MENU_BUTTON_BACKGROUND = new Color(17, 17, 17);
    public static final Color LOGO = new Color(124, 191, 174);
    public static final Color VERSION = new Color(109, 168, 154);
    public static final Color PRIMARY_TEXT = new Color(204, 204, 204);
    public static final Color SECONDARY_TEXT = new Color(150, 150, 150);
    public static final Color PROJECT_DETAILS_BACKGROUND = new Color(17, 17, 17);
  }

  public static Color random() {
    var random = new Random();
    return new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
  }
}
