package io.semillita.hugame.editor.launcher;

import io.semillita.hugame.editor.Colors;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

public class LauncherMenuButton extends JButton {
  public enum Type {
    NEW_PROJECT("New Project"),
    IMPORT_PROJECT("Import Project");

    private final String text;

    private Type(String text) {
      this.text = text;
    }
  }

  private final Type type;
  private final Runnable onClick;

  public LauncherMenuButton(Type type, Runnable onClick) {
    setBackground(Colors.Launcher.MENU_BUTTON_BACKGROUND);
    setBorder(BorderFactory.createLineBorder(Colors.Launcher.OUTLINES, 1));

    this.type = type;
    this.onClick = onClick;

    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseEntered(MouseEvent e) {
            setBackground(Colors.Launcher.BACKGROUND_LIGHT);
            super.mouseEntered(e);
          }

          @Override
          public void mouseExited(MouseEvent e) {
            setBackground(Colors.Launcher.MENU_BUTTON_BACKGROUND);
            super.mouseExited(e);
          }
        });

    addActionListener(e -> onClick.run());
  }

  @Override
  public void paint(Graphics g) {
    var g2d = (Graphics2D) g;

    var radius = 15;
    g2d.setColor(getBackground());
    var rect = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
    g2d.fill(rect);

    g2d.setColor(Colors.Launcher.OUTLINES);
    g2d.draw(rect);

    g2d.setColor(Colors.Launcher.PRIMARY_TEXT);
    g2d.setFont(new Font("Microsoft YaHei Light", Font.PLAIN, 20));
    g2d.drawString(type.text, 10, 30);

    if (type == Type.NEW_PROJECT) {
      g2d.drawLine(180, 23, 200, 23);
      g2d.drawLine(190, 13, 190, 33);
    } else if (type == Type.IMPORT_PROJECT) {
      g2d.drawLine(190, 13, 190, 28);
      g2d.drawLine(185, 23, 190, 28);
      g2d.drawLine(195, 23, 190, 28);
      g2d.drawLine(185, 33, 195, 33);
    }
  }
}
