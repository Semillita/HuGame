package io.semillita.hugame.editor.launcher.dialog;

import io.semillita.hugame.editor.Colors;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;

public class CreateProjectButton extends JButton {
  private Color backgroundColor = Colors.Launcher.MENU_BUTTON_BACKGROUND;

  public CreateProjectButton(Runnable onClick) {
    setBackground(Colors.TRANSPARENT);
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder());

    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseEntered(MouseEvent e) {
            backgroundColor = Colors.Launcher.BACKGROUND_LIGHT;
            super.mouseEntered(e);
          }

          @Override
          public void mouseExited(MouseEvent e) {
            backgroundColor = Colors.Launcher.MENU_BUTTON_BACKGROUND;
            super.mouseExited(e);
          }
        });

    addActionListener(ignored -> onClick.run());
  }

  @Override
  protected void paintBorder(Graphics g) {
    var g2d = (Graphics2D) g;
    var rect = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

    g2d.setColor(backgroundColor);
    g2d.fill(rect);

    g2d.setColor(Colors.Launcher.OUTLINES);
    g2d.draw(rect);

    g2d.setColor(Colors.Launcher.PRIMARY_TEXT);
    g2d.setFont(new Font("Microsoft YaHei Light", Font.PLAIN, 20));
    g2d.drawString("Create Project", 30, 27);
  }
}
