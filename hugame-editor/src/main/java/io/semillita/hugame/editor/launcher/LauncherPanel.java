package io.semillita.hugame.editor.launcher;

import io.semillita.hugame.editor.Colors;
import io.semillita.hugame.editor.config.ConfigFileUtils;
import io.semillita.hugame.editor.launcher.dialog.Dialog;
import io.semillita.hugame.editor.launcher.dialog.DialogAnimation;
import io.semillita.hugame.editor.launcher.dialog.NewProjectDialog;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.*;

public class LauncherPanel extends JPanel {
  public static enum DialogState {
    SHOWING,
    HIDING,
    ENABLING,
    DISABLING
  }

  public static final int SLANT_WIDTH = 100;
  private static final double DIALOG_ANIMATION_TIME = 0.2;
  private static final int DIALOG_CLOSE_BUTTON_Y = 20;
  private static final int SCROLL_THUMB_WIDTH = 6;
  private static final int SLANT_START = 600;
  private static final int SLANT_END = SLANT_START + SLANT_WIDTH;
  private static final int DIALOG_SLANT_END = 620;
  private static final int SCROLL_SPEED = 20;

  private final LauncherMenuButton newProjectButton;
  private final LauncherMenuButton importProjectButton;
  private final List<ProjectDetailsPanel> projects;

  private final Consumer<String> onOpenProject;

  private DialogState dialogState = DialogState.HIDING;
  private Dialog dialog;
  private DialogAnimation dialogAnimation;
  private long dialogProgressStartNano = 0;
  private Timer timer = null;
  private int scroll = 0;

  public LauncherPanel(Consumer<String> onOpenProject) {
    this.onOpenProject = onOpenProject;

    this.newProjectButton =
        new LauncherMenuButton(
            LauncherMenuButton.Type.NEW_PROJECT, () -> enableDialog(NewProjectDialog::new));
    add(newProjectButton);
    newProjectButton.setBounds(700, 130, 220, 45);

    this.importProjectButton =
        new LauncherMenuButton(
            LauncherMenuButton.Type.IMPORT_PROJECT, () -> System.out.println("Nothing happening"));
    add(importProjectButton);
    importProjectButton.setBounds(700, 195, 220, 45);

    this.projects = new ArrayList<>();
    var configFile = ConfigFileUtils.readOrCreate();
    for (var project : configFile.projects()) {
      addProject(project.title(), project.location(), project.createdAt());
    }
    /*addProject("Brawlhalla", "C://hugob/programmering/brawlhalla", "2025-12-06");
    addProject("Minecraft", "C://hugob/projects/Minecraft", "2027-10-17");
    addProject("WLB", "C://hugob/desktop/WLB", "2025-07-04");
    addProject("Brawlhalla", "C://hugob/programmering/brawlhalla", "2025-12-06");
    addProject("Minecraft", "C://hugob/projects/Minecraft", "2027-10-17");
    addProject("WLB", "C://hugob/desktop/WLB", "2025-07-04");
    addProject("Brawlhalla", "C://hugob/programmering/brawlhalla", "2025-12-06");
    addProject("Minecraft", "C://hugob/projects/Minecraft", "2027-10-17");
    addProject("WLB", "C://hugob/desktop/WLB", "2025-07-04");*/

    createScrollListener();
  }

  @Override
  public void paint(Graphics g) {
    g.setColor(Colors.Launcher.BACKGROUND_DARK);
    g.fillRect(0, 0, getWidth(), getHeight());

    var g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g.setColor(Colors.Launcher.BACKGROUND_LIGHT);
    g.fillPolygon(
        new int[] {SLANT_END, getWidth(), getWidth(), SLANT_START},
        new int[] {0, 0, getHeight(), getHeight()},
        4);

    g.setColor(Colors.Launcher.OUTLINES);
    g.drawLine(SLANT_END, 0, SLANT_START, getHeight());

    g.setColor(Colors.Launcher.LOGO);
    g.setFont(new Font("Segoe UI Black", Font.PLAIN, 45));
    g.drawString("HuGame", 730, 60);

    g.setColor(Colors.Launcher.VERSION);
    g.setFont(new Font("Segoe UI Black", Font.PLAIN, 15));
    g.drawString("Version 0.0.1", 820, 80);

    updateDialogAnimation();

    var childrenToDrawLater = new HashSet<Component>();
    for (var c : getComponents()) {
      if (c.getClass() != LauncherMenuButton.class && c.getClass() != ProjectDetailsPanel.class) {
        childrenToDrawLater.add(c);
        continue;
      }
      var childGraphics = g.create(c.getX(), c.getY(), c.getWidth(), c.getHeight());
      c.paint(childGraphics);
      childGraphics.dispose();
    }

    /*switch (dialogState) {
        case SHOWING -> drawDialog(g2d, 1);
        case ENABLING, DISABLING -> drawDialog(g2d, getDialogProgress());
    }*/

    for (var c : childrenToDrawLater) {
      var childGraphics = g.create(c.getX(), c.getY(), c.getWidth(), c.getHeight());
      c.paint(childGraphics);
      childGraphics.dispose();
    }

    drawScrollBar(g2d);

    g.setColor(Colors.Launcher.OUTLINES);
    g.drawLine(0, 0, getWidth(), 0);
  }

