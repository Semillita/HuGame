package io.semillita.hugame.editor.launcher;

import io.semillita.hugame.editor.Colors;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

public class ProjectDetailsPanel extends JButton {
  private final String title;
  private final String location;
  private final String createdAt;
  private final int initialY;
  private final Runnable onClick;
  int i = 0;
  Color lastColor = Color.RED;

  public ProjectDetailsPanel(
      String title, String location, String createdAt, int initialY, Runnable onClick) {
    setBackground(Colors.Launcher.PROJECT_DETAILS_BACKGROUND);
    setBorder(BorderFactory.createLineBorder(Colors.Launcher.OUTLINES, 5));
    // setOpaque(true);
    this.title = title;
    this.location = location;
    this.createdAt = createdAt;
    this.initialY = initialY;
    this.onClick = onClick;

    addActionListener(ignored -> onClick.run());

    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseEntered(MouseEvent e) {
            setBackground(Colors.Launcher.BACKGROUND_LIGHT);
            super.mouseEntered(e);
          }

          @Override
          public void mouseExited(MouseEvent e) {
            setBackground(Colors.Launcher.PROJECT_DETAILS_BACKGROUND);
            super.mouseExited(e);
          }
        });
  }

  public void updateY(int scroll) {
    setLocation(getX(), initialY - scroll);
  }

  @Override
  public void paint(Graphics g) {
    paintComponent(g);
  }

  @Override
  public void paintComponent(Graphics g) {
    var g2d = (Graphics2D) g;
    var radius = 15;
    var rect = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

    g2d.setColor(getBackground());
    g2d.fill(rect);

    g2d.setColor(Colors.Launcher.OUTLINES);
    g2d.draw(rect);

    // g2d.setFont(new Font("Gadugi", Font.PLAIN, 20));
    g2d.setFont(new Font("Microsoft YaHei Light", Font.BOLD, 20));
    g2d.setColor(Colors.Launcher.PRIMARY_TEXT);
    g2d.drawString(this.title, 15, 30);

    g2d.setFont(new Font("Nirmala UI", Font.PLAIN, 14));
    g2d.setColor(Colors.Launcher.SECONDARY_TEXT);
    g2d.drawString(location, 15, 53);

    g2d.setFont(new Font("Nirmala UI", Font.PLAIN, 14));
    g2d.setColor(Colors.Launcher.PRIMARY_TEXT);
    g2d.drawString(createdAt, 15, 75);
  }
}