  public void enableDialog(Function<Runnable, Dialog> getDialog) {
    removeDialog();
    this.dialog = getDialog.apply(this::disableDialog);
    dialog.setSize(620, LauncherWindow.HEIGHT);
    dialog.setLocation(0, 0);
    add(dialog);

    startDialogAnimation(DialogState.ENABLING, DialogAnimation.Type.ENABLING);
  }

  private void disableDialog() {
    ;
    startDialogAnimation(DialogState.DISABLING, DialogAnimation.Type.DISABLING);
  }

  private void startDialogAnimation(DialogState dialogState, DialogAnimation.Type type) {
    this.dialogState = dialogState;
    dialogProgressStartNano = System.nanoTime();
    dialogAnimation = new DialogAnimation(type);

    this.timer =
        new Timer(
            (int) (1000 * (1d / 150)),
            e -> {
              repaint();
            });
    timer.start();
  }

  private void removeDialog() {
    if (this.dialog != null) {
      remove(dialog);
      this.dialog = null;
    }
  }

  private void createScrollListener() {
    addMouseWheelListener(
        e -> {
          var scrollAreaHeight = getScrollAreaHeight();
          if (scrollAreaHeight <= LauncherWindow.HEIGHT || dialogState != DialogState.HIDING) {
            return;
          }

          var maxScroll = scrollAreaHeight - LauncherWindow.HEIGHT;

          var scrollChange = e.getPreciseWheelRotation() * SCROLL_SPEED;
          if (scroll + scrollChange < 0) {
            scroll = 0;
          } else if (scroll + scrollChange > maxScroll) {
            scroll = maxScroll;
          } else {
            scroll += (int) scrollChange;
          }

          for (var project : projects) {
            project.updateY(scroll);
          }

          LauncherPanel.this.repaint();
        });
  }

  private void drawScrollBar(Graphics2D g) {
    var windowHeight = LauncherWindow.HEIGHT;
    var scrollAreaHeight = getScrollAreaHeight();
    var partShown = ((double) windowHeight) / scrollAreaHeight;
    if (partShown >= 1) {
      return;
    }

    var slant = ((double) 100) / LauncherWindow.HEIGHT;

    var thumbHeight = (int) (partShown * LauncherWindow.HEIGHT);
    var movementSpace = LauncherWindow.HEIGHT - thumbHeight;
    var scrollProgress = scroll / (double) (scrollAreaHeight - windowHeight);

    var minY = 1;
    var maxY = LauncherWindow.HEIGHT - 10;

    // var y1 = minY;
    var y1 = minY + scrollProgress * movementSpace;
    var x1 = SLANT_END - (int) (y1 * slant);

    // var y2 = minY + partShown * (maxY - minY);
    var y2 = y1 + thumbHeight;
    var x2 = SLANT_END - (int) (y2 * slant);

    var length = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

    var shape =
        new RoundRectangle2D.Double(
            0, 0, SCROLL_THUMB_WIDTH, length, SCROLL_THUMB_WIDTH, SCROLL_THUMB_WIDTH);

    var shear = new AffineTransform();
    shear.shear(-SLANT_WIDTH / (double) LauncherWindow.HEIGHT, 0.2);

    var translate = new AffineTransform();
    translate.translate(x1 - SCROLL_THUMB_WIDTH / 2, y1);

    var combined = new AffineTransform(translate);
    combined.concatenate(shear);

    var leaningShape = combined.createTransformedShape(shape);

    g.setColor(Colors.Launcher.OUTLINES);
    g.fill(leaningShape);
  }

  private void updateDialogAnimation() {
    if (dialogAnimation == null) {
      return;
    }

    if (dialogAnimation.isFinished()) {
      switch (dialogAnimation.type()) {
        case ENABLING -> {
          dialogState = DialogState.SHOWING;
          dialogAnimation = null;
          dialog.setX(0);
        }
        case DISABLING -> {
          dialogState = DialogState.HIDING;
          dialogAnimation = null;
          removeDialog();
        }
      }
      timer.stop();
    } else {
      var animationProgress = dialogAnimation.getProgress();
      var xProgress =
          (dialogAnimation.type() == DialogAnimation.Type.ENABLING)
              ? animationProgress
              : (1 - animationProgress);
      dialog.setX((int) (DIALOG_SLANT_END * (xProgress - 1)));
    }
  }

  private double getDialogProgress() {
    var timeElapsed = (System.nanoTime() - dialogProgressStartNano) / 1_000_000_000d;
    var animationProgress = timeElapsed / DIALOG_ANIMATION_TIME;

    return (dialogState == DialogState.ENABLING) ? animationProgress : (1 - animationProgress);
  }

  @SuppressWarnings("unchecked")
  private <T> T[] reversed(T[] source) {
    var newArray = (T[]) new Object[source.length];
    for (int i = 0; i < source.length; i++) {
      newArray[i] = source[source.length - i - 1];
    }

    return newArray;
  }

  private void addProject(String title, String location, String createdAt) {
    var y = 60 + 110 * projects.size();
    var project =
        new ProjectDetailsPanel(
            title, location, createdAt, y, () -> onOpenProject.accept(location));
    project.setBounds(100, y, 380, 90);
    projects.add(project);
    add(project);
  }

  private int getScrollAreaHeight() {
    var height = 60 + 60;
    if (projects.isEmpty()) {
      return height;
    }
    height += 90;
    for (int i = 1; i < projects.size(); i++) {
      height += 110;
    }

    return height;
  }
}
